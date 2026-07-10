package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;

import java.util.List;

/**
 * 小型 NER 引擎接口（Small NER Engine）。
 * <p>
 * Layer 2 分类引擎，用于在规则引擎未命中或命中等级较低时识别命名实体。
 * 默认无操作实现返回空列表。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public interface SmallNerEngine {

    /**
     * 对文本执行 NER 识别。
     *
     * @param fieldName 字段名称
     * @param text      待识别文本
     * @return 识别出的安全标签列表
     */
    List<SecurityTag> recognize(String fieldName, String text);
}
