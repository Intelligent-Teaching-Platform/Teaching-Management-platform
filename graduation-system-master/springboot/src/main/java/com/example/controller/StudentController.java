package com.example.controller;

import com.example.common.Result;
import com.example.entity.Student;
import com.example.service.StudentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生模块前端请求接口入口
 */
@RestController
@RequestMapping("/student")
public class StudentController {
    @Resource
    private StudentService studentService;

    /**
     * 增加学生
     */
    @PostMapping("/add")
    public Result add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/update")
    public Result update(@RequestBody Student student) {
        studentService.updateByID(student);
        return Result.success();
    }

    /**
     * 分页查询学生
     */
    @GetMapping("/selectPage")
    public Result selectPage(Student student,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize) {
        PageInfo<Student> pageInfo = studentService.selectPage(student, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        studentService.deleteById(id);
        return Result.success();
    }

    /**
     * 根据ID查询学生
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Student student = studentService.selectById(id);
        return Result.success(student);
    }

    /**
     * 从Excel导入学生数据
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result importStudents(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 基础校验
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }

            // 2. 更严格的文件类型验证
            String contentType = file.getContentType();
            if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
                return Result.error("仅支持.xlsx格式的Excel文件");
            }

            // 3. 解析Excel
            List<Student> students = parseExcelFile(file);

            // 4. 批量插入
            studentService.batchInsert(students);
            return Result.success("成功导入" + students.size() + "条数据");
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 解析Excel文件内容
     */
    private List<Student> parseExcelFile(MultipartFile file) throws IOException {
        List<Student> students = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Student student = new Student();
                student.setUsername(getStringValue(row.getCell(0)));
                student.setName(getStringValue(row.getCell(1)));
                student.setSex(getStringValue(row.getCell(2)));
                student.setCode(getStringValue(row.getCell(3)));
                student.setScore(getNumericValue(row.getCell(4)));
                student.setCollegeId(getNumericValue(row.getCell(5)));
                student.setClassId(getNumericValue(row.getCell(6)));
                student.setRole("student");

                students.add(student);
            }
        }
        return students;
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

    @GetMapping("/selectByCourseId")
    public Result selectByCourseId(@RequestParam Integer courseId) {
        List<Student> list  = studentService.selectByCourseId(courseId);
        return Result.success(list);
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Student> list  = studentService.selectAll();
        return Result.success(list);
    }

}
