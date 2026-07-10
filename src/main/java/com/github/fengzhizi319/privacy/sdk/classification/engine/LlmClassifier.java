package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

/**
 * LLM 分类器接口（LLM Classifier）。
 * <p>
 * Layer 3 分类引擎，用于在规则引擎与 NER 均未命中或置信度不足时提供兜底分类。
 * 默认无操作实现按上游结果保守处理。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public interface LlmClassifier {

    /**
     * 基于上游字段分类结果执行 LLM 兜底分类。
     *
     * @param fieldName    字段名称
     * @param value        字段值
     * @param upstream     上游（规则 + NER）分类结果
     * @param defaultLevel 默认敏感度等级
     * @return 经 LLM 处理后的字段分类结果
     */
    FieldClassificationResult classify(String fieldName, String value,
                                       FieldClassificationResult upstream,
                                       SensitivityLevel defaultLevel);
}
