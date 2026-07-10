package com.github.fengzhizi319.privacy.sdk.model.classification;

/**
 * 安全标签（Security Tag）。
 * <p>
 * 描述一个字段被识别出的敏感属性，包括敏感度等级、类别、置信度、来源引擎、规则 ID、版本以及是否需要人工复核。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class SecurityTag {

    /** 敏感度等级。 */
    private SensitivityLevel level;

    /** 类别，例如 PII_ID_CARD、MEDICAL_ICD10_HIV、GENOMIC_BRCA_TP53。 */
    private String category;

    /** 置信度，范围 0.0 ~ 1.0。 */
    private double confidence;

    /** 来源引擎，例如 RULE、SMALL_NER、LLM、MANUAL。 */
    private String sourceEngine;

    /** 规则 ID，例如 RULE_ID_001。 */
    private String ruleId;

    /** 版本，默认 1.0.0。 */
    private String version = "1.0.0";

    /** 是否需要人工复核。 */
    private boolean needsHumanReview;

    /**
     * 默认构造器。
     */
    public SecurityTag() {
    }

    /**
     * 构造一个安全标签。
     *
     * @param level       敏感度等级
     * @param category    类别
     * @param confidence  置信度
     * @param sourceEngine 来源引擎
     * @param ruleId      规则 ID
     */
    public SecurityTag(SensitivityLevel level, String category, double confidence,
                       String sourceEngine, String ruleId) {
        this.level = level;
        this.category = category;
        this.confidence = confidence;
        this.sourceEngine = sourceEngine;
        this.ruleId = ruleId;
    }

    /**
     * 获取标签字符串表示，格式为 {@code level_category}，例如 {@code L3_PII_ID_CARD}。
     *
     * @return 标签字符串
     */
    public String toTagString() {
        return level + "_" + category;
    }

    public SensitivityLevel getLevel() {
        return level;
    }

    public void setLevel(SensitivityLevel level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSourceEngine() {
        return sourceEngine;
    }

    public void setSourceEngine(String sourceEngine) {
        this.sourceEngine = sourceEngine;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isNeedsHumanReview() {
        return needsHumanReview;
    }

    public void setNeedsHumanReview(boolean needsHumanReview) {
        this.needsHumanReview = needsHumanReview;
    }

    @Override
    public String toString() {
        return "SecurityTag{" +
            "level=" + level +
            ", category='" + category + '\'' +
            ", confidence=" + confidence +
            ", sourceEngine='" + sourceEngine + '\'' +
            ", ruleId='" + ruleId + '\'' +
            ", version='" + version + '\'' +
            ", needsHumanReview=" + needsHumanReview +
            '}';
    }
}
