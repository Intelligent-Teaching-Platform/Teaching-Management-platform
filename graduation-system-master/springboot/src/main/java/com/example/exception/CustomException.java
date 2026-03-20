package com.example.exception;

public class CustomException extends RuntimeException {
    private String msg;

    public CustomException(String msg, String 题型名称不能重复) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
