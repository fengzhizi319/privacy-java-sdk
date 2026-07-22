package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.CompositeRule;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.RecordClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 复合规则引擎（Composite Rule Engine）。
 * <p>
 * 用于识别“单字段不敏感、多字段组合后敏感”的上下文场景。
 * 在单条记录的字段级分类完成后执行，根据字段名组合升级敏感度等级。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class CompositeRuleEngine {

    /** 默认复合规则列表。 */
    private static final List<CompositeRule> DEFAULT_RULES = List.of(
        new CompositeRule(
            "高敏感个人信息组合",
            List.of("^name$", "id_card|idcard|identity", "mobile|phone|cell"),
            3,
            SensitivityLevel.L5,
            "COMPOSITE_PII_COMBO",
            "COMP_001"
        ),
        new CompositeRule(
            "医疗基因组合",
            List.of("diagnosis|disease|illness", "gene|genomic|mutation|brca|tp53|rs\\d+"),
            2,
            SensitivityLevel.L5,
            "COMPOSITE_MEDICAL_GENOMIC",
            "COMP_002"
        ),
        new CompositeRule(
            "金融账户组合",
            List.of("bank_card|bankcard|card_no|account|credit|transaction"),
            1,
            SensitivityLevel.L4,
            "COMPOSITE_FINANCE_COMBO",
            "COMP_003"
        )
    );

    private final List<CompositeRule> rules;

    public CompositeRuleEngine() {
        this.rules = DEFAULT_RULES;
    }

    public CompositeRuleEngine(List<CompositeRule> rules) {
        this.rules = rules == null || rules.isEmpty() ? DEFAULT_RULES : rules;
    }

    /**
     * 评估单条记录是否命中复合规则。
     *
     * @param record        原始记录
     * @param fieldResults  字段名到字段分类结果的映射
     * @return 命中的安全标签列表
     */
    public List<SecurityTag> evaluate(Map<String, Object> record, Map<String, FieldClassificationResult> fieldResults) {
        List<SecurityTag> tags = new ArrayList<>();
        Set<String> normalizedFields = new HashSet<>();
        for (String name : record.keySet()) {
            normalizedFields.add(normalize(name));
        }

        for (CompositeRule rule : rules) {
            if (rule.getFieldPatterns() == null || rule.getFieldPatterns().isEmpty()) {
                continue;
            }
            int matched = 0;
            for (String pattern : rule.getFieldPatterns()) {
                if (matchesAny(normalizedFields, pattern)) {
                    matched++;
                }
                if (matched >= rule.getMinMatches()) {
                    break;
                }
            }
            if (matched >= rule.getMinMatches()) {
                SecurityTag tag = new SecurityTag(
                    rule.getTargetLevel(),
                    rule.getCategory(),
                    1.0,
                    "COMPOSITE",
                    rule.getRuleId()
                );
                tag.setNeedsHumanReview(rule.getTargetLevel().getRank() >= SensitivityLevel.L5.getRank());
                tags.add(tag);
            }
        }
        return tags;
    }

    /**
     * 将复合规则标签合并到记录结果中并升级最终等级。
     *
     * @param recordResult   记录结果
     * @param compositeTags  复合规则标签
     * @return 更新后的记录结果
     */
    public RecordClassificationResult applyCompositeTags(RecordClassificationResult recordResult, List<SecurityTag> compositeTags) {
        if (compositeTags == null || compositeTags.isEmpty()) {
            return recordResult;
        }

        List<SecurityTag> aggregated = new ArrayList<>(recordResult.getAggregatedTags());
        for (SecurityTag tag : compositeTags) {
            String key = tag.getLevel() + "_" + tag.getCategory();
            boolean exists = aggregated.stream().anyMatch(t -> (t.getLevel() + "_" + t.getCategory()).equals(key));
            if (!exists) {
                aggregated.add(tag);
            }
        }

        SensitivityLevel newLevel = recordResult.getFinalLevel();
        for (SecurityTag tag : compositeTags) {
            newLevel = SensitivityLevel.max(newLevel, tag.getLevel());
        }

        boolean needsReview = recordResult.isNeedsHumanReview();
        for (SecurityTag tag : compositeTags) {
            if (tag.isNeedsHumanReview()) {
                needsReview = true;
                break;
            }
        }

        recordResult.setAggregatedTags(aggregated);
        recordResult.setFinalLevel(newLevel);
        recordResult.setNeedsHumanReview(needsReview);
        return recordResult;
    }

    private String normalize(String name) {
        return str(name).toLowerCase().replace("_", "").replace(" ", "");
    }

    private String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean matchesAny(Set<String> normalizedFields, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }
        try {
            Pattern compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            for (String field : normalizedFields) {
                if (compiled.matcher(field).find()) {
                    return true;
                }
            }
        } catch (PatternSyntaxException e) {
            // fall back to literal contains
            for (String field : normalizedFields) {
                if (field.contains(pattern.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
