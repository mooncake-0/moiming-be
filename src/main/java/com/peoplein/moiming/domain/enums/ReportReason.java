package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.peoplein.moiming.domain.enums.ReportTarget.*;

@Slf4j
@Getter
public enum ReportReason {

    INAPPROPRIATE_FOR_MINORS(null, 0, "음란물 또는 청소년 유해 내용 포함"),
    VIOLATION_CONTENT(null, 1, "저작권 침해 및 위반 콘텐츠, 스팸 내용"),
    INVASION_PRIVACY(null, 2, "개인정보의 침해, 욕설, 명예훼손"),
    ILLEGAL_MATERIALS(null, 3, "불법물 홍보 및 부적절한 홍보 노출"),
    INAPPROPRIATE_PURPOSE(null, 4, "다단계, 종교의 포교 목적 의심"),
    CRIME_CONTENT(null, 5, "사기, 허위, 범죄 등의 내용 포함"),
    EXTRA(null, -1, "기타"),

    SEXUAL_ASSAULT(USER, 0, "성희롱 또는 성적 행위"),
    ABUSE(USER, 1, "욕설/비하/혐오 발언"),
    SELF_ASSAULT(USER, 2, "자해/자살/섭식 장애"),
    INAPPROPRIATE_PURPOSE_USER(USER, 3, "다단계, 종교의 포교 목적 의심"),
    OBSESS(USER, 4, "과도한 개인 만남 및 연락처 요구"),
    INAPPROPRIATE_PROFILE_INFO(USER, 5, "프로필 사진, 닉네임에 문제"),
    BOTHER_MOIM(USER, 6, "비매너, 비협조적인 태도로 모임 방해"),
    EXTRA_USER(USER, -1, "기타");

    private final ReportTarget target;
    private final int index;
    private final String info;

    ReportReason(ReportTarget target, int index, String info) {
        this.target = target;
        this.index = index;
        this.info = info;
    }

    public static ReportReason findReason(ReportTarget target, int index) {
        if (target != null) { // report target 이 Map 안된 에러도 같이 점검
            boolean isTargetUser = target.equals(USER); // 변환하려는게 User 임
            for (ReportReason reason : ReportReason.values()) {
                if (isTargetUser) {
                    if (reason.getTarget() == null) { // Target 이 USER 가 아닌 것들은 패쓰
                        continue;
                    }
                    if (reason.getTarget().equals(USER) && reason.getIndex() == index) {
                        return reason;
                    }
                } else {
                    if (reason.getTarget() == null && reason.getIndex() == index) {
                        return reason;
                    }
                }
            }
        }
        log.error("{}, findReason :: {}", "ReportReason", "[" + target + ", " + index + "] 에 해당하는 객체를 찾을 수 없습니다");
        throw new MoimingApiException(ExceptionValue.COMMON_MAPPABLE_ENUM_VALUE);
    }
}
