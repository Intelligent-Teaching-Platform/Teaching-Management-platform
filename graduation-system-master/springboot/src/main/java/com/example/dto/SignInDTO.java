package com.example.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SignInDTO {
    private Integer id;
    private String qrCodeUrl;
    private Date expireTime;


}