package com.github.fengzhizi319.privacy.sdk.model.classification;

/**
 * 异步分类任务状态枚举（Classification Job Status）。
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public enum ClassificationJobStatus {

    /** 等待执行。 */
    PENDING,

    /** 正在执行。 */
    RUNNING,

    /** 执行成功。 */
    DONE,

    /** 执行失败。 */
    FAILED
}
