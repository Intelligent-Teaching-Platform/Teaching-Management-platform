package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignInRecordDTO {

    @NotNull(message = "课程ID不能为空")
    private Integer courseId;


    @NotNull(message = "纬度不能为空")
    private Double latitude;

    @NotNull(message = "经度不能为空")
    private Double longitude;

    @NotNull(message = "学生学号不能为空")
    private Integer studentId;

}