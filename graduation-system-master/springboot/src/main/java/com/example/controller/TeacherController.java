package com.example.controller;

import com.example.common.Result;
import com.example.entity.Student;
import com.example.entity.Teacher;
import com.example.entity.Speciality;
import com.example.service.TeacherService;
import com.example.service.SpecialityService;
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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 教师模块前端请求接口入口
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {
    @Resource
    private TeacherService teacherService;

    @Resource
    private SpecialityService specialityService;

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
     * 下载教师批量导入模板（与 teacher/import 固定列索引严格对齐）
     */
    @GetMapping("/importTemplate")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        String fileName = "教师批量导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );

        try (Workbook workbook = new XSSFWorkbook(); ServletOutputStream os = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("导入模板");
            Sheet help = workbook.createSheet("说明");

            // 表头（固定列索引：0..7；解析时用到 0,1,2,3,4,5,7）
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("用户名(username)");
            header.createCell(1).setCellValue("密码(password)");
            header.createCell(2).setCellValue("姓名(name)");
            header.createCell(3).setCellValue("性别(sex)");
            header.createCell(4).setCellValue("职称(title)");
            header.createCell(5).setCellValue("所属学院(collegeName，学院名字)");
            header.createCell(6).setCellValue("预留（不参与导入）");
            header.createCell(7).setCellValue("头像(avatar，可留空)");

            // 示例行（第1行 i=1；导入时从第2行开始解析）
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("teacher01");
            example.createCell(1).setCellValue("123456");
            example.createCell(2).setCellValue("张三");
            example.createCell(3).setCellValue("男");
            example.createCell(4).setCellValue("讲师");
            example.createCell(5).setCellValue("计算机学院");
            example.createCell(6).setCellValue("");
            example.createCell(7).setCellValue("");

            // 说明
            int r = 0;
            Row h0 = help.createRow(r++);
            h0.createCell(0).setCellValue("教师批量导入说明（请勿改动“导入模板”列顺序）");
            r++;
            Row h1 = help.createRow(r++);
            h1.createCell(0).setCellValue("列索引 -> 字段");
            Object[][] helpRows = new Object[][]{
                    {0, "用户名(username)", "必填"},
                    {1, "密码(password)", "必填（未填时可手动补）"},
                    {2, "姓名(name)", "必填"},
                    {3, "性别(sex)", "男/女"},
                    {4, "职称(title)", "讲师/教授/副教授（与前端下拉一致）"},
                    {5, "所属学院(collegeName)", "必填；填写系统中已存在的学院名称（与学院管理中的名称一致）"},
                    {7, "头像(avatar)", "可留空；支持存储路径或 URL 字符串（与系统头像展示兼容）"},
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
private List<Teacher> parseExcelFile(MultipartFile file) throws IOException {
    List<Teacher> teachers = new ArrayList<>();

    // collegeName -> specialityId（取同学院的第一条 speciality 作为兜底）
    Map<String, Integer> collegeNameToSpecialityId = new HashMap<>();
    List<Speciality> specialityList = specialityService.selectAll();
    if (specialityList != null) {
        for (Speciality s : specialityList) {
            if (s == null || s.getId() == null || s.getCollegeName() == null) continue;
            String key = normalizeText(s.getCollegeName());
            if (key == null || key.isEmpty()) continue;
            collegeNameToSpecialityId.putIfAbsent(key, s.getId());
        }
    }

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
            // Excel 第 6 列：所属学院（学院名字 -> 映射到 specialityId）
            String collegeCell = getStringValue(row.getCell(5));
            if (collegeCell == null || collegeCell.trim().isEmpty()) {
                throw new RuntimeException("第" + (i + 1) + "行所属学院不能为空");
            }

            String collegeKey = normalizeText(collegeCell);
            Integer specialityId;
            // 兼容：如果用户仍然填了数字，就按 specialityId 直接解析
            if (collegeKey != null && collegeKey.matches("^\\d+$")) {
                specialityId = Integer.parseInt(collegeKey);
            } else {
                specialityId = collegeNameToSpecialityId.get(collegeKey);
            }

            if (specialityId == null) {
                throw new RuntimeException("第" + (i + 1) + "行所属学院不存在: " + collegeCell);
            }
            teacher.setSpecialityId(specialityId);
            teacher.setAvatar(getStringValue(row.getCell(7)));
            teacher.setRole("TEACHER");


            teachers.add(teacher);
        }
    }
    return teachers;
}

/**
 * 文本归一化：去掉全角空格与所有空白字符，提升“学院名称匹配”的鲁棒性。
 */
private String normalizeText(String s) {
    if (s == null) return null;
    return s.replace('\u3000', ' ').replaceAll("\\s+", "").trim();
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





