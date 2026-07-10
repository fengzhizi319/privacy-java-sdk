package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.MaskingApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MaskingApi} 的单元测试。
 * <p>
 * 覆盖手机号、身份证、姓名的自动掩码，以及 HMAC 哈希与截断功能。
 * </p>
 */
class MaskingApiTest {

    /** 被测 API 实例。 */
    private final MaskingApi api = new MaskingApi();

    /**
     * 测试手机号自动掩码规则。
     */
    @Test
    void testMaskMobile() {
        assertEquals("138****5678", api.maskValue("mobile", "13812345678", "doctor_query"));
    }

    /**
     * 测试身份证号自动掩码规则。
     */
    @Test
    void testMaskIdCard() {
        assertEquals("110105********1234", api.maskValue("id_card", "110105199001011234", "doctor_query"));
    }

    /**
     * 测试姓名自动掩码规则。
     */
    @Test
    void testMaskName() {
        assertEquals("张**丰", api.maskValue("patient_name", "张三丰", "doctor_query"));
    }

    /**
     * 测试 HMAC-SHA256 哈希的确定性、盐敏感性与输出长度。
     */
    @Test
    void testHashValue() {
        String h1 = api.hashValue("13812345678", "salt-a");
        String h2 = api.hashValue("13812345678", "salt-a");
        String h3 = api.hashValue("13812345678", "salt-b");
        assertEquals(h1, h2);
        assertNotEquals(h1, h3);
        assertEquals(16, h1.length());
    }

    /**
     * 测试字符串截断保留前缀并追加 "***"。
     */
    @Test
    void testTruncate() {
        assertEquals("深圳市南山区***", api.truncate("深圳市南山区科技南十二路18号", 6));
    }
}
