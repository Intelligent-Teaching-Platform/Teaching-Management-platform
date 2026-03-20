package com.example.controller;

import com.example.common.Result;
import com.example.entity.Course;
import com.example.service.CourseService;
import com.example.service.TeacherService;
import com.example.service.CollegeService;
import com.example.service.ClazzService;
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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 课程信息模块前端请求接口入口
 */
@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    @Resource
    private TeacherService teacherService;

    @Resource
    private CollegeService collegeService;

    @Resource
    private ClazzService clazzService;

    @GetMapping("selectByName")
    public Result selectByName(@RequestParam String name) {
        Course course = courseService.selectByName(name);
        return Result.success(course);
    }

    /**
     * 根据教师姓名查询课程
     */
    @GetMapping("/selectByTeacherName")
    public Result selectByTeacherName(@RequestParam String teacherName) {
        List<Course> courses = courseService.selectByTeacherName(teacherName);
        return Result.success(courses);
    }

    /**
     * 增加
     */
    @PostMapping("/add")
    public Result add(@RequestBody Course course) {
        courseService.add(course);
        return Result.success();
    }

    /**
     * 更新
     */
    @PutMapping("/update")
    public Result update(@RequestBody Course course) {
        courseService.updateByID(course);
        return Result.success();
    }

    /**
     * 分页查询接口
     */
    @GetMapping("/selectPage")
    public Result selectPage(Course course,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "5") Integer pageSize) {
        PageInfo<Course> pageInfo = courseService.selectPage(course, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        courseService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Course> list = courseService.selectAll();
        return Result.success(list);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result importCourse(@RequestParam("file") MultipartFile file) {
        try {
            // 基础校验：判断文件是否存在或者内容为空
            if (file == null || file.isEmpty()) {
                return Result.error("请选择上传文件");
            }

            // 进一步检验文件是否为excel类型文件
            String contentType = file.getContentType();
            if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
                return Result.error("仅支持.xlsx格式的Excel文件");
            }

            // 解析上传的excel文件
            List<Course> courses = parseExcelFile(file);

            // 批量插入
            courseService.batchInsert(courses);
            return Result.success("成功导入" + courses.size() + "条数据");
        } catch (Exception e) {
            return Result.error("导入失败，失败原因：" + e.getMessage());
        }
    }

    /**
     * 下载课程批量导入模板（与 /course/import 固定列顺序严格对齐）
     */
    @GetMapping("/importTemplate")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        String fileName = "课程批量导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (Workbook workbook = new XSSFWorkbook(); ServletOutputStream os = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("导入模板");
            Sheet help = workbook.createSheet("说明");

            // 表头（固定列顺序：0..10）
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("课程名称(name)");
            header.createCell(1).setCellValue("课程介绍(content)");
            header.createCell(2).setCellValue("课程学分(score，数字)");
            header.createCell(3).setCellValue("授课教师(teacherName)");
            header.createCell(4).setCellValue("开班人数(num，数字)");
            header.createCell(5).setCellValue("上课时间(time)");
            header.createCell(6).setCellValue("上课地点(location)");
            header.createCell(7).setCellValue("班级(className 或 classId)");
            header.createCell(8).setCellValue("所属学院(collegeName)");
            header.createCell(9).setCellValue("已选人数(alreadyNum，可留空)");
            header.createCell(10).setCellValue("学期(term)");

            // 示例行（可删除后填写）
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("数据结构");
            example.createCell(1).setCellValue("基础数据结构与算法");
            example.createCell(2).setCellValue(3);
            example.createCell(3).setCellValue("张三");
            example.createCell(4).setCellValue(50);
            example.createCell(5).setCellValue("周一 1-2节");
            example.createCell(6).setCellValue("A101");
            example.createCell(7).setCellValue("计科2201");
            example.createCell(8).setCellValue("计算机学院");
            example.createCell(9).setCellValue(0);
            example.createCell(10).setCellValue("2025-2026-1");

            for (int i = 0; i <= 10; i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(Math.max(w + 1024, 4096), 20000));
            }

            // ===== 说明 Sheet =====
            int r = 0;
            Row t0 = help.createRow(r++);
            t0.createCell(0).setCellValue("课程批量导入说明（请勿改动“导入模板”Sheet 的列顺序）");

            r++; // 空一行
            Row t1 = help.createRow(r++);
            t1.createCell(0).setCellValue("列顺序与含义（导入模板 Sheet，按 0..10 列固定解析）");
            Row h = help.createRow(r++);
            h.createCell(0).setCellValue("列序号");
            h.createCell(1).setCellValue("表头");
            h.createCell(2).setCellValue("要求/说明");

            Object[][] rows = new Object[][]{
                    {0, "课程名称(name)", "必填"},
                    {1, "课程介绍(content)", "可选"},
                    {2, "课程学分(score)", "必填；数字（如 3）"},
                    {3, "授课教师(teacherName)", "必填；必须是系统中已存在的教师姓名，否则导入报错"},
                    {4, "开班人数(num)", "必填；数字"},
                    {5, "上课时间(time)", "可选；如 “周一 1-2节”"},
                    {6, "上课地点(location)", "可选；如 “A101”"},
                    {7, "班级(className 或 classId)", "推荐填班级名称；也兼容纯数字班级ID"},
                    {8, "所属学院(collegeName)", "必填；必须是系统中已存在的学院名称，否则导入报错"},
                    {9, "已选人数(alreadyNum)", "可留空或填 0；导入后系统会重置为 0"},
                    {10, "学期(term)", "可选；如 “2025-2026-1”"},
            };

            for (Object[] one : rows) {
                Row rr = help.createRow(r++);
                rr.createCell(0).setCellValue(((Number) one[0]).intValue());
                rr.createCell(1).setCellValue(String.valueOf(one[1]));
                rr.createCell(2).setCellValue(String.valueOf(one[2]));
            }

            r++;
            Row t2 = help.createRow(r++);
            t2.createCell(0).setCellValue("常见错误与解决");
            Row e1 = help.createRow(r++);
            e1.createCell(0).setCellValue("1) “教师不存在”：请确认第 4 列填写的是教师姓名（与系统教师管理中的姓名一致）");
            Row e2 = help.createRow(r++);
            e2.createCell(0).setCellValue("2) “学院不存在”：请确认第 9 列填写的是学院名称（与学院信息中的名称一致）");
            Row e3 = help.createRow(r++);
            e3.createCell(0).setCellValue("3) “班级不存在”：请确认第 8 列填写班级名称，或填写纯数字班级ID");

            for (int i = 0; i <= 2; i++) {
                help.autoSizeColumn(i);
                int w = help.getColumnWidth(i);
                help.setColumnWidth(i, Math.min(Math.max(w + 1024, 4096), 26000));
            }

            workbook.write(os);
            os.flush();
        }
    }

    /**
     * 具体解析excel文件
     */
    private List<Course> parseExcelFile(MultipartFile file) throws IOException {
        // 1. 加载名称-ID映射
        Map<String, Integer> teacherNameToId = teacherService.getNameIdMap();
        Map<String, Integer> collegeNameToId = collegeService.getNameIdMap();
        Map<String, Integer> classNameToId = clazzService.getNameIdMap();

        List<Course> courses = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 2. 读取Excel中的名称
                String teacherName = getStringValue(row.getCell(3)); // 第4列是教师名称
                String collegeName = getStringValue(row.getCell(8)); // 第9列是学院名称

                // 3. 验证名称是否存在
                Integer teacherId = teacherNameToId.get(teacherName);
                if (teacherId == null) {
                    throw new RuntimeException("第" + (i + 1) + "行教师不存在: " + teacherName);
                }
                Integer collegeId = collegeNameToId.get(collegeName);
                if (collegeId == null) {
                    throw new RuntimeException("第" + (i + 1) + "行学院不存在: " + collegeName);
                }

                // 4. 构建Course对象
                Course course = new Course();
                // 设置从名称转换来的ID
                course.setTeacherId(teacherId);
                course.setCollegeId(collegeId);

                // 设置其他字段（注意调整列索引）
                course.setName(getStringValue(row.getCell(0)));
                course.setContent(getStringValue(row.getCell(1)));
                course.setScore(getNumericValue(row.getCell(2)));
                course.setNum(getNumericValue(row.getCell(4)));
                course.setTime(getStringValue(row.getCell(5)));
                course.setLocation(getStringValue(row.getCell(6)));
                // Excel 第 8 列：可能是班级ID（数字），也可能是班级名称（字符串）
                String classCell = getStringValue(row.getCell(7));
                String classKey = normalizeText(classCell);
                Integer classId = classNameToId.get(classKey);
                if (classId == null) {
                    // 如果 Excel 单元格其实是数字ID，就兼容解析；否则直接报错，避免写入错误的 0 导致 className 为空
                    if (classKey != null && classKey.matches("^\\d+$")) {
                        classId = Integer.parseInt(classKey);
                    } else if (classKey != null && !classKey.isEmpty()) {
                        throw new RuntimeException("第" + (i + 1) + "行班级不存在: " + classCell);
                    }
                }
                course.setClassId(classId);
                course.setAlreadyNum(getNumericValue(row.getCell(9)));
                course.setTerm(getStringValue(row.getCell(10)));

                course.setAlreadyNum(0); // 初始已选人数设为0

                courses.add(course);
            }
        }
        return courses;
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
     * 文本归一化：去掉全角空格与所有空白字符，提升“班级名称匹配”的鲁棒性。
     */
    private String normalizeText(String s) {
        if (s == null) return null;
        return s.replace('\u3000', ' ').replaceAll("\\s+", "").trim();
    }
}