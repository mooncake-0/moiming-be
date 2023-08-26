package com.peoplein.moiming.support;

import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 Date 혹은 다양한 Format 관련하여
 @JsonFormatter 등이 변환을 해줘서 String 값을 넣어주기 어려운 Dto 들이 있다.
 사실상 RequestDto 를 JSON 으로 잘 만들어주기 위함. 그 이후는 상관 없음
 */
public class TestDto {


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestMemberRequestDto {

        private String memberEmail;
        private String password;
        private String memberName;
        private String memberPhone;
        private MemberGender memberGender;
        private boolean isForeigner;
        private String memberBirth; // 사실 얘 때문 ..
        private String fcmToken;

        /*public TestMemberRequestDto() {

        }

        public TestMemberRequestDto(String memberEmail, String password, String memberName, String memberPhone, MemberGender memberGender
                , boolean isForeigner, String memberBirth, String fcmToken) {

            this.memberEmail = memberEmail;
            this.password = password;
            this.memberName = memberName;
            this.memberPhone = memberPhone;
            this.memberGender = memberGender;
            this.isForeigner = isForeigner;
            this.memberBirth = memberBirth;
            this.fcmToken = fcmToken;

        }*/

    }

}
