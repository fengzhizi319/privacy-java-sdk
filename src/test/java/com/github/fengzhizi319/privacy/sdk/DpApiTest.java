package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.DpApi;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DpApi} 的单元测试。
 * <p>
 * 覆盖差分隐私计数、求和、均值以及预算耗尽场景。
 * </p>
 */
class DpApiTest {

    /** 测试用预算记账本，namespace 为 "test-dp"。 */
    private final BudgetAccountant budget = BudgetAccountant.getInstance("test-dp", 100.0, 1e-4);

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
     * 测试预算耗尽时抛出 {@link RuntimeException}（实际为 {@link com.github.fengzhizi319.privacy.sdk.exception.PrivacyBudgetExhaustedException}）。
     */
    @Test
    void testBudgetExhausted() {
        BudgetAccountant smallBudget = BudgetAccountant.getInstance("test-dp-small", 0.01, 1e-6);
        DpApi smallApi = new DpApi(smallBudget);
        assertThrows(RuntimeException.class, () -> smallApi.count(10L, 1.0, 0.0, "laplace"));
    }
}
