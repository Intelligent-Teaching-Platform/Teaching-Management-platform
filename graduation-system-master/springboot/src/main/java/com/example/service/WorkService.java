package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Work;
import com.example.exception.CustomException;
import com.example.mapper.WorkMapper;
import com.example.utils.SimilarityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程信息业务层处理
 */
@Service
public class WorkService {
    @Resource
    private WorkMapper workMapper;

    public void add(Work work) {
        // 仅“课后任务/作业”(lab=1)做查重，避免覆盖实验任务(lab=2)的 tip1/2/3 字段含义
        if (work.getLab() != null && work.getLab() == 1
                && ObjectUtil.isNotEmpty(work.getScontent())
                && work.getScontent().trim().length() >= 10) {
            calculateAndSetSimilarity(work);
        }
        workMapper.insert(work);
    }

    public PageInfo<Work> selectPage(Work work, Integer pageNum, Integer pageSize, Integer courseId) {
        List<Work> list;
        PageHelper.startPage(pageNum, pageSize);
        if (courseId != null) {
            // 课程详情页：按课程过滤
            if (ObjectUtil.isNotEmpty(work.getStudentId())) {
                list = workMapper.selectByCourseAndStudent(courseId, work.getStudentId());
            } else if (ObjectUtil.isNotEmpty(work.getTeacherId())) {
                list = workMapper.selectByCourseAndTeacher(courseId, work.getTeacherId());
            } else {
                list = workMapper.selectByCourse(courseId);
            }
        } else {
            if(ObjectUtil.isNotEmpty(work.getStudentId())) {
                // 学生登录进行分页查询
                if(ObjectUtil.isNotEmpty(work.getName())) {
                    list = workMapper.selectByNameAndStudentId(work.getName(), work.getStudentId());
                } else {
                    list = workMapper.selectAllByStudentId(work.getStudentId());
                }
            } else if(ObjectUtil.isNotEmpty(work.getTeacherId())){
                // 老师登录进行分页查询
                if(ObjectUtil.isNotEmpty(work.getName())) {
                    list = workMapper.selectByNameAndTeacherId(work.getName(), work.getTeacherId());
                } else {
                    list = workMapper.selectAllByTeacherId(work.getTeacherId());
                }
            } else {
                // 管理员登录进行分页查询
                if(ObjectUtil.isNotEmpty(work.getName())) {
                    list = workMapper.selectByName(work.getName());
                } else {
                    list = workMapper.selectAll();
                }
            }
        }

        // 只保留 lab 字段值为 2 的记录
        list = list.stream()
                .filter(w -> w.getLab() != null && w.getLab() == 2)
                .collect(Collectors.toList());

        return PageInfo.of(list);
    }
    public PageInfo<Work> selectPageone(Work work, Integer pageNum, Integer pageSize, Integer courseId) {
        List<Work> list;
        PageHelper.startPage(pageNum, pageSize);
        if (courseId != null) {
            if (ObjectUtil.isNotEmpty(work.getStudentId())) {
                list = workMapper.selectByCourseAndStudent(courseId, work.getStudentId());
            } else if (ObjectUtil.isNotEmpty(work.getTeacherId())) {
                list = workMapper.selectByCourseAndTeacher(courseId, work.getTeacherId());
            } else {
                list = workMapper.selectByCourse(courseId);
            }
        } else {
            if(ObjectUtil.isNotEmpty(work.getStudentId())) {
                // 学生登录进行分页查询
                if(ObjectUtil.isNotEmpty(work.getName())) {
                    list = workMapper.selectByNameAndStudentId(work.getName(), work.getStudentId());
                } else {
                    list = workMapper.selectAllByStudentId(work.getStudentId());
                }
            } else if(ObjectUtil.isNotEmpty(work.getTeacherId())){
                // 老师登录进行分页查询
                if(ObjectUtil.isNotEmpty(work.getName())) {
                    list = workMapper.selectByNameAndTeacherId(work.getName(), work.getTeacherId());
                } else {
                    list = workMapper.selectAllByTeacherId(work.getTeacherId());
                }
            } else {
                // 管理员登录进行分页查询
                if(ObjectUtil.isNotEmpty(work.getName())) {
                    list = workMapper.selectByName(work.getName());
                } else {
                    list = workMapper.selectAll();
                }
            }
        }

        // 学生：本页需同时看到课后作业(lab=1)与实验作业(lab=2)，且查询条件已按 studentId 限定为本学生
        // 教师/管理员：仍仅展示课后作业(lab=1)
        if (ObjectUtil.isNotEmpty(work.getStudentId())) {
            list = list.stream()
                    .filter(w -> w.getLab() != null && (w.getLab() == 1 || w.getLab() == 2))
                    .collect(Collectors.toList());
        } else {
            list = list.stream()
                    .filter(w -> w.getLab() != null && w.getLab() == 1)
                    .collect(Collectors.toList());
        }
        return PageInfo.of(list);
    }


    public void updateByID(Work work) {

        workMapper.updateById(work);
    }
    public void updateByIDone(Work work) {
        // 仅“课后任务/作业”(lab=1)做查重，避免覆盖实验任务(lab=2)的 tip1/2/3 字段含义
        if (work.getLab() != null && work.getLab() == 1
                && ObjectUtil.isNotEmpty(work.getScontent())
                && work.getScontent().trim().length() >= 10) {
            calculateAndSetSimilarity(work);
        }
        workMapper.updateByIdOne(work);
    }

    /**
     * 计算并设置相似度
     * @param work 当前作业
     */
    private void calculateAndSetSimilarity(Work work) {
        try {
            // 获取所有其他作业的提交内容（用于相似度比较）
            List<Work> allWorks = workMapper.selectAll();
            
            // 转换为Map列表，只包含id和scontent
            List<Map<String, Object>> otherTexts = allWorks.stream()
                    .filter(w -> w.getScontent() != null && !w.getScontent().trim().isEmpty())
                    .map(w -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", w.getId());
                        map.put("scontent", w.getScontent());
                        return map;
                    })
                    .collect(Collectors.toList());

            // 计算最大相似度
            double maxSimilarity = SimilarityUtil.calculateMaxSimilarity(
                    work.getScontent(),
                    otherTexts,
                    work.getId()  // 排除当前作业自身
            );

            // 将相似度保存为百分比字符串（保留2位小数）
            work.setTip1(String.format("%.2f%%", maxSimilarity * 100));
        } catch (Exception e) {
            // 如果计算失败，不影响主流程，只记录日志
            System.err.println("计算相似度失败: " + e.getMessage());
            work.setTip1("0.00%");
        }
    }

    public void deleteById(Integer id) {
        workMapper.deleteById(id);
    }

    /**
     * 教师审核实验作业时的查重提示：返回“最大相似度 + 对应学生”
     * 规则：优先在同一 task_id 下比较；若 task_id 为空，则按(teacher_id + name + lab=2)兜底。
     */
    public Map<String, Object> experimentSimilarityHint(Integer workId) {
        Work current = workMapper.selectById(workId);
        if (current == null) {
            throw new IllegalArgumentException("作业不存在");
        }
        if (current.getLab() == null || current.getLab() != 2) {
            throw new IllegalArgumentException("仅支持实验作业查重提示");
        }

        // 为避免“实验目的/要求(scontent)”被老师统一预填导致相似度失真：
        // 实验相似度基于学生分段提交的“实验内容与步骤(tip2)”+“实验总结与心得(tip3)”
        // 相似度至少需要阶段二内容(tip2)，tip3 仅在存在时拼接。
        String currentText = current.getTip2();

        List<Work> others;
        if (ObjectUtil.isNotEmpty(current.getTaskId())) {
            others = workMapper.selectOthersByTaskIdForSimilarity(current.getTaskId(), current.getId());
        } else {
            others = workMapper.selectOthersByTeacherAndNameForSimilarity(current.getTeacherId(), current.getName(), current.getId());
        }

        if (!hasText(currentText)) {
            Map<String, Object> res = new HashMap<>();
            res.put("maxSimilarity", 0.0);
            res.put("maxSimilarityText", null);
            res.put("matchedStudentId", null);
            res.put("matchedStudentName", null);
            res.put("comparedCount", 0);
            return res;
        }

        if (hasText(current.getTip3())) {
            currentText = currentText + "\n" + current.getTip3();
        }

        double best = 0.0;
        Integer bestStudentId = null;
        String bestStudentName = null;
        Integer compared = 0;

        for (Work w : others) {
            if (!hasText(w.getTip2())) continue; // 至少需要阶段二内容，才具备可比性
            String otherText = w.getTip2();
            if (hasText(w.getTip3())) {
                otherText = otherText + "\n" + w.getTip3();
            }
            compared++;
            double sim = SimilarityUtil.calculateCosineSimilarity(currentText, otherText);
            if (sim > best) {
                best = sim;
                bestStudentId = w.getStudentId();
                bestStudentName = w.getStudentName();
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("maxSimilarity", best);
        // 若没有可对比文本，则返回 null，让前端显示“未分析”
        res.put("maxSimilarityText", compared > 0 ? String.format("%.2f%%", best * 100) : null);
        res.put("matchedStudentId", bestStudentId);
        res.put("matchedStudentName", bestStudentName);
        res.put("comparedCount", compared);
        return res;
    }

    /**
     * 教师端：实验作业相似度预警（面向“作业预警”看板）
     * 返回相似度较高的学生姓名，并说明是与谁的实验作业对比得到的最大相似度。
     *
     * 口径：
     * - 只统计当前教师 + 当前下拉课程（courseId）下的实验作业（lab=2）
     * - 每个学生取其全部实验作业中的“最大相似度”
     * - 相似度对比基于 tip2（阶段二）+ tip3（阶段三，可选拼接），并且仍复用 experimentSimilarityHint 的对比集合规则
     * - 只保留 comparedCount > 0 的可对比结果
     */
    public Map<String, Object> experimentSimilarityAlertByTeacherAndCourse(Integer teacherId, Integer courseId) {
        double thresholdPercent = 60d;
        int limit = 10;

        List<Work> lab2Works = workMapper.selectLab2ByCourseAndTeacher(courseId, teacherId);
        if (lab2Works == null) lab2Works = List.of();

        // studentId -> best record
        Map<Integer, Map<String, Object>> bestByStudent = new HashMap<>();

        for (Work w : lab2Works) {
            if (w == null || w.getId() == null) continue;
            if (w.getStudentId() == null) continue;

            // tip2 为空时无需计算（experimentSimilarityHint 内部依然会去取其它作业，浪费查询）
            if (!hasText(w.getTip2())) continue;

            Map<String, Object> hint = experimentSimilarityHint(w.getId());
            double maxSimilarity = hint.get("maxSimilarity") instanceof Number n ? n.doubleValue() : 0d;
            int comparedCount = hint.get("comparedCount") instanceof Number n ? n.intValue() : 0;
            if (comparedCount <= 0) continue;

            double similarityPercent = maxSimilarity * 100d;
            if (similarityPercent <= 0) continue;

            int sid = w.getStudentId();
            Map<String, Object> current = bestByStudent.get(sid);
            double currentBest = current != null && current.get("similarityPercent") instanceof Number cn ? cn.doubleValue() : -1d;

            if (current == null || similarityPercent > currentBest) {
                bestByStudent.put(sid, new HashMap<>(Map.of(
                        "studentId", sid,
                        "studentName", w.getStudentName(),
                        "comparedStudentName", hint.get("matchedStudentName"),
                        "similarityPercent", similarityPercent,
                        "similarityText", String.format("%.2f%%", similarityPercent)
                )));
            }
        }

        List<Map<String, Object>> list = bestByStudent.values().stream()
                .filter(m -> m.get("similarityPercent") instanceof Number n && n.doubleValue() >= thresholdPercent)
                .sorted((a, b) -> {
                    double av = a.get("similarityPercent") instanceof Number an ? an.doubleValue() : 0d;
                    double bv = b.get("similarityPercent") instanceof Number bn ? bn.doubleValue() : 0d;
                    return Double.compare(bv, av);
                })
                .limit(limit)
                .collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();
        res.put("threshold", (int) thresholdPercent);
        res.put("highSimilarityList", list);
        return res;
    }

    /**
     * 教师端：实验任务各阶段学生人数（按右上角课程下拉框 courseId 汇总）
     * 阶段判定：
     * - 阶段一：tip1 非空（实验环境提交）
     * - 阶段二：tip1 非空且 tip2 非空（阶段二提交）
     * - 阶段三：isExperimentContentSubmitted(w)（阶段三提交）
     *
     * 统计口径：去重学生（同一学生在多份实验任务中只计一次）。
     */
    public List<Map<String, Object>> experimentStageCountsByTeacherAndCourse(Integer teacherId, Integer courseId) {
        List<Work> lab2Works = workMapper.selectLab2ByCourseAndTeacher(courseId, teacherId);
        if (lab2Works == null) lab2Works = List.of();

        Set<Integer> stage1Students = new HashSet<>();
        Set<Integer> stage2Students = new HashSet<>();
        Set<Integer> stage3Students = new HashSet<>();

        for (Work w : lab2Works) {
            if (w == null || w.getStudentId() == null) continue;

            boolean has1 = hasText(w.getTip1());
            boolean has2 = hasText(w.getTip2());

            if (has1) stage1Students.add(w.getStudentId());
            if (has1 && has2) stage2Students.add(w.getStudentId());
            if (isExperimentContentSubmitted(w)) stage3Students.add(w.getStudentId());
        }

        return List.of(
                Map.of("label", "实验环境", "value", stage1Students.size()),
                Map.of("label", "实验内容步骤", "value", stage2Students.size()),
                Map.of("label", "实验总结", "value", stage3Students.size())
        );
    }

    public List<Work> selectAll() {
            // 只返回 lab 为 2 的记录
            List<Work> allWorks = workMapper.selectAll();
            return allWorks.stream()
                    .filter(w -> w.getLab() != null && w.getLab() == 2)
                    .collect(Collectors.toList());
    }
    public List<Work> selectAllone() {
        // 只返回 lab 为 2 的记录
        List<Work> allWorks = workMapper.selectAll();
        return allWorks.stream()
                .filter(w -> w.getLab() != null && w.getLab() == 1)
                .collect(Collectors.toList());
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** 实验任务是否已完成分段提交（阶段三：实验总结） */
    public boolean isExperimentContentSubmitted(Work w) {
        if (w == null) return false;
        if (w.getSubmitPhase() != null && w.getSubmitPhase() >= 3) {
            return hasText(w.getTip1()) && hasText(w.getTip2()) && hasText(w.getTip3());
        }
        // 兼容历史数据：以 tip1/2/3 是否齐全为准
        return hasText(w.getTip1()) && hasText(w.getTip2()) && hasText(w.getTip3());
    }

    /**
     * 学生分段保存实验作业（lab=2）。
     * phase：1 实验环境(tip1) 2 实验内容与步骤(tip2) 3 实验总结(tip3)
     */
    public void submitStudentStage(Integer studentId, int phase, Work body) {
        if (body == null || body.getId() == null) {
            throw new CustomException("参数错误", "");
        }
        Work db = workMapper.selectById(body.getId());
        if (db == null) {
            throw new CustomException("作业不存在", "");
        }
        if (db.getLab() == null || db.getLab() != 2) {
            throw new CustomException("仅实验任务支持分段提交", "");
        }
        if (!studentId.equals(db.getStudentId())) {
            throw new CustomException("无权操作该作业", "");
        }

        int n;
        switch (phase) {
            case 1 -> {
                if (!hasText(body.getTip1())) {
                    throw new CustomException("请填写实验环境", "");
                }
                n = workMapper.updateStudentStage1(body.getId(), studentId, body.getTip1().trim());
            }
            case 2 -> {
                if (!hasText(body.getTip2())) {
                    throw new CustomException("请填写实验内容与步骤", "");
                }
                n = workMapper.updateStudentStage2(body.getId(), studentId, body.getTip2().trim());
            }
            case 3 -> {
                if (!hasText(body.getTip3())) {
                    throw new CustomException("请填写实验总结与心得体会", "");
                }
                n = workMapper.updateStudentStage3(body.getId(), studentId, body.getTip3().trim());
            }
            default -> throw new CustomException("无效阶段", "");
        }
        if (n == 0) {
            throw new CustomException("保存失败：请先完成上一阶段或检查数据", "");
        }
    }

    /**
     * 某实验任务下，各分段累计完成人数与姓名（环境 → 步骤 → 总结）
     */
    public Map<String, Object> stageStatsByTaskId(Integer taskId) {
        if (taskId == null) {
            throw new CustomException("缺少 taskId", "");
        }
        List<Work> list = workMapper.selectByTaskIdForStageStats(taskId);
        List<Map<String, Object>> envList = new ArrayList<>();
        List<Map<String, Object>> stepsList = new ArrayList<>();
        List<Map<String, Object>> summaryList = new ArrayList<>();

        for (Work w : list) {
            String name = w.getStudentName() != null ? w.getStudentName() : ("学生#" + w.getStudentId());
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("studentId", w.getStudentId());
            one.put("studentName", name);
            one.put("workId", w.getId());

            if (hasText(w.getTip1())) {
                envList.add(one);
            }
            if (hasText(w.getTip1()) && hasText(w.getTip2())) {
                stepsList.add(new LinkedHashMap<>(one));
            }
            if (isExperimentContentSubmitted(w)) {
                summaryList.add(new LinkedHashMap<>(one));
            }
        }

        Map<String, Object> phaseTitle = new LinkedHashMap<>();
        phaseTitle.put("count", envList.size());
        phaseTitle.put("students", envList);

        Map<String, Object> phaseReq = new LinkedHashMap<>();
        phaseReq.put("count", stepsList.size());
        phaseReq.put("students", stepsList);

        Map<String, Object> phaseContent = new LinkedHashMap<>();
        phaseContent.put("count", summaryList.size());
        phaseContent.put("students", summaryList);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("taskId", taskId);
        res.put("totalStudents", list.size());
        res.put("phaseTitle", phaseTitle);
        res.put("phaseRequirement", phaseReq);
        res.put("phaseContent", phaseContent);
        return res;
    }
}