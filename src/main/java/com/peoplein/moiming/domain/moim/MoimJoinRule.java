package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Entity
@Getter
@Table(name = "moim_join_rule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimJoinRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moim_join_rule_id")
    private Long id;

    /*
     isAgeRule = false 일 시 ageMax, ageMin = -1
     */
    private boolean isAgeRule;

    private int ageMax;

    private int ageMin;

    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;

    public static MoimJoinRule createMoimJoinRule(boolean isAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        if (!isAgeRule) {
            ageMax = -1;
            ageMin = -1;
        }

        if (ageMin >= ageMax) {
            throw new MoimingApiException("잘못된 설정입니다: 최소 나이가 더 큰 값");
        }

        return new MoimJoinRule(isAgeRule, ageMax, ageMin, memberGender);
    }


    private MoimJoinRule(boolean isAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        this.isAgeRule = isAgeRule;
        this.ageMax = ageMax;
        this.ageMin = ageMin;
        this.memberGender = memberGender;
    }

    public void judgeByRule(Member member) {

        if (this.isAgeRule) {
            int memberAge = member.getMemberAge();
            if (this.ageMin > memberAge || this.ageMax < memberAge) { // 최소 나이보다 작거나 최대 나이보다 많다면
                throw new MoimingApiException("요청한 유저가 가입 조건에 부합하지 않습니다: 나이 부적합");
            }
        }

        if (this.memberGender != MemberGender.N) {
            if (!member.getMemberInfo().getMemberGender().equals(this.memberGender)) {
                throw new MoimingApiException("요청한 유저가 가입 조건에 부합하지 않습니다: 요구성별 - " +  this.memberGender.toString());
            }
        }
    }


    // WARN: ID 변경은 MOCK 용: 호출된 곳이 test Pckg 인지 확인
    public void changeMockObjectIdForTest(Long mockObjectId, URL classUrl) {

        try {
            URI uri = classUrl.toURI();
            File file = new File(uri);
            String absolutePath = file.getAbsolutePath();

            if (absolutePath.contains("test")) { // 빌드 Class 경로가 test 내부일경우
                this.id = mockObjectId;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
