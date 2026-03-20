package com.example.controller;

import com.example.common.Result;
import com.example.entity.Student;
import com.example.entity.Teacher;
import com.example.service.TeacherService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师模块前端请求接口入口
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {
    @Resource
    private TeacherService teacherService;

    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Teacher teacher) {

        teacherService.add(teacher);
        return Result.success();
    }

    /**
     * 更新
     */
    @PutMapping("/update")
    public Result update(@RequestBody Teacher teacher) {
        teacherService.updateByID(teacher);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Teacher teacher,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize) {
        PageInfo<Teacher> pageInfo = teacherService.selectPage(teacher, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        teacherService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询所有教师信息
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Teacher> list = teacherService.selectAll();
        return Result.success(list);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result importTeachers(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("请选择上传文件");
            }

            String contentType = file.getContentType();
            if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
                return Result.error("仅支持.xlsx格式的Excel文件");
            }

            List<Teacher> teachers = parseExcelFile(file);
            teacherService.batchInsert(teachers); // 确保TeacherService中有这个方法
            return Result.success("成功导入" + teachers.size() + "条数据");
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

/**
 * 解析Excel文件内容
 */
private List<Teacher> parseExcelFile(MultipartFile file) throws IOException {
    List<Teacher> teachers = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Teacher teacher = new Teacher();
            teacher.setUsername(getStringValue(row.getCell(0)));
            teacher.setPassword(getStringValue(row.getCell(1)));
            teacher.setName(getStringValue(row.getCell(2)));
            teacher.setSex(getStringValue(row.getCell(3)));
            teacher.setTitle(getStringValue(row.getCell(4)));
            teacher.setSpecialityId(getNumericValue(row.getCell(5)));
            teacher.setAvatar(getStringValue(row.getCell(7)));
            teacher.setRole("TEACHER");


            teachers.add(teacher);
        }
    }
    return teachers;
}

/**
 * 获取单元格字符串值
 */
private String getStringValue(Cell cell) {
    if (cell == null) {
        return null;
    }
    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue().trim();
        case NUMERIC:
            return String.valueOf((int) cell.getNumericCellValue());
        default:
            return null;
    }
}

/**
 * 获取单元格数值
 */
private Integer getNumericValue(Cell cell) {
    if (cell == null) {
        return 0;
    }
    switch (cell.getCellType()) {
        case NUMERIC:
            return (int) cell.getNumericCellValue();
        case STRING:
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        default:
            return 0;
    }
}
}





