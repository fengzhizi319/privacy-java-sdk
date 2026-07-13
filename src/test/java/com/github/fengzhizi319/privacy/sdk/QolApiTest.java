package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.QolApi;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link QolApi} 的单元测试。
 * <p>
 * 验证查询混淆后列表长度正确且真实查询被包含。
 * </p>
 */
class QolApiTest {

    /** 被测 API 实例。 */
    private final QolApi api = new QolApi();

    /**
     * 测试 obfuscateQuery：虚拟查询数量 + 真实查询 = 总数，且真实查询必然存在于结果中。
     */
    @Test
    void testObfuscateQuery() {
        String real = "糖尿病患者用药趋势";
        List<String> result = api.obfuscateQuery(real, 3, "medical", null, null);
        assertEquals(4, result.size());
        assertTrue(result.contains(real));
    }
}
