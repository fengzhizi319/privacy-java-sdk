package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PrivacyClient 端到端集成测试。
 * <p>
 * 验证完整调用链：Builder 构建 → 各原语协同工作 → 预算耗尽 → 异常处理。
 * </p>
 */
class PrivacyClientIntegrationTest {

    /**
     * 测试 Builder 模式构建客户端并执行完整隐私处理流水线。
     */
    @Test
    void testBuilderAndFullPipeline() {
        PrivacyClient client = PrivacyClient.builder()
                .namespace("integration-test")
                .epsilon(100.0)
                .delta(1.0)
                .build();

        // 1. 脱敏
        String masked = client.maskValue("mobile", "13812345678", "");
        assertEquals("138****5678", masked);

        // 2. 差分隐私
        double noisyCount = client.dpCount(1000L, 1.0, 0.0, "laplace");
        assertTrue(noisyCount >= 0);

        // 3. 查询混淆
        List<String> obfuscated = client.obfuscateQuery("高血压饮食建议", 3, "medical", null, null);
        assertEquals(4, obfuscated.size());
        assertTrue(obfuscated.contains("高血压饮食建议"));

        // 4. 本地 DP
        List<Integer> perturbed = client.perturbBinaryBatch(List.of(1, 1, 0, 1, 0), 1.0);
        assertEquals(5, perturbed.size());

        // 5. 批量脱敏
        List<String> batchResult = client.maskBatch(
                List.of("email", "name"),
                List.of("test@example.com", "张三"),
                ""
        );
        assertEquals(2, batchResult.size());
        assertEquals("t***@example.com", batchResult.get(0));

        // 6. 预算查询
        Map<String, Double> remaining = client.budgetRemaining();
        assertTrue(remaining.get("epsilon") < 100.0);
        assertTrue(remaining.get("epsilon") > 0);
    }

    /**
     * 测试预算耗尽后抛出异常。
     */
    @Test
    void testBudgetExhaustion() {
        PrivacyClient client = PrivacyClient.builder()
                .namespace("integration-exhaust")
                .epsilon(0.5)
                .delta(1e-6)
                .build();

        // 第一次消耗 0.3，成功
        assertDoesNotThrow(() -> client.dpNoisyCount(10.0, 0.3, 0.0, "laplace"));

        // 第二次消耗 0.3，总计 0.6 > 0.5，应抛出异常
        assertThrows(RuntimeException.class,
                () -> client.dpNoisyCount(10.0, 0.3, 0.0, "laplace"));
    }

    /**
     * 测试 DP 高级算子完整链路。
     */
    @Test
    void testAdvancedDpPipeline() {
        PrivacyClient client = PrivacyClient.builder()
                .namespace("integration-adv-dp")
                .epsilon(500.0)
                .delta(1.0)
                .build();

        // Histogram
        Map<String, Double> hist = client.dpHistogram(
                List.of("A", "B", "A", "A"), List.of("A", "B"), 1.0, 1e-5, "laplace");
        assertEquals(2, hist.size());

        // VectorSum
        double[] vecSum = client.dpVectorSum(
                List.of(new double[]{1, 2}, new double[]{3, 4}), 10.0, 1.0, 1e-5, "gaussian");
        assertEquals(2, vecSum.length);

        // GroupBy
        List<Map<String, Object>> rows = List.of(
                Map.of("city", "BJ", "amount", 100.0),
                Map.of("city", "BJ", "amount", 200.0),
                Map.of("city", "SH", "amount", 300.0)
        );
        Map<String, Double> grouped = client.dpGroupBy(
                rows, "city", "amount", "sum", 1.0, 1e-5, 0.0, 500.0, "laplace");
        assertNotNull(grouped);
    }

    /**
     * 测试 MaskRecord 与 Classify 协同。
     */
    @Test
    void testMaskAndClassifyPipeline() {
        PrivacyClient client = PrivacyClient.builder()
                .namespace("integration-mask-classify")
                .epsilon(100.0)
                .delta(1.0)
                .build();

        Map<String, Object> record = Map.of(
                "mobile", "13812345678",
                "id_card", "110105199001011234",
                "age", 30
        );
        Map<String, Object> masked = client.maskRecord(record, "");
        assertEquals("138****5678", masked.get("mobile"));
        assertEquals("110105********1234", masked.get("id_card"));
        assertEquals(30, masked.get("age"));
    }
}
