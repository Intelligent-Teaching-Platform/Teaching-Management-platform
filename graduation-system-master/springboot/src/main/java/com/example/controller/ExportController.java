package com.example.controller;

import com.example.entity.Score;
import com.example.entity.Clazz;
import com.example.entity.Student;
import com.example.entity.Work;
import com.example.mapper.ScoreMapper;
import com.example.mapper.ClazzMapper;
import com.example.mapper.StudentMapper;
import com.example.mapper.WorkMapper;
import com.example.service.ExperimentReportService;
import com.example.service.StudentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/export")
public class ExportController {

    @Resource
    private ScoreMapper scoreMapper;
    @Resource
    private WorkMapper workMapper;
    @Resource
    private StudentService studentService;
    @Resource
    private ExperimentReportService experimentReportService;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private ClazzMapper clazzMapper;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 课程资料一键导出（Excel 多 Sheet）
     *
     * Sheet：
     * - 试卷成绩
     * - 课后作业（lab=1）
     * - 实验作业（lab=2）
     * - 实验报告（lab=2，报告下载链接）
     * - 成绩汇总（按学生）
     */
    @GetMapping("/courseData")
    public void exportCourseData(@RequestParam Integer courseId,
                                 @RequestParam Integer teacherId,
                                 HttpServletResponse response) throws IOException {
        String fileName = "课程资料导出.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        // 1) 学生名单（该课程）
        List<Student> students = Optional.ofNullable(studentService.selectByCourseId(courseId)).orElseGet(List::of);

        // 2) 试卷成绩（该课程 + 该教师）
        Score scoreQuery = new Score();
        scoreQuery.setCourseId(courseId);
        scoreQuery.setTeacherId(teacherId);
        List<Score> scores = Optional.ofNullable(scoreMapper.selectAll(scoreQuery)).orElseGet(List::of);

        // 3) 作业（课后/实验：lab=1/2）
        List<Work> works = Optional.ofNullable(workMapper.selectByCourseAndTeacher(courseId, teacherId)).orElseGet(List::of);
        List<Work> lab1Works = works.stream().filter(w -> w.getLab() != null && w.getLab() == 1).collect(Collectors.toList());
        List<Work> lab2Works = works.stream().filter(w -> w.getLab() != null && w.getLab() == 2).collect(Collectors.toList());

        try (Workbook wb = new XSSFWorkbook()) {
            writeScoresSheet(wb.createSheet("试卷成绩"), scores);
            writeWorkSheet(wb.createSheet("课后作业"), lab1Works, false);
            writeWorkSheet(wb.createSheet("实验作业"), lab2Works, true);
            writeExperimentReportSheet(wb.createSheet("实验报告"), lab2Works);
            writeSummarySheet(wb.createSheet("成绩汇总"), students, scores, lab1Works, lab2Works);

            wb.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    /**
     * 导出某课程下某个实验作业（taskId）的所有学生实验报告，并打包为 zip。
     * 文件名：实验名称 + 学生学号 + 学生姓名.docx
     */
    @GetMapping("/experimentReportsZip")
    public void exportExperimentReportsZip(@RequestParam Integer courseId,
                                           @RequestParam Integer teacherId,
                                           @RequestParam Integer taskId,
                                           HttpServletResponse response) throws IOException {
        // 取该 taskId 下的实验作业（lab=2）
        List<Work> works = Optional.ofNullable(workMapper.selectByTaskIdForStageStats(taskId)).orElseGet(List::of);
        List<Work> lab2Works = works.stream()
                .filter(w -> w.getLab() != null && w.getLab() == 2)
                .filter(w -> Objects.equals(w.getCourseId(), courseId))
                .filter(w -> Objects.equals(w.getTeacherId(), teacherId))
                .collect(Collectors.toList());

        String fileName = "实验报告导出.zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Set<String> usedNames = new HashSet<>();
            String taskFolder = sanitizeFileName(lab2Works.isEmpty() ? "实验作业" : nvlStr(lab2Works.get(0).getName()));
            if (taskFolder.isEmpty()) taskFolder = "实验作业";

            // 逐个学生生成 docx 并写入 zip
            for (Work w : lab2Works) {
                byte[] docx = experimentReportService.generateReportDocx(w.getId());

                String expName = nvlStr(w.getName());
                String studentName = nvlStr(w.getStudentName());
                String studentCode = "";
                String className = "";
                if (w.getStudentId() != null) {
                    Student stu = studentMapper.selectById(w.getStudentId());
                    if (stu != null) {
                        if (stu.getCode() != null) studentCode = stu.getCode();
                        try {
                            Clazz clazz = clazzMapper.selectById(stu.getClassId());
                            if (clazz != null && clazz.getName() != null) className = clazz.getName();
                        } catch (Exception ignored) {
                        }
                    }
                }

                String baseFile = sanitizeFileName(expName + studentCode + studentName);
                if (baseFile.isEmpty()) baseFile = "实验报告";
                String dir = taskFolder + "/";
                String safeClazz = sanitizeFileName(className);
                if (!safeClazz.isEmpty()) dir = dir + safeClazz + "/";

                String entryName = uniqueName(dir + baseFile + ".docx", usedNames);

                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(docx);
                zos.closeEntry();
            }
            zos.finish();
            zos.flush();
        }
    }

    private void writeScoresSheet(Sheet sheet, List<Score> scores) {
        int r = 0;
        Row header = sheet.createRow(r++);
        String[] cols = {"试卷ID", "试卷名称", "学生ID", "学生姓名", "状态", "得分", "教师ID", "课程ID"};
        for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

        for (Score s : scores) {
            Row row = sheet.createRow(r++);
            int c = 0;
            row.createCell(c++).setCellValue(nvl(s.getPaperId()));
            row.createCell(c++).setCellValue(nvlStr(s.getName()));
            row.createCell(c++).setCellValue(nvl(s.getStudentId()));
            row.createCell(c++).setCellValue(nvlStr(s.getStudentName()));
            row.createCell(c++).setCellValue(nvlStr(s.getStatus()));
            Cell scoreCell = row.createCell(c++);
            if (s.getScore() == null) scoreCell.setCellValue("");
            else scoreCell.setCellValue(s.getScore());
            row.createCell(c++).setCellValue(nvl(s.getTeacherId()));
            row.createCell(c++).setCellValue(nvl(s.getCourseId()));
        }
        autosize(sheet, cols.length);
    }

    private void writeWorkSheet(Sheet sheet, List<Work> works, boolean isLab2) {
        int r = 0;
        Row header = sheet.createRow(r++);
        List<String> cols = new ArrayList<>(List.of(
                "作业ID", "作业名称", "学生ID", "学生姓名", "教师ID", "教师姓名",
                "状态", "分数", "最后提交或修改时间"
        ));
        if (!isLab2) {
            cols.addAll(List.of("作业内容", "提交内容", "文件"));
        } else {
            cols.addAll(List.of("实验环境(tip1)", "实验内容步骤(tip2)", "实验总结(tip3)", "阶段(submitPhase)"));
        }
        cols.addAll(List.of("修改意见", "教师评价"));

        for (int i = 0; i < cols.size(); i++) header.createCell(i).setCellValue(cols.get(i));

        for (Work w : works) {
            Row row = sheet.createRow(r++);
            int c = 0;
            row.createCell(c++).setCellValue(nvl(w.getId()));
            row.createCell(c++).setCellValue(nvlStr(w.getName()));
            row.createCell(c++).setCellValue(nvl(w.getStudentId()));
            row.createCell(c++).setCellValue(nvlStr(w.getStudentName()));
            row.createCell(c++).setCellValue(nvl(w.getTeacherId()));
            row.createCell(c++).setCellValue(nvlStr(w.getTeacherName()));
            row.createCell(c++).setCellValue(nvlStr(w.getState()));
            if (w.getScore() == null) row.createCell(c++).setCellValue("");
            else row.createCell(c++).setCellValue(w.getScore());
            row.createCell(c++).setCellValue(formatDT(w.getStudentLastSubmitTime()));

            if (!isLab2) {
                row.createCell(c++).setCellValue(nvlStr(w.getContent()));
                row.createCell(c++).setCellValue(nvlStr(w.getScontent()));
                row.createCell(c++).setCellValue(nvlStr(w.getFile()));
            } else {
                row.createCell(c++).setCellValue(nvlStr(w.getTip1()));
                row.createCell(c++).setCellValue(nvlStr(w.getTip2()));
                row.createCell(c++).setCellValue(nvlStr(w.getTip3()));
                row.createCell(c++).setCellValue(w.getSubmitPhase() == null ? "" : String.valueOf(w.getSubmitPhase()));
            }

            row.createCell(c++).setCellValue(nvlStr(w.getAmendment()));
            row.createCell(c++).setCellValue(nvlStr(w.getTeacherComment()));
        }
        autosize(sheet, cols.size());
    }

    private void writeExperimentReportSheet(Sheet sheet, List<Work> lab2Works) {
        int r = 0;
        Row header = sheet.createRow(r++);
        String[] cols = {"作业ID", "实验名称", "学生ID", "学生姓名", "报告下载链接(相对)", "阶段(submitPhase)", "最后提交或修改时间"};
        for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

        for (Work w : lab2Works) {
            Row row = sheet.createRow(r++);
            int c = 0;
            row.createCell(c++).setCellValue(nvl(w.getId()));
            row.createCell(c++).setCellValue(nvlStr(w.getName()));
            row.createCell(c++).setCellValue(nvl(w.getStudentId()));
            row.createCell(c++).setCellValue(nvlStr(w.getStudentName()));
            row.createCell(c++).setCellValue("/work/report/" + nvl(w.getId()));
            row.createCell(c++).setCellValue(w.getSubmitPhase() == null ? "" : String.valueOf(w.getSubmitPhase()));
            row.createCell(c++).setCellValue(formatDT(w.getStudentLastSubmitTime()));
        }
        autosize(sheet, cols.length);
    }

    private void writeSummarySheet(Sheet sheet,
                                   List<Student> students,
                                   List<Score> scores,
                                   List<Work> lab1Works,
                                   List<Work> lab2Works) {
        int r = 0;
        Row header = sheet.createRow(r++);
        String[] cols = {
                "学生ID", "学生姓名",
                "试卷次数", "试卷平均分",
                "课后作业次数", "课后作业平均分",
                "实验作业次数", "实验作业平均分"
        };
        for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

        Map<Integer, List<Score>> scoreByStu = scores.stream()
                .filter(s -> s.getStudentId() != null)
                .collect(Collectors.groupingBy(Score::getStudentId));

        Map<Integer, List<Work>> lab1ByStu = lab1Works.stream()
                .filter(w -> w.getStudentId() != null)
                .collect(Collectors.groupingBy(Work::getStudentId));
        Map<Integer, List<Work>> lab2ByStu = lab2Works.stream()
                .filter(w -> w.getStudentId() != null)
                .collect(Collectors.groupingBy(Work::getStudentId));

        for (Student stu : students) {
            Integer sid = stu.getId();
            Row row = sheet.createRow(r++);
            int c = 0;
            row.createCell(c++).setCellValue(nvl(sid));
            row.createCell(c++).setCellValue(nvlStr(stu.getName()));

            List<Score> sList = scoreByStu.getOrDefault(sid, List.of());
            row.createCell(c++).setCellValue(sList.size());
            row.createCell(c++).setCellValue(avgInt(sList.stream().map(Score::getScore).collect(Collectors.toList())));

            List<Work> w1 = lab1ByStu.getOrDefault(sid, List.of());
            row.createCell(c++).setCellValue(w1.size());
            row.createCell(c++).setCellValue(avgInt(w1.stream().map(Work::getScore).collect(Collectors.toList())));

            List<Work> w2 = lab2ByStu.getOrDefault(sid, List.of());
            row.createCell(c++).setCellValue(w2.size());
            row.createCell(c++).setCellValue(avgInt(w2.stream().map(Work::getScore).collect(Collectors.toList())));
        }
        autosize(sheet, cols.length);
    }

    private static String formatDT(LocalDateTime dt) {
        return dt == null ? "" : DT.format(dt);
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private static String nvlStr(String s) {
        return s == null ? "" : s;
    }

    private static String sanitizeFileName(String name) {
        if (name == null) return "";
        // Windows/zip entry 安全：去掉非法字符，并压缩空白
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        // 避免太长
        if (cleaned.length() > 120) {
            cleaned = cleaned.substring(0, 120);
        }
        return cleaned;
    }

    private static String uniqueName(String fullName, Set<String> used) {
        if (used.add(fullName)) return fullName;
        int dot = fullName.lastIndexOf('.');
        String base = dot > 0 ? fullName.substring(0, dot) : fullName;
        String ext = dot > 0 ? fullName.substring(dot) : "";
        for (int i = 2; i < 10000; i++) {
            String next = base + "(" + i + ")" + ext;
            if (used.add(next)) return next;
        }
        // 极端情况兜底
        String fallback = base + "(" + System.currentTimeMillis() + ")" + ext;
        used.add(fallback);
        return fallback;
    }

    private static void autosize(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            try {
                sheet.autoSizeColumn(i);
            } catch (Exception ignored) {
            }
        }
    }

    private static double avgDouble(List<Double> vals) {
        List<Double> list = vals == null ? List.of() : vals.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (list.isEmpty()) return 0d;
        double sum = 0d;
        for (Double v : list) sum += v;
        return Math.round((sum / list.size()) * 10d) / 10d;
    }

    private static double avgInt(List<Integer> vals) {
        List<Integer> list = vals == null ? List.of() : vals.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (list.isEmpty()) return 0d;
        double sum = 0d;
        for (Integer v : list) sum += v;
        return Math.round((sum / list.size()) * 10d) / 10d;
    }
}

