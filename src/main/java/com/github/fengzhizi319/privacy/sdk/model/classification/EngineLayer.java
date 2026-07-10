package com.github.fengzhizi319.privacy.sdk.model.classification;

/**
 * 分类引擎层级枚举（Classification Engine Layer）。
 * <p>
 * 对应三层分类引擎：规则引擎（Layer 1）、小型 NER 模型（Layer 2）、LLM 分类器（Layer 3）。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public enum EngineLayer {

    /** 规则引擎层 / Rule-based engine layer。 */
    RULE,

    /** 小型 NER 模型层 / Small NER model layer。 */
    SMALL_NER,

    /** 大语言模型分类层 / LLM classifier layer。 */
    LLM
}
