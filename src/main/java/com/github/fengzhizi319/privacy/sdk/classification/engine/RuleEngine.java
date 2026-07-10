package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationParams;
import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;

import java.util.List;

/**
 * 规则引擎接口（Rule Engine）。
 * <p>
 * 负责基于字段名与字段值执行 Layer 1 规则匹配，返回命中的安全标签列表。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public interface RuleEngine {

    /**
     * 对单个字段执行规则匹配。
     *
     * @param fieldName 字段名称
     * @param value     字段值
     * @param params    分类参数
     * @return 命中的安全标签列表；未命中时返回空列表
     */
    List<SecurityTag> classifyField(String fieldName, Object value, ClassificationParams params);

    /**
     * 获取规则引擎版本。
     *
     * @return 版本字符串
     */
    String getVersion();
}
