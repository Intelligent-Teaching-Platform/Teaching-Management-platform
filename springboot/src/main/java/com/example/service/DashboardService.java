package com.example.service;

import com.example.mapper.DashboardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Resource
    private WorkService workService;

    public Map<String, Object> mainLeft1() {
        Map<String, Object> res = new HashMap<>();
        res.put("courseCount", dashboardMapper.countCourses());
        res.put("teacherCount", dashboardMapper.countTeachers());
        res.put("studentCount", dashboardMapper.countStudents());
        res.put("clazzCount", dashboardMapper.countClazz());
        return res;
    }

    public List<Map<String, Object>> mainLeft2() {
        return dashboardMapper.workStateStatsAll();
    }

    public List<Map<String, Object>> mainLeft3() {
        return dashboardMapper.topStudentCompletionAll();
    }

    public List<Map<String, Object>> mainMid1() {
        return dashboardMapper.courseCountByTermAll();
    }

    public List<Map<String, Object>> mainMid2() {
        return dashboardMapper.latestCourseCards();
    }

    public Map<String, Object> mainMid3(Integer courseId) {
        Integer resolvedCourseId = courseId;
        if (resolvedCourseId == null) {
            resolvedCourseId = dashboardMapper.latestCourseId();
        }
        if (resolvedCourseId == null) {
            return Map.of(
                    "courseId", null,
                    "indicators", List.of("签到", "考试", "作业"),
                    "series", List.of()
            );
        }
        Map<String, Object> courseInfo = dashboardMapper.courseTeacherAndClazz(resolvedCourseId);
        Integer teacherId = asInt(courseInfo.get("teacherId"));
        Integer classId = asInt(courseInfo.get("classId"));
        return buildRadar(resolvedCourseId, teacherId, classId);
    }

    public List<Map<String, Object>> mainRight1() {
        return dashboardMapper.teacherTitleStats();
    }

    public Map<String, Object> mainRight2() {
        // 全局低完成率学生统计（按所有 work 记录汇总）
        List<Map<String, Object>> topList = dashboardMapper.topStudentCompletionAll();
        long lowCount = topList.stream()
                .filter(m -> asDouble(m.get("completionRate")) < 80d)
                .count();
        Map<String, Object> res = new HashMap<>();
        res.put("threshold", 80);
        res.put("lowRateCountInTop10", lowCount);
        res.put("top10", topList);
        return res;
    }

    public List<Map<String, Object>> mainRight3() {
        return dashboardMapper.recentWorkList();
    }

    // ---------------- teacher screen ----------------
    public Map<String, Object> screenLeft1(Integer teacherId) {
        Map<String, Object> res = new HashMap<>();
        res.put("courseCount", dashboardMapper.teacherCourseCount(teacherId));
        res.put("studentCount", dashboardMapper.teacherStudentDistinctCount(teacherId));
        res.put("clazzCount", dashboardMapper.teacherClazzDistinctCount(teacherId));
        return res;
    }

    public List<Map<String, Object>> screenLeft2(Integer teacherId, Integer courseId) {
        Map<String, Object> c = dashboardMapper.courseTeacherAndClazz(courseId);
        Integer classId = asInt(c.get("classId"));

        Map<String, Object> stats = dashboardMapper.workSubmitStateStatsByTeacherAndCourse(teacherId, courseId, classId);
        if (stats == null) stats = Map.of();

        int afterUnsubmitted = asInt(stats.get("after_unsubmitted")) == null ? 0 : asInt(stats.get("after_unsubmitted"));
        int afterSubmitted = asInt(stats.get("after_submitted")) == null ? 0 : asInt(stats.get("after_submitted"));
        int expUnsubmitted = asInt(stats.get("exp_unsubmitted")) == null ? 0 : asInt(stats.get("exp_unsubmitted"));
        int expSubmitted = asInt(stats.get("exp_submitted")) == null ? 0 : asInt(stats.get("exp_submitted"));

        return List.of(
                Map.of("name", "课后任务", "unsubmitted", afterUnsubmitted, "submitted", afterSubmitted),
                Map.of("name", "实验任务", "unsubmitted", expUnsubmitted, "submitted", expSubmitted)
        );
    }

    public List<Map<String, Object>> screenLeft3(Integer teacherId, Integer courseId) {
        // 严格按 courseId 统计：避免在同一班级下不同课程串数据
        return dashboardMapper.topStudentCompletionByTeacherAndCourse(teacherId, courseId);
    }

    public List<Map<String, Object>> screenMid1(Integer teacherId) {
        // 用卡片展示课程上课时间/地点（教师当前授课全部课程）
        return dashboardMapper.teacherCourseCards(teacherId);
    }

    public List<Map<String, Object>> screenMid2(Integer teacherId) {
        return dashboardMapper.teacherCourseCards(teacherId);
    }

    public Map<String, Object> screenMid3(Integer teacherId, Integer courseId) {
        Map<String, Object> courseInfo = dashboardMapper.courseTeacherAndClazz(courseId);
        Integer classId = asInt(courseInfo.get("classId"));
        return buildRadar(courseId, teacherId, classId);
    }

    public Map<String, Object> screenRight1(Integer courseId) {
        return dashboardMapper.signInSignedUnsignedByCourse(courseId);
    }

    public Map<String, Object> screenRight2(Integer teacherId, Integer courseId) {
        // 改为：实验作业相似度预警（按右上角下拉课程）
        return workService.experimentSimilarityAlertByTeacherAndCourse(teacherId, courseId);
    }

    public List<Map<String, Object>> screenRight3(Integer teacherId, Integer courseId) {
        // 将“近期考试/测验成绩”替换为“实验任务各阶段学生人数”
        return workService.experimentStageCountsByTeacherAndCourse(teacherId, courseId);
    }

    // ---------------- shared ----------------
    private Map<String, Object> buildRadar(Integer courseId, Integer teacherId, Integer classId) {
        List<Map<String, Object>> students = dashboardMapper.studentsByCourse(courseId);
        Integer totalSignIns = dashboardMapper.signInTotalByCourse(courseId);

        Map<Integer, Integer> attendMap = dashboardMapper.signInAttendByCourse(courseId).stream()
                .filter(m -> m.get("studentId") != null)
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("studentId")).intValue(),
                        m -> ((Number) m.get("attendCount")).intValue(),
                        (a, b) -> a
                ));

        Map<Integer, Double> workAvgMap = dashboardMapper.workAvgScoreByTeacherAndClass(teacherId, classId).stream()
                .filter(m -> m.get("studentId") != null)
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("studentId")).intValue(),
                        m -> asDouble(m.get("avgScore")),
                        (a, b) -> a
                ));

        Map<Integer, Double> examAvgMap = dashboardMapper.examAvgScoreByCourse(courseId).stream()
                .filter(m -> m.get("studentId") != null)
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("studentId")).intValue(),
                        m -> asDouble(m.get("avgScore")),
                        (a, b) -> a
                ));

        List<Map<String, Object>> series = new ArrayList<>();
        for (Map<String, Object> s : students) {
            Integer sid = ((Number) s.get("studentId")).intValue();
            // 签到分：满分10，成功签到次数/教师发布签到次数 * 10
            double signPoint = (totalSignIns == null || totalSignIns == 0)
                    ? 0d
                    : 10d * (attendMap.getOrDefault(sid, 0) * 1.0d / totalSignIns);
            // 考试分：满分40，所有考试平均得分/100 * 40
            double examPoint = 40d * (examAvgMap.getOrDefault(sid, 0d) / 100d);
            // 作业分：满分50，所有作业平均得分/100 * 50
            double workPoint = 50d * (workAvgMap.getOrDefault(sid, 0d) / 100d);

            double total = signPoint + examPoint + workPoint;
            Map<String, Object> item = new HashMap<>();
            item.put("studentId", sid);
            item.put("studentName", s.get("studentName"));
            item.put("code", s.get("code"));
            // 雷达图显示归一化到10分制用于可视化对比
            item.put("values", List.of(
                round1(signPoint),           // 签到满分10
                round1(examPoint * 10 / 40), // 考试满分40，归一化到10
                round1(workPoint * 10 / 50)  // 作业满分50，归一化到10
            ));
            // 实际得分
            item.put("signScore", round1(signPoint));
            item.put("examScore", round1(examPoint));
            item.put("workScore", round1(workPoint));
            item.put("total", round1(total));
            series.add(item);
        }

        series.sort((a, b) -> Double.compare(asDouble(b.get("total")), asDouble(a.get("total"))));
        if (series.size() > 6) {
            series = series.subList(0, 6);
        }

        return Map.of(
                "courseId", courseId,
                "indicators", List.of("签到(10分)", "考试(40分)", "作业(50分)"),
                "series", series
        );
    }

    private static double asDouble(Object o) {
        if (o == null) return 0d;
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return 0d;
        }
    }

    private static double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}

