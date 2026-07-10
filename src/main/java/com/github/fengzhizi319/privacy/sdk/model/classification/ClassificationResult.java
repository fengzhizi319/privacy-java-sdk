package com.github.fengzhizi319.privacy.sdk.model.classification;

/**
 * 分类结果包装器（Classification Result Wrapper）。
 * <p>
 * 可持有记录级结果或表级结果之一，并包含审计信息，用于统一返回给调用方。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ClassificationResult {

    /** 记录级分类结果（与 tableResult 互斥）。 */
    private RecordClassificationResult recordResult;

    /** 表级分类结果（与 recordResult 互斥）。 */
    private TableClassificationResult tableResult;

    /** 审计信息。 */
    private AuditInfo auditInfo;

    /**
     * 默认构造器。
     */
    public ClassificationResult() {
    }

    /**
     * 构造一个记录级分类结果。
     *
     * @param recordResult 记录级结果
     * @param auditInfo    审计信息
     */
    public ClassificationResult(RecordClassificationResult recordResult, AuditInfo auditInfo) {
        this.recordResult = recordResult;
        this.auditInfo = auditInfo;
    }

    /**
     * 构造一个表级分类结果。
     *
     * @param tableResult 表级结果
     * @param auditInfo   审计信息
     */
    public ClassificationResult(TableClassificationResult tableResult, AuditInfo auditInfo) {
        this.tableResult = tableResult;
        this.auditInfo = auditInfo;
    }

    /**
     * 获取当前包装的最终敏感度等级。
     * <p>
     * 优先返回记录结果中的等级，其次表结果中的等级；都不存在时返回 {@code null}。
     * </p>
     *
     * @return 最终敏感度等级
     */
    public SensitivityLevel getFinalLevel() {
        if (recordResult != null) {
            return recordResult.getFinalLevel();
        }
        if (tableResult != null) {
            return tableResult.getFinalLevel();
        }
        return null;
    }

    public RecordClassificationResult getRecordResult() {
        return recordResult;
    }

    public void setRecordResult(RecordClassificationResult recordResult) {
        this.recordResult = recordResult;
    }

    public TableClassificationResult getTableResult() {
        return tableResult;
    }

    public void setTableResult(TableClassificationResult tableResult) {
        this.tableResult = tableResult;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }
}
