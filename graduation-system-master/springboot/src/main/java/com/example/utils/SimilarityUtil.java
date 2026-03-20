package com.example.utils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 文本相似度计算工具类
 * 使用余弦相似度算法计算文本相似度
 */
public class SimilarityUtil {

    /**
     * 计算两个文本的余弦相似度
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度值（0-1之间）
     */
    public static double calculateCosineSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.trim().isEmpty() || text2.trim().isEmpty()) {
            return 0.0;
        }

        // 预处理文本
        String processedText1 = preprocessText(text1);
        String processedText2 = preprocessText(text2);

        if (processedText1.isEmpty() || processedText2.isEmpty()) {
            return 0.0;
        }

        // 分词
        List<String> tokens1 = tokenize(processedText1);
        List<String> tokens2 = tokenize(processedText2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        // 构建词频向量
        Map<String, Integer> vector1 = buildVector(tokens1);
        Map<String, Integer> vector2 = buildVector(tokens2);

        // 获取所有不重复的词
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(vector1.keySet());
        allTerms.addAll(vector2.keySet());

        // 构建向量数组
        double[] array1 = new double[allTerms.size()];
        double[] array2 = new double[allTerms.size()];
        int index = 0;
        for (String term : allTerms) {
            array1[index] = vector1.getOrDefault(term, 0);
            array2[index] = vector2.getOrDefault(term, 0);
            index++;
        }

        // 计算余弦相似度
        return cosineSimilarity(array1, array2);
    }

    /**
     * 计算当前文本与多个文本的最大相似度
     * @param currentText 当前文本
     * @param otherTexts 其他文本列表（每个元素包含id和scontent字段）
     * @param excludeId 排除的ID（避免与自身比较）
     * @return 最大相似度值（0-1之间）
     */
    public static double calculateMaxSimilarity(String currentText, List<Map<String, Object>> otherTexts, Integer excludeId) {
        if (currentText == null || currentText.trim().isEmpty() || otherTexts == null || otherTexts.isEmpty()) {
            return 0.0;
        }

        double maxSimilarity = 0.0;
        for (Map<String, Object> item : otherTexts) {
            // 排除自身
            Object id = item.get("id");
            if (excludeId != null && id != null && excludeId.equals(Integer.valueOf(id.toString()))) {
                continue;
            }

            Object scontent = item.get("scontent");
            if (scontent == null || scontent.toString().trim().isEmpty()) {
                continue;
            }

            double similarity = calculateCosineSimilarity(currentText, scontent.toString());
            maxSimilarity = Math.max(maxSimilarity, similarity);
        }

        return maxSimilarity;
    }

    /**
     * 预处理文本：转小写，去除非字母数字字符
     */
    private static String preprocessText(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5 ]", "");
    }

    /**
     * 分词：对中文按字分割，对英文按空格分割
     */
    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return tokens;
        }

        // 按空格分割
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            // 如果是中文，按字分割
            if (Pattern.matches(".*[\\u4e00-\\u9fa5].*", word)) {
                for (char c : word.toCharArray()) {
                    if (c != ' ') {
                        tokens.add(String.valueOf(c));
                    }
                }
            } else {
                // 英文单词直接添加
                tokens.add(word);
            }
        }

        return tokens;
    }

    /**
     * 构建词频向量
     */
    private static Map<String, Integer> buildVector(List<String> tokens) {
        Map<String, Integer> vector = new HashMap<>();
        for (String token : tokens) {
            vector.put(token, vector.getOrDefault(token, 0) + 1);
        }
        return vector;
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private static double cosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
