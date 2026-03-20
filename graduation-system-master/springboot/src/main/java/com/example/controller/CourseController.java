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

import java.io.IOException;
import java.io.InputStream;
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