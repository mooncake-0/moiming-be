package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
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
     ageRuleFlag = false 일 시 ageMax, ageMin = -1
     */
    private boolean hasAgeRule;

    private int ageMax;

    private int ageMin;

    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;


    public static MoimJoinRule createMoimJoinRule(boolean hasAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        if (!hasAgeRule) {
            ageMax = -1;
            ageMin = -1;
        }

        // static 함수이므로 validateAge 는 생성자에서
        return new MoimJoinRule(hasAgeRule, ageMax, ageMin, memberGender);
    }


    private MoimJoinRule(boolean hasAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        validateAge(hasAgeRule, ageMin, ageMax);

        this.hasAgeRule = hasAgeRule;
        this.ageMax = ageMax;
        this.ageMin = ageMin;
        this.memberGender = memberGender;
    }


    public void judgeByRule(Member member) {
        if (this.hasAgeRule) {
            int memberAge = member.getMemberAge();
            if (this.ageMin > memberAge || this.ageMax < memberAge) { // 최소 나이보다 작거나 최대 나이보다 많다면
                throw new MoimingApiException(MOIM_JOIN_FAIL_BY_AGE_RULE);
            }
        }

        if (this.memberGender != MemberGender.N) {
            if (!member.getMemberInfo().getMemberGender().equals(this.memberGender)) {
                throw new MoimingApiException(MOIM_JOIN_FAIL_BY_GENDER_RULE);
            }
        }
    }


    public void changeJoinRule(boolean hasAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        if (!hasAgeRule) {
            ageMax = -1;
            ageMin = -1;
        }

        validateAge(hasAgeRule, ageMin, ageMax);

        this.hasAgeRule = hasAgeRule;
        this.ageMax = ageMax;
        this.ageMin = ageMin;
        this.memberGender = memberGender;
    }

    private void validateAge(boolean hasAgeRule, int ageMin, int ageMax) {
        // ageMin = -1 이면 없다는 뜻이므로, 통과할 수 있음 -> -1 이 아니면서 15 이하면 에러
        // ageMax= -1 이면 없다는 뜻이므로, 통과할 수 있음 -> 100을 넘기만 하면 에러
        if (ageMin > ageMax || (hasAgeRule && ageMin < 15) || (hasAgeRule && ageMax > 100)) {
            log.info("{}, validateAge :: {}", this.getClass().getName(), "최소 연령이 더 큰 값 or 최소 나이 15 미만이나 최대 나이 100 초과로 입력");
            throw new MoimingApiException(MOIM_RULE_AGE_NOT_VALID);
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
