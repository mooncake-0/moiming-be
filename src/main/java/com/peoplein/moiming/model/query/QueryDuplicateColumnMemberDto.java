package com.peoplein.moiming.model.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class QueryDuplicateColumnMemberDto {

    private String uid;
    private String memberEmail;

    // ...  중복 불가 칼럼들 도입
    // private String memberPhone;

}
