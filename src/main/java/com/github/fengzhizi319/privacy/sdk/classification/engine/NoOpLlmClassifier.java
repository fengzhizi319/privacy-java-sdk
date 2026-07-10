package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.EngineLayer;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

/**
 * 无操作 LLM 分类器（No-Op LLM Classifier）。
 * <p>
 * 默认实现：当上游最大置信度 &lt; 0.6 时，按上游最高等级或默认等级保守处理，并标记需要人工复核。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class NoOpLlmClassifier implements LlmClassifier {

    @Override
    public FieldClassificationResult classify(String fieldName, String value,
                                              FieldClassificationResult upstream,
                                              SensitivityLevel defaultLevel) {
        if (upstream == null) {
            FieldClassificationResult result = new FieldClassificationResult(fieldName);
            result.setFieldValue(value);
            result.setFinalLevel(defaultLevel == null ? SensitivityLevel.L3 : defaultLevel);
            result.setConfidence(0.0);
            result.setEngineLayer(EngineLayer.LLM);
            result.setNeedsHumanReview(true);
            result.setReasoning("LLM 未启用，无上游结果，按默认等级保守处理");
            return result;
        }

        FieldClassificationResult result = new FieldClassificationResult(fieldName);
        result.setFieldValue(value);
        result.setTags(upstream.getTags());
        result.setEngineLayer(EngineLayer.LLM);

        SensitivityLevel finalLevel = upstream.getFinalLevel() != null ? upstream.getFinalLevel() : defaultLevel;
        double confidence = upstream.getConfidence();

        if (confidence < 0.6) {
            result.setFinalLevel(finalLevel);
            result.setConfidence(confidence);
            result.setNeedsHumanReview(true);
            result.setReasoning("LLM 未启用，按上游最高等级降级/保守处理");
        } else {
            result.setFinalLevel(finalLevel);
            result.setConfidence(confidence);
            result.setNeedsHumanReview(upstream.isNeedsHumanReview());
            result.setReasoning(upstream.getReasoning());
        }
        return result;
    }
}
