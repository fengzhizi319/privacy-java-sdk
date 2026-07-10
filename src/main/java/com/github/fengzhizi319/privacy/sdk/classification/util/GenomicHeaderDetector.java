package com.github.fengzhizi319.privacy.sdk.classification.util;

import java.util.regex.Pattern;

/**
 * 基因组文件头/序列检测器（Genomic Header Detector）。
 * <p>
 * 识别 BAM、VCF、FASTQ 文件头特征，以及长序列片段（ATCGN）。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class GenomicHeaderDetector {

    /** 长基因序列片段正则：至少 50 个 ATCGN（大小写均可）。 */
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("[ATCGNatcgn]{50,}");

    /**
     * 判断值是否为 BAM 文件头。
     * <p>
     * 特征：以 magic {@code BAM\x01} 开头，或以 {@code @SQ} 开头的 SAM/BAM 序列头。
     * </p>
     *
     * @param value 字段值
     * @return 命中时返回 {@code true}
     */
    public static boolean isBamHeader(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.startsWith("BAM\u0001") || value.startsWith("@SQ");
    }

    /**
     * 判断值是否为 VCF 文件头。
     *
     * @param value 字段值
     * @return 命中时返回 {@code true}
     */
    public static boolean isVcfHeader(String value) {
        return value != null && value.startsWith("##fileformat=VCF");
    }

    /**
     * 判断值是否为 FASTQ 文件头。
     * <p>
     * 特征：以 {@code @} 开头，包含 SRR/ERR/DRR 读取名，或第二行为碱基、第三行为 {@code +}。
     * 当前实现对纯文件头做宽松判断。
     * </p>
     *
     * @param value 字段值
     * @return 命中时返回 {@code true}
     */
    public static boolean isFastqHeader(String value) {
        if (value == null || value.isEmpty() || value.charAt(0) != '@') {
            return false;
        }
        String lower = value.toLowerCase();
        if (lower.contains("srr") || lower.contains("err") || lower.contains("drr")) {
            return true;
        }
        String[] lines = value.split("\\r?\\n");
        if (lines.length >= 3 && "+".equals(lines[2].trim())) {
            return true;
        }
        return false;
    }

    /**
     * 判断值中是否包含长基因序列片段（>=50 个碱基）。
     *
     * @param value 字段值
     * @return 命中时返回 {@code true}
     */
    public static boolean containsSequence(String value) {
        return value != null && SEQUENCE_PATTERN.matcher(value).find();
    }

    /**
     * 综合判断字段值是否与基因组数据相关。
     *
     * @param value 字段值
     * @return 命中 BAM/VCF/FASTQ/序列之一时返回 {@code true}
     */
    public static boolean isGenomicContent(String value) {
        return isBamHeader(value) || isVcfHeader(value) || isFastqHeader(value) || containsSequence(value);
    }
}
