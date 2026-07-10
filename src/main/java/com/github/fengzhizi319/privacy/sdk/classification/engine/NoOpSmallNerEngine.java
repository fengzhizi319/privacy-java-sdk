package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;

import java.util.Collections;
import java.util.List;

/**
 * 无操作 Small NER 引擎（No-Op Small NER Engine）。
 * <p>
 * 默认实现，始终返回空列表，不引入外部模型依赖。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class NoOpSmallNerEngine implements SmallNerEngine {

    @Override
    public List<SecurityTag> recognize(String fieldName, String text) {
        return Collections.emptyList();
    }
}
