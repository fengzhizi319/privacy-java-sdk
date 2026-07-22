package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.List;

/**
 * 复合规则定义（Composite Rule Definition）。
 * <p>
 * 当记录中同时命中 {@code minMatches} 个字段模式时，将记录敏感度升级为 {@code targetLevel}，
 * 并附加 {@code category} 标签。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class CompositeRule {

    /** 规则名称。 */
    private String name = "";

    /** 字段模式列表，支持正则表达式。 */
    private List<String> fieldPatterns;

    /** 最少命中字段数。 */
    private int minMatches;

    /** 命中后升级到的目标等级。 */
    private SensitivityLevel targetLevel;

    /** 附加标签类别。 */
    private String category = "COMPOSITE";

    /** 规则 ID。 */
    private String ruleId = "COMPOSITE_001";

    public CompositeRule() {
    }

    public CompositeRule(String name, List<String> fieldPatterns, int minMatches,
                         SensitivityLevel targetLevel, String category, String ruleId) {
        this.name = name;
        this.fieldPatterns = fieldPatterns;
        this.minMatches = minMatches;
        this.targetLevel = targetLevel;
        this.category = category;
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFieldPatterns() {
        return fieldPatterns;
    }

    public void setFieldPatterns(List<String> fieldPatterns) {
        this.fieldPatterns = fieldPatterns;
    }

    public int getMinMatches() {
        return minMatches;
    }

    public void setMinMatches(int minMatches) {
        this.minMatches = minMatches;
    }

    public SensitivityLevel getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(SensitivityLevel targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
}
