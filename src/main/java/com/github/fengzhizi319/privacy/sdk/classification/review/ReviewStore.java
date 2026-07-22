package com.github.fengzhizi319.privacy.sdk.classification.review;

import com.github.fengzhizi319.privacy.sdk.api.MaskingApi;
import com.github.fengzhizi319.privacy.sdk.model.classification.ReviewEntry;
import com.github.fengzhizi319.privacy.sdk.model.classification.ReviewStatus;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人工复核样本存储（Review Store）。
 * <p>
 * 提供轻量复核队列：自动收集需要人工复核的字段/记录，支持确认/修正等级，
 * 并导出复核样本。当前实现为内存模式，进程结束时数据丢失。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ReviewStore {

    private final Map<String, ReviewEntry> entries = new ConcurrentHashMap<>();
    private final MaskingApi maskingApi = new MaskingApi();

    /**
     * 添加一条需要人工复核的条目。
     *
     * @param recordIndex   记录索引
     * @param fieldName     字段名
     * @param fieldValue    字段值
     * @param predictedLevel 预测等级
     * @param predictedTags 预测标签字符串列表
     * @return 新增的复核条目
     */
    public ReviewEntry addReview(int recordIndex, String fieldName, String fieldValue,
                                 SensitivityLevel predictedLevel, List<String> predictedTags) {
        String reviewId = "review-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ReviewEntry entry = new ReviewEntry();
        entry.setReviewId(reviewId);
        entry.setRecordIndex(recordIndex);
        entry.setFieldName(fieldName);
        entry.setFieldValue(fieldValue);
        entry.setPredictedLevel(predictedLevel);
        entry.setPredictedTags(predictedTags == null ? new ArrayList<>() : predictedTags);
        entry.setStatus(ReviewStatus.PENDING);
        entry.setCreatedAt(Instant.now().toString());
        entries.put(reviewId, entry);
        return entry;
    }

    /**
     * 确认或修正复核条目。
     *
     * @param reviewId       复核条目 ID
     * @param correctedLevel 修正后的敏感度等级
     * @param reviewer       复核人
     * @param comment        复核说明
     * @return 更新后的复核条目
     * @throws IllegalArgumentException 当复核条目不存在时
     */
    public ReviewEntry confirmReview(String reviewId, String correctedLevel, String reviewer, String comment) {
        ReviewEntry entry = entries.get(reviewId);
        if (entry == null) {
            throw new IllegalArgumentException("review not found: " + reviewId);
        }
        entry.setCorrectedLevel(correctedLevel);
        entry.setReviewer(reviewer == null ? "" : reviewer);
        entry.setComment(comment == null ? "" : comment);
        entry.setStatus(ReviewStatus.CONFIRMED);
        entry.setUpdatedAt(Instant.now().toString());
        return entry;
    }

    /**
     * 导出所有复核条目。
     *
     * @param mask 是否使用 {@link MaskingApi} 对字段值脱敏
     * @return 复核条目列表；返回的是副本，修改不会影响存储中的数据
     */
    public List<ReviewEntry> exportReviews(boolean mask) {
        List<ReviewEntry> result = new ArrayList<>(entries.size());
        for (ReviewEntry entry : entries.values()) {
            ReviewEntry copy = copyEntry(entry);
            if (mask && copy.getFieldValue() != null) {
                copy.setFieldValue(maskingApi.maskValue(copy.getFieldName(), copy.getFieldValue(), "review"));
            }
            result.add(copy);
        }
        return result;
    }

    private ReviewEntry copyEntry(ReviewEntry entry) {
        ReviewEntry copy = new ReviewEntry();
        copy.setReviewId(entry.getReviewId());
        copy.setRecordIndex(entry.getRecordIndex());
        copy.setFieldName(entry.getFieldName());
        copy.setFieldValue(entry.getFieldValue());
        copy.setPredictedLevel(entry.getPredictedLevel());
        copy.setPredictedTags(entry.getPredictedTags() == null ? null : new ArrayList<>(entry.getPredictedTags()));
        copy.setCorrectedLevel(entry.getCorrectedLevel());
        copy.setReviewer(entry.getReviewer());
        copy.setComment(entry.getComment());
        copy.setStatus(entry.getStatus());
        copy.setCreatedAt(entry.getCreatedAt());
        copy.setUpdatedAt(entry.getUpdatedAt());
        return copy;
    }
}
