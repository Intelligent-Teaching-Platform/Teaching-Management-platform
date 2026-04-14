package com.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class SignInStatsDTO {
    private List<String> dates;
    private List<Integer> counts;


}