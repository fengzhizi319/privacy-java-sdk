package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.List;

/**
 * 影子模式差异信息（Shadow Diff）。
 * <p>
 * 记录当前规则集与影子规则集之间的分类差异。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ShadowDiff {

    /** 字段名。 */
    private String fieldName = "";

    /** 记录索引。 */
    private int recordIndex;

    /** 当前规则集的最终等级。 */
    private SensitivityLevel currentLevel;

    /** 影子规则集的最终等级。 */
    private SensitivityLevel shadowLevel;

    /** 当前规则集的标签字符串列表。 */
    private List<String> currentTags;

    /** 影子规则集的标签字符串列表。 */
    private List<String> shadowTags;

    public ShadowDiff() {
    }

    public ShadowDiff(String fieldName, int recordIndex, SensitivityLevel currentLevel,
                      SensitivityLevel shadowLevel, List<String> currentTags, List<String> shadowTags) {
        this.fieldName = fieldName;
        this.recordIndex = recordIndex;
        this.currentLevel = currentLevel;
        this.shadowLevel = shadowLevel;
        this.currentTags = currentTags;
        this.shadowTags = shadowTags;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getRecordIndex() {
        return recordIndex;
    }

    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    public SensitivityLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(SensitivityLevel currentLevel) {
        this.currentLevel = currentLevel;
    }

    public SensitivityLevel getShadowLevel() {
        return shadowLevel;
    }

    public void setShadowLevel(SensitivityLevel shadowLevel) {
        this.shadowLevel = shadowLevel;
    }

    public List<String> getCurrentTags() {
        return currentTags;
    }

    public void setCurrentTags(List<String> currentTags) {
        this.currentTags = currentTags;
    }

    public List<String> getShadowTags() {
        return shadowTags;
    }

    public void setShadowTags(List<String> shadowTags) {
        this.shadowTags = shadowTags;
    }
}
