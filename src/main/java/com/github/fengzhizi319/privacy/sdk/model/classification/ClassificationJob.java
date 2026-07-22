package com.github.fengzhizi319.privacy.sdk.model.classification;

/**
 * 异步分类任务（Classification Job）。
 * <p>
 * 记录异步分类任务的 ID、状态、执行结果、错误信息以及起止时间。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ClassificationJob {

    /** 任务 ID。 */
    private String jobId;

    /** 当前任务状态。 */
    private ClassificationJobStatus status;

    /** 执行结果，仅在 {@code status == DONE} 时有值。 */
    private ClassificationResult result;

    /** 错误信息，仅在 {@code status == FAILED} 时有值。 */
    private String error;

    /** 创建时间（ISO-8601）。 */
    private String createdAt;

    /** 完成时间（ISO-8601）。 */
    private String finishedAt;

    public ClassificationJob() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ClassificationJobStatus getStatus() {
        return status;
    }

    public void setStatus(ClassificationJobStatus status) {
        this.status = status;
    }

    public ClassificationResult getResult() {
        return result;
    }

    public void setResult(ClassificationResult result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }
}
