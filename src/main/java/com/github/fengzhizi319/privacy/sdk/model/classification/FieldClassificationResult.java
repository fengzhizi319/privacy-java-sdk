package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段级分类结果（Field Classification Result）。
 * <p>
 * 描述单个字段的分类结果，包括命中的安全标签、最终敏感度等级、置信度、引擎层级、是否需要人工复核以及可解释文本。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class FieldClassificationResult {

    /** 字段名称。 */
    private String fieldName;

    /** 字段值，输出中可能被省略或掩码。 */
    private String fieldValue;

    /** 命中的安全标签列表。 */
    private List<SecurityTag> tags = new ArrayList<>();

    /** 最终敏感度等级。 */
    private SensitivityLevel finalLevel;

    /** 最终置信度。 */
    private double confidence;

    /** 引擎层级。 */
    private EngineLayer engineLayer;

    /** 是否需要人工复核。 */
    private boolean needsHumanReview;

    /** 可解释推理文本。 */
    private String reasoning;

    /**
     * 默认构造器。
     */
    public FieldClassificationResult() {
    }

    /**
     * 构造字段分类结果。
     *
     * @param fieldName 字段名称
     */
    public FieldClassificationResult(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public List<SecurityTag> getTags() {
        return tags;
    }

    public void setTags(List<SecurityTag> tags) {
        this.tags = tags == null ? new ArrayList<>() : tags;
    }

    public SensitivityLevel getFinalLevel() {
        return finalLevel;
    }

    public void setFinalLevel(SensitivityLevel finalLevel) {
        this.finalLevel = finalLevel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public EngineLayer getEngineLayer() {
        return engineLayer;
    }

    public void setEngineLayer(EngineLayer engineLayer) {
        this.engineLayer = engineLayer;
    }

    public boolean isNeedsHumanReview() {
        return needsHumanReview;
    }

    public void setNeedsHumanReview(boolean needsHumanReview) {
        this.needsHumanReview = needsHumanReview;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    @Override
    public String toString() {
        return "FieldClassificationResult{" +
            "fieldName='" + fieldName + '\'' +
            ", finalLevel=" + finalLevel +
            ", confidence=" + confidence +
            ", engineLayer=" + engineLayer +
            ", needsHumanReview=" + needsHumanReview +
            ", reasoning='" + reasoning + '\'' +
            '}';
    }
}
