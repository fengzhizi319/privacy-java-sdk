package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.MaskingApi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MaskingApi} 的单元测试。
 * <p>
 * 覆盖手机号、身份证、姓名、邮箱、地址的自动掩码，以及 HMAC 哈希、截断、批量脱敏功能。
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
     * 测试邮箱地址自动掩码规则。
     */
    @Test
    void testMaskEmail() {
        assertEquals("z***@example.com", api.maskValue("email", "zhangsan@example.com", ""));
    }

    /**
     * 测试地址自动掩码规则。
     */
    @Test
    void testMaskAddress() {
        assertEquals("北京市朝阳区***", api.maskValue("home_address", "北京市朝阳区建国路88号", ""));
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

    /**
     * 测试批量脱敏。
     */
    @Test
    void testMaskBatch() {
        List<String> fieldNames = List.of("mobile", "email", "patient_name");
        List<String> values = List.of("13812345678", "test@mail.com", "王五");
        List<String> results = api.maskBatch(fieldNames, values, "");

        assertEquals(3, results.size());
        assertEquals("138****5678", results.get(0));
        assertEquals("t***@mail.com", results.get(1));
        assertEquals("王*", results.get(2));
    }

    /**
     * 测试 map 记录脱敏。
     */
    @Test
    void testMaskRecord() {
        Map<String, Object> record = Map.of(
                "mobile", "13912345678",
                "name", "赵六",
                "age", 30
        );
        Map<String, Object> result = api.maskRecord(record, "");

        assertEquals("139****5678", result.get("mobile"));
        assertEquals("赵*", result.get("name"));
        assertEquals(30, result.get("age")); // 非字符串值保持不变
    }

    /**
     * 测试字段类型识别（email/address 新增类型）。
     */
    @Test
    void testFieldTypeRecognition() {
        // 邮箱关键字识别
        assertEquals("a***@test.org", api.maskValue("user_email_addr", "admin@test.org", ""));
        // 地址关键字识别
        assertEquals("上海市浦东新***", api.maskValue("家庭住址", "上海市浦东新区张江高科技园区", ""));
    }
}
