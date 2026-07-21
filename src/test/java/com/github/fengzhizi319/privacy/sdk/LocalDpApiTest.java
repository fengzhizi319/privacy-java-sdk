package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.LocalDpApi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link LocalDpApi} 的单元测试。
 * <p>
 * 覆盖二值随机响应、多类别随机响应及对应的无偏频率估计器。
 * </p>
 */
class LocalDpApiTest {

    /** 使用固定随机种子的 API 实例，保证测试可复现。 */
    private final LocalDpApi api = new LocalDpApi(new Random(42L));

    /**
     * 测试二值扰动：大部分值应保留原值（p ≈ 0.73 when ε=1）。
     */
    @Test
    void testPerturbBinaryBatch() {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            values.add(1);
        }
        List<Integer> perturbed = api.perturbBinaryBatch(values, 1.0);

        assertEquals(1000, perturbed.size());
        long count1 = perturbed.stream().filter(v -> v == 1).count();
        // p = e^1/(1+e^1) ≈ 0.73，允许统计波动
        assertTrue(count1 > 600 && count1 < 900,
                "expected ~730 ones, got " + count1);
    }

    /**
     * 测试二值频率估计：估计值应接近真实频率。
     */
    @Test
    void testEstimateBinaryFrequency() {
        // 真实频率为 0.7
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            values.add(i < 7000 ? 1 : 0);
        }
        List<Integer> perturbed = api.perturbBinaryBatch(values, 2.0);
        double estimate = api.estimateBinaryFrequency(perturbed, 2.0);

        assertTrue(Math.abs(estimate - 0.7) < 0.1,
                "expected estimate ~0.7, got " + estimate);
    }

    /**
     * 测试多类别扰动：输出长度正确且值在类别范围内。
     */
    @Test
    void testPerturbCategoricalBatch() {
        List<String> categories = List.of("A", "B", "C");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            values.add(categories.get(i % 3));
        }
        List<String> perturbed = api.perturbCategoricalBatch(values, categories, 2.0);

        assertEquals(3000, perturbed.size());
        for (String p : perturbed) {
            assertTrue(categories.contains(p),
                    "perturbed value '" + p + "' not in categories");
        }
    }

    /**
     * 测试多类别频率估计：各类别频率应接近 1/3。
     */
    @Test
    void testEstimateCategoricalHistogram() {
        List<String> categories = List.of("A", "B", "C");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            values.add(categories.get(i % 3));
        }
        List<String> perturbed = api.perturbCategoricalBatch(values, categories, 2.0);
        Map<String, Double> hist = api.estimateCategoricalHistogram(perturbed, categories, 2.0);

        assertEquals(3, hist.size());
        for (String c : categories) {
            assertTrue(Math.abs(hist.get(c) - 1.0 / 3.0) < 0.15,
                    "expected ~0.33 for " + c + ", got " + hist.get(c));
        }
    }

    /**
     * 测试空输入返回空列表。
     */
    @Test
    void testEmptyInput() {
        List<Integer> emptyBinary = api.perturbBinaryBatch(new ArrayList<>(), 1.0);
        assertTrue(emptyBinary.isEmpty());

        List<String> emptyCategorical = api.perturbCategoricalBatch(new ArrayList<>(), List.of("A"), 1.0);
        assertTrue(emptyCategorical.isEmpty());
    }
}
