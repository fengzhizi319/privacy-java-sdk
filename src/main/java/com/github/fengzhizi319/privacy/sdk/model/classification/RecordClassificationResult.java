package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 记录级分类结果（Record Classification Result）。
 * <p>
 * 描述单条记录（多个字段）的分类结果，聚合所有字段的标签，并取字段中的最高敏感度等级作为记录最终等级。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class RecordClassificationResult {

    /** 记录在批次中的索引。 */
    private int recordIndex;

    /** 各字段的分类结果，key 为字段名。 */
    private Map<String, FieldClassificationResult> fieldResults = new LinkedHashMap<>();

    /** 聚合后的安全标签列表。 */
    private List<SecurityTag> aggregatedTags = new ArrayList<>();

    /** 记录最终敏感度等级。 */
    private SensitivityLevel finalLevel;

    /** 记录最终置信度。 */
    private double confidence;

    /** 是否需要人工复核。 */
    private boolean needsHumanReview;

    /**
     * 默认构造器。
     */
    public RecordClassificationResult() {
    }

    /**
     * 构造记录分类结果。
     *
     * @param recordIndex 记录索引
     */
    public RecordClassificationResult(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    public int getRecordIndex() {
        return recordIndex;
    }

    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    public Map<String, FieldClassificationResult> getFieldResults() {
        return fieldResults;
    }

    public void setFieldResults(Map<String, FieldClassificationResult> fieldResults) {
        this.fieldResults = fieldResults == null ? new LinkedHashMap<>() : fieldResults;
    }

    public List<SecurityTag> getAggregatedTags() {
        return aggregatedTags;
    }

    public void setAggregatedTags(List<SecurityTag> aggregatedTags) {
        this.aggregatedTags = aggregatedTags == null ? new ArrayList<>() : aggregatedTags;
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

    public boolean isNeedsHumanReview() {
        return needsHumanReview;
    }

    public void setNeedsHumanReview(boolean needsHumanReview) {
        this.needsHumanReview = needsHumanReview;
    }
}
