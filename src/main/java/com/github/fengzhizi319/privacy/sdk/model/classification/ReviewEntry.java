package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.List;

/**
 * 人工复核条目（Review Entry）。
 * <p>
 * 记录需要人工复核的字段、预测等级、预测标签以及复核状态。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ReviewEntry {

    /** 复核 ID。 */
    private String reviewId;

    /** 记录索引。 */
    private int recordIndex;

    /** 字段名。 */
    private String fieldName;

    /** 字段值。 */
    private String fieldValue;

    /** 预测等级。 */
    private SensitivityLevel predictedLevel;

    /** 预测标签字符串列表。 */
    private List<String> predictedTags;

    /** 修正后的等级。 */
    private String correctedLevel;

    /** 复核人。 */
    private String reviewer = "";

    /** 复核说明。 */
    private String comment = "";

    /** 复核状态。 */
    private ReviewStatus status = ReviewStatus.PENDING;

    /** 创建时间（ISO-8601）。 */
    private String createdAt;

    /** 更新时间（ISO-8601）。 */
    private String updatedAt;

    public ReviewEntry() {
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public int getRecordIndex() {
        return recordIndex;
    }

    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
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

    public SensitivityLevel getPredictedLevel() {
        return predictedLevel;
    }

    public void setPredictedLevel(SensitivityLevel predictedLevel) {
        this.predictedLevel = predictedLevel;
    }

    public List<String> getPredictedTags() {
        return predictedTags;
    }

    public void setPredictedTags(List<String> predictedTags) {
        this.predictedTags = predictedTags;
    }

    public String getCorrectedLevel() {
        return correctedLevel;
    }

    public void setCorrectedLevel(String correctedLevel) {
        this.correctedLevel = correctedLevel;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
