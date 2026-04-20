package com.example.service;

import com.example.entity.Work;
import com.example.mapper.WorkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 实验报告导出服务
 * 使用 word.docx 模板替换参数生成报告
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentReportService {

    private final WorkMapper workMapper;

    // Word 模板路径
    private static final String TEMPLATE_PATH = "templates/word.docx";

    /**
     * 生成实验报告 Word 文档（使用模板替换方式）
     *
     * @param workId 作业ID
     * @return Word 文档字节数组
     */
    public byte[] generateReportDocx(Integer workId) {
        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new RuntimeException("作业不存在: " + workId);
        }

        try {
            // 1. 读取模板文件
            ClassPathResource templateResource = new ClassPathResource(TEMPLATE_PATH);
            if (!templateResource.exists()) {
                // 如果 classpath 中没有，尝试从文件系统读取（开发环境）
                File templateFile = new File("vue/public/word.docx");
                if (!templateFile.exists()) {
                    throw new RuntimeException("模板文件不存在: " + TEMPLATE_PATH + " 或 vue/public/word.docx");
                }
                return generateFromTemplate(work, new FileInputStream(templateFile));
            }
            return generateFromTemplate(work, templateResource.getInputStream());
        } catch (Exception e) {
            log.error("生成实验报告失败, workId: {}", workId, e);
            throw new RuntimeException("生成实验报告失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用模板生成 Word 文档
     */
    private byte[] generateFromTemplate(Work work, InputStream templateInput) throws IOException {
        // 读取模板到内存
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        templateInput.transferTo(baos);
        byte[] templateBytes = baos.toByteArray();
        templateInput.close();

        // 准备替换数据
        Map<String, String> replacements = prepareReplacements(work);

        // 处理 docx 文件（本质是 zip）
        ByteArrayInputStream bais = new ByteArrayInputStream(templateBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(bais);
             ZipOutputStream zos = new ZipOutputStream(outputStream)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                zos.putNextEntry(new ZipEntry(entryName));

                if (entryName.equals("word/document.xml")) {
                    // 读取并处理 document.xml
                    String content = new String(zis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    String processedContent = processDocumentXml(content, replacements);
                    zos.write(processedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } else {
                    // 直接复制其他文件
                    zis.transferTo(zos);
                }
                zos.closeEntry();
                zis.closeEntry();
            }
        }

        return outputStream.toByteArray();
    }

    /**
     * 处理 document.xml 内容
     * Word 的 XML 会把文本分散在多个 <w:t> 标签中，需要先合并再处理
     */
    private String processDocumentXml(String xml, Map<String, String> replacements) {
        String result = xml;

        // 1. 先处理 tableData 循环标记（这些标记可能跨多个 XML 标签）
        result = processTableDataLoop(result);

        // 2. 替换普通占位符（处理可能分散在多个标签中的情况）
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = escapeXml(entry.getValue());

            // 处理 {key} 格式（可能分散在多个 <w:t> 标签中）
            result = replacePlaceholderInXml(result, key, value);
        }

        // 3. 清理未替换的占位符
        result = cleanupRemainingPlaceholders(result);

        return result;
    }

    /**
     * 在 XML 中替换占位符（处理分散在多个 <w:t> 标签中的情况）
     */
    private String replacePlaceholderInXml(String xml, String key, String value) {
        String result = xml;

        // 模式1: 完整的占位符在一个 <w:t> 标签中
        // <w:t>{key}</w:t> 或 <w:t>{key | filter}</w:t>
        result = result.replace(">{" + key + "}<", ">" + value + "<");
        result = result.replaceAll(">\\{" + Pattern.quote(key) + "\\s*\\|[^}]*\\}<", ">" + Matcher.quoteReplacement(value) + "<");

        // 模式2: 占位符被 XML 标签分割（如 <w:t>{</w:t><w:t>#tableData</w:t>...）
        // 这种情况已经在 processTableDataLoop 中处理了大部分

        return result;
    }

    /**
     * 处理 tableData 循环标记
     * Word XML 中 {#tableData} 和 {/tableData} 可能被分散在多个标签中
     */
    private String processTableDataLoop(String xml) {
        String result = xml;

        // ===== 处理 {#tableData} 开始标记 =====
        // 完整形式: <w:t>{#tableData}</w:t>
        result = result.replaceAll("<w:t>\\{#tableData\\}</w:t>", "");

        // 分散形式1: <w:t>{</w:t><w:t>#</w:t><w:t>tableData</w:t><w:t>}</w:t>
        result = result.replaceAll("<w:t>\\{</w:t>\\s*<w:t>#</w:t>\\s*<w:t>tableData</w:t>\\s*<w:t>\\}</w:t>", "");

        // 分散形式2: <w:t>{</w:t></w:r>...<w:r><w:t>#</w:t>... 等跨多个 <w:r> 的情况
        // 使用更宽松的模式，匹配 { # tableData } 之间可能有任意 XML 标签
        result = result.replaceAll("<w:t>\\{</w:t>\\s*</w:r>\\s*<w:r>\\s*<w:t>#</w:t>\\s*</w:r>\\s*<w:r>\\s*<w:t>tableData</w:t>\\s*</w:r>\\s*<w:r>\\s*<w:t>\\}</w:t>", "");

        // 其他可能的分隔形式
        result = result.replaceAll("\\{[^<]*<[^>]+>\\s*#\\s*</[^>]+>[^<]*tableData[^<]*<[^>]+>\\s*\\}", "");

        // ===== 处理 {/tableData} 结束标记 =====
        // 完整形式: <w:t>{/tableData}</w:t>
        result = result.replaceAll("<w:t>\\{/tableData\\}</w:t>", "");

        // 分散形式1: <w:t>{</w:t><w:t>/</w:t><w:t>tableData</w:t><w:t>}</w:t>
        result = result.replaceAll("<w:t>\\{</w:t>\\s*<w:t>/</w:t>\\s*<w:t>tableData</w:t>\\s*<w:t>\\}</w:t>", "");

        // 分散形式2: 跨多个 <w:r> 的情况
        result = result.replaceAll("<w:t>\\{</w:t>\\s*</w:r>\\s*<w:r>\\s*<w:t>/</w:t>\\s*</w:r>\\s*<w:r>\\s*<w:t>tableData</w:t>\\s*</w:r>\\s*<w:r>\\s*<w:t>\\}</w:t>", "");

        // 其他可能的分隔形式
        result = result.replaceAll("\\{[^<]*<[^>]+>\\s*/\\s*</[^>]+>[^<]*tableData[^<]*<[^>]+>\\s*\\}", "");

        // ===== 清理可能残留的单独标记 =====
        result = result.replaceAll("<w:t>#tableData</w:t>", "");
        result = result.replaceAll("<w:t>/tableData</w:t>", "");
        result = result.replaceAll("<w:t>\\{#tableData</w:t>", "");
        result = result.replaceAll("<w:t>tableData\\}</w:t>", "");
        result = result.replaceAll("<w:t>/tableData\\}</w:t>", "");

        // 处理循环内的 {.field} 标记 -> {field}
        result = result.replace("{.courseName}", "{courseName}");
        result = result.replace("{.teacherName}", "{teacherName}");
        result = result.replace("{.studentName}", "{studentName}");
        result = result.replace("{.studentCode}", "{studentCode}");
        result = result.replace("{.classIds}", "{classIds}");
        result = result.replace("{.place}", "{place}");
        result = result.replace("{.time}", "{time}");

        return result;
    }

    /**
     * 清理剩余的未替换占位符
     */
    private String cleanupRemainingPlaceholders(String xml) {
        String result = xml;

        // 清理 tableData 相关（开头和结尾的）
        result = result.replaceAll("<w:t>\\{</w:t>\\s*<w:t>#</w:t>\\s*<w:t>tableData</w:t>\\s*<w:t>\\}</w:t>", "");
        result = result.replaceAll("<w:t>\\{</w:t>\\s*<w:t>/</w:t>\\s*<w:t>tableData</w:t>\\s*<w:t>\\}</w:t>", "");

        // 清理 {key} 格式的占位符
        result = result.replaceAll("<w:t>\\{[a-zA-Z0-9_]+\\}</w:t>", "");
        // 清理 {key | filter} 格式的占位符
        result = result.replaceAll("<w:t>\\{[a-zA-Z0-9_]+\\s*\\|[^}]*\\}</w:t>", "");

        // 清理分散形式的占位符（如 <w:t>{</w:t><w:t>key</w:t><w:t>}</w:t>）
        result = result.replaceAll("<w:t>\\{</w:t>\\s*<w:t>[a-zA-Z0-9_]+</w:t>\\s*<w:t>\\}</w:t>", "");

        // 清理单独的 { 和 }
        result = result.replaceAll("<w:t>\\{</w:t>\\s*$", "");
        result = result.replaceAll("^\\s*<w:t>\\}</w:t>", "");

        // 最终清理：任何包含 tableData 的 <w:t> 标签
        result = result.replaceAll("<w:t>[^<]*tableData[^<]*</w:t>", "");

        return result;
    }

    /**
     * 准备替换数据
     */
    private Map<String, String> prepareReplacements(Work work) {
        Map<String, String> map = new HashMap<>();

        // 基本信息
        map.put("courseName", nullToEmpty(work.getCourseName()));
        map.put("teacherName", nullToEmpty(work.getTeacherName()));
        map.put("studentName", nullToEmpty(work.getStudentName()));
        map.put("studentCode", nullToEmpty(work.getStudentCode()));
        map.put("classIds", nullToEmpty(work.getClassName()));
        map.put("place", nullToEmpty(work.getPlace()));

        // 时间处理：只取年月日
        String time = "";
        if (work.getExperimentTime() != null) {
            time = work.getExperimentTime().split(" ")[0];
        }
        map.put("time", time);

        // 实验内容、目的、环境
        map.put("name", nullToEmpty(work.getExperimentContent()));
        map.put("purpose", nullToEmpty(work.getExperimentPurpose()));
        map.put("environment", nullToEmpty(work.getExperimentEnvironment()));

        // 题目和答案（q1-q9, tip1-tip9）
        for (int i = 1; i <= 9; i++) {
            String q = getFieldValue(work, "q" + i);
            String tip = getFieldValue(work, "tip" + i);

            // 如果题目为空，则不显示（替换为空字符串）
            if (q == null || q.trim().isEmpty()) {
                map.put("q" + i, "");
                map.put("tip" + i, "");
            } else {
                map.put("q" + i, q);
                map.put("tip" + i, nullToEmpty(tip));
            }
        }

        // 心得体会和教师评语
        map.put("experience", nullToEmpty(work.getExperience()));
        map.put("teacherComment", nullToEmpty(work.getTeacherComment()));

        // 成绩
        map.put("score", work.getScore() != null ? String.valueOf(work.getScore()) : "");

        // 兼容前端模板的其他可能字段
        map.put("experimentContent", nullToEmpty(work.getExperimentContent()));
        map.put("experimentPurpose", nullToEmpty(work.getExperimentPurpose()));
        map.put("experimentEnvironment", nullToEmpty(work.getExperimentEnvironment()));
        map.put("experimentRequirement", nullToEmpty(work.getExperimentRequirement()));

        return map;
    }

    /**
     * 获取 Work 对象的字段值
     */
    private String getFieldValue(Work work, String fieldName) {
        return switch (fieldName) {
            case "q1" -> work.getQ1();
            case "q2" -> work.getQ2();
            case "q3" -> work.getQ3();
            case "q4" -> work.getQ4();
            case "q5" -> work.getQ5();
            case "q6" -> work.getQ6();
            case "q7" -> work.getQ7();
            case "q8" -> work.getQ8();
            case "q9" -> work.getQ9();
            case "tip1" -> work.getTip1();
            case "tip2" -> work.getTip2();
            case "tip3" -> work.getTip3();
            case "tip4" -> work.getTip4();
            case "tip5" -> work.getTip5();
            case "tip6" -> work.getTip6();
            case "tip7" -> work.getTip7();
            case "tip8" -> work.getTip8();
            case "tip9" -> work.getTip9();
            default -> null;
        };
    }

    /**
     * XML 特殊字符转义
     * 将换行符转换为 Word 的换行标签 <w:br/>
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        // 先进行 XML 转义
        String result = text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");

        // 将换行符转换为 Word 换行标签
        // 处理 \r\n 和 \n 两种换行符
        result = result.replace("\r\n", "<w:br/>");
        result = result.replace("\n", "<w:br/>");
        result = result.replace("\r", "<w:br/>");

        return result;
    }

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }
}
