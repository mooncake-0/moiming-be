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

    private int ageMax;

    private int ageMin;

    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;


    public static MoimJoinRule createMoimJoinRule(int ageMax, int ageMin, MemberGender memberGender, Member creator) {
        return new MoimJoinRule(ageMax, ageMin, memberGender, creator);
    }


    // 24.07 Update
    // validateAge 함수가 static 팩토리 함수에 추가될 수 없으므로, 생성자에서 진행한다
    // 여기서 Validation 을 해주는 느낌이니, Creator 의 적합 여부도 여기서 판단한다
    // Create Logic 에서 judgeByJoinRule 을 호출하는 것도 가능하지만, MoimJoinRule 이 생성되는 모든 Test 에서도 적용하기 위해 생성자단에서 적용한다
    private MoimJoinRule(int ageMax, int ageMin, MemberGender memberGender, Member creator) {

        // SET 을 해두고 해당 객체와 validation 을 한다
        // JudgeByRule 이 this 객체 값이기 때문에 먼저 Set 해줘야 함
        // 좀 이상하긴 한데 하기 로직을 진행하는데 아무 이슈 없음

        this.ageMin = ageMin;
        this.ageMax = ageMax;
        this.memberGender = memberGender;

        if (ageMax != -1 && ageMin != -1) {
            validateAge(ageMax, ageMin); // 요청한 값에 대해
            try {
                judgeByRule(creator); // 생성하려는 자 역시 대상이 된다
            } catch (MoimingApiException exception) { // 발생 예외 전환
                throw new MoimingApiException(MOIM_CREATE_FAIL_BY_UNAVAILABLE_JOIN_RULE);
            }
        }
    }


    public void judgeByRule(Member member) {
        int memberAge = member.getMemberInfo().getMemberAge();
        if (this.ageMin > memberAge || this.ageMax < memberAge) { // 최소 나이보다 작거나 최대 나이보다 많다면
            throw new MoimingApiException(MOIM_JOIN_FAIL_BY_AGE_RULE);
        }

        if (this.memberGender != MemberGender.N) {
            if (!member.getMemberInfo().getMemberGender().equals(this.memberGender)) {
                throw new MoimingApiException(MOIM_JOIN_FAIL_BY_GENDER_RULE);
            }
        }
    }


    public void changeJoinRule(int ageMax, int ageMin, MemberGender memberGender) {
        if (ageMax != -1 && ageMin != -1) {
            validateAge(ageMax, ageMin);
        }

        this.ageMax = ageMax;
        this.ageMin = ageMin;
        this.memberGender = memberGender;
    }

    private void validateAge(int ageMax, int ageMin) {
        // 최소 15, 최대 100
        if (ageMin > ageMax || ageMin < 15 || ageMax > 100) {
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
