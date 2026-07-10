package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.classification.util.GenomicHeaderDetector;
import com.github.fengzhizi319.privacy.sdk.classification.util.Icd10Matcher;
import com.github.fengzhizi319.privacy.sdk.classification.util.IdCardValidator;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationParams;
import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 默认规则引擎（Default Rule Engine）。
 * <p>
 * 实现规范中 Layer 1 的全部字段名规则与值规则，返回命中的安全标签列表。
 * 所有规则命中的置信度均为 1.0，默认不需要人工复核。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class DefaultRuleEngine implements RuleEngine {

    /** 引擎版本。 */
    private static final String VERSION = "1.0.0";

    /** 手机号正则。 */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 上海医保卡号正则。 */
    private static final Pattern MEDICAL_CARD_PATTERN = Pattern.compile("^\\d{9}$");

    /** rs 编号或基因组变异常量词正则。 */
    private static final Pattern RS_OR_VARIANT_PATTERN = Pattern.compile("(^|[^a-zA-Z0-9])rs\\d+");

    /** 中国大陆身份证正则（含校验和）。 */
    private static final String SOURCE_ENGINE = "RULE";

    @Override
    public List<SecurityTag> classifyField(String fieldName, Object value, ClassificationParams params) {
        List<SecurityTag> tags = new ArrayList<>();
        String normalizedName = normalizeFieldName(fieldName);
        String strValue = value == null ? "" : value.toString();

        // 1. 字段名规则
        applyFieldNameRules(normalizedName, tags, params);

        // 2. 值规则
        applyValueRules(fieldName, strValue, tags, params);

        return tags;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * 规范化字段名：转小写并移除下划线与空格。
     *
     * @param fieldName 原始字段名
     * @return 规范化后的字段名
     */
    private String normalizeFieldName(String fieldName) {
        if (fieldName == null) {
            return "";
        }
        return fieldName.toLowerCase().replace("_", "").replace(" ", "");
    }

    /**
     * 应用字段名规则。
     */
    private void applyFieldNameRules(String normalizedName, List<SecurityTag> tags, ClassificationParams params) {
        // RULE_ID_G_001: brca1 / brca2 / tp53
        if (containsAny(normalizedName, "brca1", "brca2", "tp53")) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_BRCA_TP53", "RULE_ID_G_001"));
        }

        // RULE_ID_G_002: rs\d+ / snp / cnv / genome / genomic
        if (RS_OR_VARIANT_PATTERN.matcher(normalizedName).find()
            || containsAny(normalizedName, "snp", "cnv", "genome", "genomic")) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_VARIANT", "RULE_ID_G_002"));
        }

        // RULE_ID_G_003: gene / mutation / variant
        if (containsAny(normalizedName, "gene", "mutation", "variant")) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_HINT", "RULE_ID_G_003"));
        }

        // RULE_ID_G_004: bam / vcf / fastq
        if (containsAny(normalizedName, "bam", "vcf", "fastq")) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_FILE", "RULE_ID_G_004"));
        }
    }

    /**
     * 应用值规则。
     */
    private void applyValueRules(String fieldName, String value, List<SecurityTag> tags, ClassificationParams params) {
        // RULE_ID_001: 中国大陆身份证号
        if (IdCardValidator.isValid(value)) {
            tags.add(ruleTag(SensitivityLevel.L3, "PII_ID_CARD", "RULE_ID_001"));
        }

        // RULE_ID_002: 手机号
        if (MOBILE_PATTERN.matcher(value).matches()) {
            tags.add(ruleTag(SensitivityLevel.L3, "PII_MOBILE", "RULE_ID_002"));
        }

        // RULE_ID_003: 上海医保卡号
        if (isValidShanghaiMedicalCard(value)) {
            tags.add(ruleTag(SensitivityLevel.L3, "PII_MEDICAL_CARD", "RULE_ID_003"));
        }

        // RULE_ID_004: ICD-10 编码
        if (Icd10Matcher.isValidCode(value)) {
            String normalized = Icd10Matcher.normalize(value);
            SecurityTag icdTag = classifyIcd10(normalized, params);
            if (icdTag != null) {
                tags.add(icdTag);
            }
        }

        // RULE_ID_G_010: BAM 文件头
        if (GenomicHeaderDetector.isBamHeader(value)) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_BAM", "RULE_ID_G_010"));
        }

        // RULE_ID_G_011: VCF 文件头
        if (GenomicHeaderDetector.isVcfHeader(value)) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_VCF", "RULE_ID_G_011"));
        }

        // RULE_ID_G_012: FASTQ 文件头
        if (GenomicHeaderDetector.isFastqHeader(value)) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_FASTQ", "RULE_ID_G_012"));
        }

        // RULE_ID_G_013: 基因序列片段
        if (GenomicHeaderDetector.containsSequence(value)) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_SEQUENCE", "RULE_ID_G_013"));
        }

        // RULE_ID_G_002（值侧补充）：rs 编号同样可出现在字段值中
        if (RS_OR_VARIANT_PATTERN.matcher(value).find()) {
            tags.add(ruleTag(SensitivityLevel.L5, "GENOMIC_VARIANT", "RULE_ID_G_002"));
        }

        // RULE_ID_L1_001: 公开报表字段白名单
        String normalizedName = fieldName == null ? "" : fieldName.toLowerCase();
        if (containsAny(normalizedName, toArray(params.getPublicFieldWhitelist()))) {
            tags.add(ruleTag(SensitivityLevel.L1, "PUBLIC_REPORT", "RULE_ID_L1_001"));
        }

        // RULE_ID_L2_001: 运营统计字段
        if (containsAny(normalizedName, toArray(params.getOperationalFieldPatterns()))) {
            tags.add(ruleTag(SensitivityLevel.L2, "OPERATIONAL_STAT", "RULE_ID_L2_001"));
        }
    }

    /**
     * 对 ICD-10 编码执行区间映射。
     */
    private SecurityTag classifyIcd10(String normalized, ClassificationParams params) {
        if (normalized == null) {
            return null;
        }
        for (ClassificationParams.Icd10Interval interval : params.getIcd10L4Intervals()) {
            if (Icd10Matcher.inRange(normalized, interval.getStart(), interval.getEnd())) {
                String category;
                String start = interval.getStart();
                if ("B20".equals(start)) {
                    category = "MEDICAL_ICD10_HIV";
                } else if ("F20".equals(start)) {
                    category = "MEDICAL_ICD10_PSYCHIATRIC";
                } else if ("C00".equals(start)) {
                    category = "MEDICAL_ICD10_CANCER";
                } else {
                    category = "MEDICAL_ICD10_GENERAL";
                }
                return ruleTag(SensitivityLevel.L4, category, "RULE_ID_004");
            }
        }
        return ruleTag(SensitivityLevel.L3, "MEDICAL_ICD10_GENERAL", "RULE_ID_004");
    }

    /**
     * 校验上海医保卡号及其校验和。
     */
    private boolean isValidShanghaiMedicalCard(String value) {
        if (value == null || !MEDICAL_CARD_PATTERN.matcher(value).matches()) {
            return false;
        }
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1};
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += (value.charAt(i) - '0') * weights[i];
        }
        int check = (10 - sum % 10) % 10;
        return check == (value.charAt(8) - '0');
    }

    /**
     * 构造规则命中的安全标签。
     */
    private SecurityTag ruleTag(SensitivityLevel level, String category, String ruleId) {
        SecurityTag tag = new SecurityTag(level, category, 1.0, SOURCE_ENGINE, ruleId);
        tag.setVersion(VERSION);
        tag.setNeedsHumanReview(false);
        return tag;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isEmpty() && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String[] toArray(List<String> list) {
        return list == null ? new String[0] : list.toArray(new String[0]);
    }
}
