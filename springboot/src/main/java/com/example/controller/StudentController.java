package com.example.controller;

import com.example.common.Result;
import com.example.entity.Student;
import com.example.service.CollegeService;
import com.example.service.ClazzService;
import com.example.service.StudentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Resource
    private CollegeService collegeService;

    @Resource
    private ClazzService clazzService;

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
     * 重置学生密码为 123456
     */
    @PutMapping("/resetPassword/{id}")
    public Result resetPassword(@PathVariable Integer id) {
        studentService.resetPassword(id);
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
     * 下载学生批量导入模板（与 student/import 固定列索引严格对齐）
     *
     * 固定列索引（0..5）：
     * 0 username, 1 name, 2 sex, 3 code,
     * 4 collegeName（导入时会映射到 collegeId），5 className（导入时会映射到 classId）
     */
    @GetMapping("/importTemplate")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        String fileName = "学生批量导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );

        try (Workbook workbook = new XSSFWorkbook(); ServletOutputStream os = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("导入模板");
            Sheet help = workbook.createSheet("说明");

            // 表头（固定列索引：0..5）
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("用户名(username)");
            header.createCell(1).setCellValue("姓名(name)");
            header.createCell(2).setCellValue("性别(sex)");
            header.createCell(3).setCellValue("学号(code)");
            header.createCell(4).setCellValue("所属学院(collegeName，学院名字)");
            header.createCell(5).setCellValue("班级(className，班级名称)");

            // 示例行：第1行 i=1（导入解析从第2行开始）
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("stu01");
            example.createCell(1).setCellValue("王五");
            example.createCell(2).setCellValue("男");
            example.createCell(3).setCellValue("20230001");
            example.createCell(4).setCellValue("计算机学院");
            example.createCell(5).setCellValue("计科2201");

            // 说明 Sheet
            int r = 0;
            Row h0 = help.createRow(r++);
            h0.createCell(0).setCellValue("学生批量导入说明（请勿改动导入模板列顺序）");
            r++;
            Row h1 = help.createRow(r++);
            h1.createCell(0).setCellValue("列索引 -> 字段");
            Object[][] helpRows = new Object[][]{
                    {0, "用户名(username)", "必填"},
                    {1, "姓名(name)", "必填"},
                    {2, "性别(sex)", "男/女"},
                    {3, "学号(code)", "必填（字符串）"},
                    {4, "所属学院(collegeName)", "必填；必须填写系统中已存在的学院名称"},
                    {5, "班级(className)", "必填；必须是系统中已存在的班级名称"},
            };
            for (Object[] one : helpRows) {
                Row rr = help.createRow(r++);
                rr.createCell(0).setCellValue(((Number) one[0]).intValue());
                rr.createCell(1).setCellValue(String.valueOf(one[1]));
                rr.createCell(2).setCellValue(String.valueOf(one[2]));
            }

            workbook.write(os);
            os.flush();
        }
    }

    /**
     * 解析Excel文件内容
     */
    private List<Student> parseExcelFile(MultipartFile file) throws IOException {
        List<Student> students = new ArrayList<>();

        // collegeName(归一化) -> collegeId
        java.util.Map<String, Integer> collegeNameToId = new java.util.HashMap<>();
        java.util.Map<String, Integer> rawMap = collegeService.getNameIdMap();
        if (rawMap != null) {
            for (java.util.Map.Entry<String, Integer> e : rawMap.entrySet()) {
                if (e == null || e.getKey() == null) continue;
                String key = normalizeText(e.getKey());
                if (key == null || key.isEmpty()) continue;
                collegeNameToId.putIfAbsent(key, e.getValue());
            }
        }

        // className(归一化) -> classId
        java.util.Map<String, Integer> classNameToId = new java.util.HashMap<>();
        java.util.Map<String, Integer> rawClassMap = clazzService.getNameIdMap();
        if (rawClassMap != null) {
            for (java.util.Map.Entry<String, Integer> e : rawClassMap.entrySet()) {
                if (e == null || e.getKey() == null) continue;
                String key = normalizeText(e.getKey());
                if (key == null || key.isEmpty()) continue;
                classNameToId.putIfAbsent(key, e.getValue());
            }
        }

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
                // 兼容两种模板：
                // 1) 旧模板：0 username,1 name,2 sex,3 code,4 score,5 collegeName,6 className
                // 2) 新模板：0 username,1 name,2 sex,3 code,4 collegeName,5 className
                String classCellMaybeOld = getStringValue(row.getCell(6));
                boolean isOldTemplate = classCellMaybeOld != null && !classCellMaybeOld.trim().isEmpty();

                if (isOldTemplate) {
                    student.setScore(getNumericValue(row.getCell(4)));
                } else {
                    // 新模板不再导入学分：给默认值，避免 batchInsert 中 score 为空造成 SQL 异常
                    student.setScore(0);
                }

                // Excel 第 6/5 列：所属学院（学院名字 -> collegeId）
                String collegeCell = isOldTemplate ? getStringValue(row.getCell(5)) : getStringValue(row.getCell(4));
                if (collegeCell == null || collegeCell.trim().isEmpty()) {
                    throw new RuntimeException("第" + (i + 1) + "行所属学院不能为空");
                }
                String collegeKey = normalizeText(collegeCell);
                Integer collegeId;
                // 兼容：如果用户仍然填了数字，就按 collegeId 直接解析
                if (collegeKey != null && collegeKey.matches("^\\d+$")) {
                    collegeId = Integer.parseInt(collegeKey);
                } else {
                    collegeId = collegeNameToId.get(collegeKey);
                }
                if (collegeId == null) {
                    throw new RuntimeException("第" + (i + 1) + "行所属学院不存在: " + collegeCell);
                }
                student.setCollegeId(collegeId);

                // Excel 第 7/6 列：班级（班级名称 -> classId）
                String classCell = isOldTemplate ? getStringValue(row.getCell(6)) : getStringValue(row.getCell(5));
                if (classCell == null || classCell.trim().isEmpty()) {
                    throw new RuntimeException("第" + (i + 1) + "行班级不能为空（请填班级名称）");
                }
                String classKey = normalizeText(classCell);
                Integer classId;
                // 兼容：若填了数字，则当作 classId
                if (classKey != null && classKey.matches("^\\d+$")) {
                    classId = Integer.parseInt(classKey);
                } else {
                    classId = classNameToId.get(classKey);
                }
                if (classId == null) {
                    throw new RuntimeException("第" + (i + 1) + "行班级不存在: " + classCell);
                }
                student.setClassId(classId);
                student.setRole("STUDENT");

                // 密码默认：学号后6位（若学号不足6位则用完整学号）
                String code = student.getCode();
                if (code != null && code.length() >= 6) {
                    student.setPassword(code.substring(code.length() - 6));
                } else if (code != null) {
                    student.setPassword(code);
                } else {
                    student.setPassword("123456"); // 学号为空时的兜底
                }

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

    /**
     * 文本归一化：去掉全角空格与所有空白字符，提升“学院名称匹配”的鲁棒性。
     */
    private String normalizeText(String s) {
        if (s == null) return null;
        return s.replace('\u3000', ' ').replaceAll("\\s+", "").trim();
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
