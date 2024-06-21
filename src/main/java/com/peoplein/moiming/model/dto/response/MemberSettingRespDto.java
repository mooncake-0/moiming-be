package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.domain.member.MemberSetting;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MemberSettingRespDto {

    @ApiModel(value = "Member Setting API - 응답 - 회원 설정 조회")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberSettingViewRespDto{

        private boolean allowNoticeNotify;
        private boolean allowMoimNotify;

        public MemberSettingViewRespDto(MemberSetting memberSetting) {
            this.allowMoimNotify = memberSetting.isMoimActivityNotifyAllowed();
            this.allowNoticeNotify = memberSetting.isNoticeNotifyAllowed();
        }
    }

    @ApiModel(value = "Member Setting API - 응답 - 유저 알람 수정")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MemberSettingNotifyUpdateRespDto{

        private boolean allowNoticeNotify;
        private boolean allowMoimNotify;

        public MemberSettingNotifyUpdateRespDto(MemberSetting memberSetting) {
            this.allowMoimNotify = memberSetting.isMoimActivityNotifyAllowed();
            this.allowNoticeNotify = memberSetting.isNoticeNotifyAllowed();
        }
    }
}
