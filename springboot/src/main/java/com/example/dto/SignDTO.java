package com.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class SignDTO {

    /** 当前签到记录ID（如有需要可使用） */
    private Integer signInId;
    /** 已签到人数 */
    private Integer num;
    /** 未签到人数 */
    private Integer unum;
    /** 已签到学生姓名列表 */
    private List<String> signedNames;
    /** 未签到学生姓名列表 */
    private List<String> unsignedNames;
}