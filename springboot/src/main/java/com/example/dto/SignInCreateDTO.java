package com.example.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignInCreateDTO {
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @NotNull(message = "签到时长不能为空")
    @Min(value = 1, message = "签到时长至少1分钟")
    @Max(value = 60, message = "签到时长最多60分钟")
    private Integer duration;

    @NotNull(message = "允许距离不能为空")
    @Min(value = 50, message = "允许距离至少50米")
    @Max(value = 1000, message = "允许距离最多1000米")
    private Integer distance;

    private Integer teacherId;

    private Integer clazzId;


}