package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.ArrayList;
import java.util.List;

/**
 * 表级/批次分类结果（Table Classification Result）。
 * <p>
 * 描述整张表或一批记录的分类结果，包含表结构、各条记录的分类结果，以及聚合后的标签与最终敏感度等级。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class TableClassificationResult {

    /** 表结构，即列名列表。 */
    private List<String> schema = new ArrayList<>();

    /** 各条记录的分类结果。 */
    private List<RecordClassificationResult> recordResults = new ArrayList<>();

    /** 聚合后的安全标签列表。 */
    private List<SecurityTag> aggregatedTags = new ArrayList<>();

    /** 表最终敏感度等级。 */
    private SensitivityLevel finalLevel;

    /** 表最终置信度。 */
    private double confidence;

    /** 是否需要人工复核。 */
    private boolean needsHumanReview;

    /**
     * 默认构造器。
     */
    public TableClassificationResult() {
    }

    /**
     * 构造表级分类结果。
     *
     * @param schema 表结构列名列表
     */
    public TableClassificationResult(List<String> schema) {
        this.schema = schema == null ? new ArrayList<>() : schema;
    }

    public List<String> getSchema() {
        return schema;
    }

    public void setSchema(List<String> schema) {
        this.schema = schema == null ? new ArrayList<>() : schema;
    }

    public List<RecordClassificationResult> getRecordResults() {
        return recordResults;
    }

    public void setRecordResults(List<RecordClassificationResult> recordResults) {
        this.recordResults = recordResults == null ? new ArrayList<>() : recordResults;
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
