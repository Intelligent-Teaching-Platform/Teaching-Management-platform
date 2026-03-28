package com.example.exception;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.example.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice(basePackages = "com.example.controller")
public class GlobalExceptionHandler {

    public static final Log log = LogFactory.get();

    /** 数据库错误（如缺少 cover 列）返回明确提示，避免只显示笼统的「请求失败」 */
    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    public Result dataAccessError(HttpServletRequest request, DataAccessException e) {
        log.error("数据库异常：", e);
        Throwable c = e.getMostSpecificCause();
        String detail = c != null ? c.getMessage() : e.getMessage();
        if (detail != null && detail.toLowerCase().contains("cover")) {
            return Result.error("保存失败：task 表缺少 cover 字段。请在数据库执行 sql/task_cover.sql（ADD COLUMN cover）后重试。");
        }
        if (detail != null && detail.toLowerCase().contains("course_id")) {
            return Result.error("保存失败：task 表缺少 course_id 字段，请执行项目内相关 ALTER 脚本后重试。");
        }
        return Result.error("保存失败（数据库）：" + (detail != null ? detail : "未知错误"));
    }

    //统一异常处理@ExceptionHandler,主要用于Exception
    @ExceptionHandler(Exception.class)
    @ResponseBody//返回json串
    public Result error(HttpServletRequest request, Exception e) {
        log.error("异常信息：", e);
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody//返回json串
    public Result customError(HttpServletRequest request, CustomException e) {
        return Result.error(e.getMsg());
    }

}
