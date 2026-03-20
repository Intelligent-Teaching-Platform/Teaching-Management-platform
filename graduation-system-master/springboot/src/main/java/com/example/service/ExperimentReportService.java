package com.example.service;

import com.example.entity.Work;
import com.example.mapper.WorkMapper;
import jakarta.annotation.Resource;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 实验报告 Word 文档生成服务
 * 实验任务（lab=2）学生提交后，按预定格式生成实验报告.docx
 */
@Service
public class ExperimentReportService {

    @Resource
    private WorkMapper workMapper;

    /**
     * 根据作业 id 生成实验报告 Word 文档，返回字节数组
     */
    public byte[] generateReportDocx(Integer workId) throws IOException {
        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new IllegalArgumentException("作业不存在");
        }
        if (work.getLab() == null || work.getLab() != 2) {
            throw new IllegalArgumentException("仅支持实验任务类型的作业");
        }
        return buildDocx(work);
    }

    private byte[] buildDocx(Work w) throws IOException {
        XWPFDocument doc = new XWPFDocument();

        // 标题
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("实验报告");
        titleRun.setBold(true);
        titleRun.setFontSize(22);
        titleRun.setFontFamily("宋体");

        emptyParagraph(doc);

        // 信息表格：实验名称、学生、教师、实验内容、提交内容、相似度、成绩、修改意见
        String[][] rows = {
                {"实验名称", nullToEmpty(w.getName())},
                {"学生姓名", nullToEmpty(w.getStudentName())},
                {"授课教师", nullToEmpty(w.getTeacherName())},
                {"实验内容", nullToEmpty(w.getContent())},
                {"提交内容", nullToEmpty(w.getScontent())},
                {"相似度", nullToEmpty(w.getTip1())},
                {"成绩", w.getScore() != null ? w.getScore().toString() : ""},
                {"修改意见", nullToEmpty(w.getAmendment())},
        };

        XWPFTable table = doc.createTable(rows.length, 2);
        table.setWidth("100%");
        for (int i = 0; i < rows.length; i++) {
            XWPFTableRow row = table.getRow(i);
            row.getCell(0).setText(rows[i][0]);
            row.getCell(1).setText(rows[i][1]);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        doc.close();
        return out.toByteArray();
    }

    private void emptyParagraph(XWPFDocument doc) {
        doc.createParagraph().createRun().addBreak();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
