package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.DpApi;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DpApi} 的单元测试。
 * <p>
 * 覆盖差分隐私基础算子、高级算子以及预算耗尽场景。
 * </p>
 */
class DpApiTest {

    /** 测试用预算记账本，namespace 为 "test-dp"。 */
    private final BudgetAccountant budget = BudgetAccountant.getInstance("test-dp", 1000.0, 1.0);

    /** 使用固定随机种子的 API 实例，保证测试可复现。 */
    private final DpApi api = new DpApi(budget, new Random(42L));

    /**
     * 测试 count 返回非负且噪声在合理范围内。
     */
    @Test
    void testDpCount() {
        double result = api.count(100L, 1.0, 0.0, "laplace");
        assertTrue(result >= 0);
        assertTrue(Math.abs(result - 100) < 20);
    }

    /**
     * 测试 sum 返回结果与真实求和相差在噪声合理范围内。
     */
    @Test
    void testDpSum() {
        List<Double> values = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        double result = api.sum(values, 1.0, 0.0, "laplace", null, null);
        assertTrue(Math.abs(result - 15.0) < 10.0);
    }

    /**
     * 测试 mean 返回结果与真实均值相差在噪声合理范围内。
     */
    @Test
    void testDpMean() {
        List<Double> values = List.of(10.0, 20.0, 30.0, 40.0);
        double result = api.mean(values, 1.0, 0.0, "laplace", null, null);
        assertTrue(Math.abs(result - 25.0) < 15.0);
    }

    /**
     * 测试预算耗尽时抛出 {@link RuntimeException}。
     */
    @Test
    void testBudgetExhausted() {
        BudgetAccountant smallBudget = BudgetAccountant.getInstance("test-dp-small", 0.01, 1e-6);
        DpApi smallApi = new DpApi(smallBudget);
        assertThrows(RuntimeException.class, () -> smallApi.count(10L, 1.0, 0.0, "laplace"));
    }

    // --- 高级 DP 算子测试 / Advanced DP Primitives Tests ---

    /**
     * 测试差分隐私直方图计数。
     */
    @Test
    void testHistogram() {
        List<String> values = List.of("A", "B", "A", "C", "A", "B");
        List<String> categories = List.of("A", "B", "C");
        Map<String, Double> result = api.histogram(values, categories, 1.0, 1e-5, "laplace");

        assertEquals(3, result.size());
        assertTrue(result.get("A") >= 0);
        assertTrue(result.containsKey("B"));
        assertTrue(result.containsKey("C"));
    }

    /**
     * 测试对已聚合计数注入 DP 噪声。
     */
    @Test
    void testNoisyCount() {
        double result = api.noisyCount(100.0, 1.0, 1e-5, "laplace");
        assertTrue(result >= 0);
    }

    /**
     * 测试对已聚合求和注入 DP 噪声。
     */
    @Test
    void testNoisySum() {
        double result = api.noisySum(500.0, 10.0, 1.0, 1e-5, "laplace");
        assertTrue(Math.abs(result - 500.0) < 100.0);
    }

    /**
     * 测试对已聚合 sum/count 注入 DP 噪声后得到均值。
     */
    @Test
    void testNoisyMean() {
        double result = api.noisyMean(500.0, 100.0, 10.0, 1.0, 1e-5, "laplace", 5.0);
        assertTrue(result > -10 && result < 20);
    }

    /**
     * 测试对已聚合直方图计数注入 DP 噪声。
     */
    @Test
    void testNoisyHistogram() {
        Map<String, Double> trueCounts = Map.of("A", 10.0, "B", 20.0, "C", 5.0);
        Map<String, Double> result = api.noisyHistogram(trueCounts, 1.0, 1e-5, "laplace");

        assertEquals(3, result.size());
        for (double v : result.values()) {
            assertTrue(v >= 0);
        }
    }

    /**
     * 测试高维向量 L2 截断与加噪。
     */
    @Test
    void testVectorSum() {
        List<double[]> vectors = List.of(
                new double[]{1.0, 2.0, 3.0},
                new double[]{4.0, 5.0, 6.0},
                new double[]{7.0, 8.0, 9.0}
        );
        double[] result = api.vectorSum(vectors, 10.0, 1.0, 1e-5, "gaussian");
        assertEquals(3, result.length);
    }

    /**
     * 测试高维向量 DP 均值。
     */
    @Test
    void testVectorMean() {
        List<double[]> vectors = List.of(
                new double[]{1.0, 2.0},
                new double[]{3.0, 4.0},
                new double[]{5.0, 6.0},
                new double[]{7.0, 8.0},
                new double[]{9.0, 10.0}
        );
        double[] result = api.vectorMean(vectors, 10.0, 1.0, 1e-5, "gaussian", 2.0);
        assertEquals(2, result.length);
    }

    /**
     * 测试 DP 自适应截断。
     */
    @Test
    void testAdaptiveClip() {
        List<Double> values = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        double[] result = api.adaptiveClip(values, 1.0, 0.95, 10, 20.0);

        assertEquals(2, result.length);
        assertTrue(result[0] <= result[1], "lower should <= upper");
    }

    /**
     * 测试 Tau-Thresholding DP Group-By。
     */
    @Test
    void testGroupBy() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("city", "Beijing", "amount", 100.0));
        rows.add(Map.of("city", "Beijing", "amount", 200.0));
        rows.add(Map.of("city", "Beijing", "amount", 150.0));
        rows.add(Map.of("city", "Shanghai", "amount", 300.0));
        rows.add(Map.of("city", "Shanghai", "amount", 250.0));
        rows.add(Map.of("city", "Shanghai", "amount", 280.0));

        Map<String, Double> result = api.groupBy(rows, "city", "amount", "sum",
                1.0, 1e-5, 0.0, 500.0, "laplace");

        // 至少应有一个分组通过 Tau 阈值
        assertNotNull(result);
    }
}
