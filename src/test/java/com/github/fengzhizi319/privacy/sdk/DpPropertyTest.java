package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.DpApi;
import com.github.fengzhizi319.privacy.sdk.api.LocalDpApi;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 差分隐私算法的属性测试（Property-Based Testing）。
 * <p>
 * 使用 jqwik 框架验证 DP 算法的统计性质：
 * - 无偏性：大量采样后均值趋近真实值
 * - 非负性：计数/直方图结果非负
 * - 维度保持：向量算子输出维度与输入一致
 * - 本地 DP 频率估计收敛性
 * </p>
 */
class DpPropertyTest {

    private DpApi createApi() {
        return new DpApi(BudgetAccountant.getInstance("prop-test-" + System.nanoTime(), 1e6, 1.0));
    }

    /**
     * 属性：NoisyCount 结果始终非负。
     */
    @Property(tries = 100)
    void noisyCountAlwaysNonNegative(
            @ForAll @DoubleRange(min = 0, max = 10000) double trueCount,
            @ForAll @DoubleRange(min = 0.1, max = 10.0) double epsilon) {
        DpApi api = createApi();
        double result = api.noisyCount(trueCount, epsilon, 0.0, "laplace");
        assertTrue(result >= 0, "noisyCount must be non-negative, got " + result);
    }

    /**
     * 属性：NoisySum 在 epsilon 较大时接近真实值（统计无偏）。
     */
    @Property(tries = 50)
    void noisySumUnbiased(
            @ForAll @DoubleRange(min = 100, max = 10000) double trueSum,
            @ForAll @DoubleRange(min = 1.0, max = 5.0) double epsilon) {
        DpApi api = createApi();
        // 多次采样取均值，验证无偏性
        double avg = 0;
        int trials = 200;
        for (int i = 0; i < trials; i++) {
            avg += api.noisySum(trueSum, 1.0, epsilon, 0.0, "laplace");
        }
        avg /= trials;
        // 允许 20% 偏差（统计波动）
        assertTrue(Math.abs(avg - trueSum) / trueSum < 0.2,
                "noisySum average " + avg + " too far from true " + trueSum);
    }

    /**
     * 属性：Histogram 结果桶数等于类别数，且所有值非负。
     */
    @Property(tries = 50)
    void histogramProperties(
            @ForAll @Size(min = 1, max = 5) List<@IntRange(min = 0, max = 2) Integer> indices) {
        DpApi api = createApi();
        List<String> categories = List.of("A", "B", "C");
        List<String> values = new ArrayList<>();
        for (int idx : indices) {
            values.add(categories.get(idx));
        }
        Map<String, Double> result = api.histogram(values, categories, 1.0, 1e-5, "laplace");
        assertEquals(3, result.size(), "histogram must have exactly 3 buckets");
        for (double v : result.values()) {
            assertTrue(v >= 0, "histogram values must be non-negative");
        }
    }

    /**
     * 属性：VectorSum 输出维度与输入向量维度一致。
     */
    @Property(tries = 50)
    void vectorSumDimensionPreserved(
            @ForAll @IntRange(min = 1, max = 10) int dim,
            @ForAll @IntRange(min = 1, max = 20) int numVectors) {
        DpApi api = createApi();
        List<double[]> vectors = new ArrayList<>();
        for (int i = 0; i < numVectors; i++) {
            double[] vec = new double[dim];
            for (int j = 0; j < dim; j++) {
                vec[j] = i + j;
            }
            vectors.add(vec);
        }
        double[] result = api.vectorSum(vectors, 100.0, 1.0, 1e-5, "gaussian");
        assertEquals(dim, result.length, "output dimension must match input");
    }

    /**
     * 属性：本地 DP 二值频率估计在大样本下收敛到真实频率。
     */
    @Property(tries = 10)
    void localDpBinaryEstimateConverges(
            @ForAll @DoubleRange(min = 0.2, max = 0.8) double trueFreq) {
        LocalDpApi ldp = new LocalDpApi();
        int n = 50000;
        List<Integer> values = new ArrayList<>(n);
        int trueOnes = (int) (n * trueFreq);
        for (int i = 0; i < n; i++) {
            values.add(i < trueOnes ? 1 : 0);
        }
        List<Integer> perturbed = ldp.perturbBinaryBatch(values, 2.0);
        double estimate = ldp.estimateBinaryFrequency(perturbed, 2.0);
        // 大样本下估计值应在 ±0.05 内
        assertTrue(Math.abs(estimate - trueFreq) < 0.05,
                "estimate " + estimate + " too far from true freq " + trueFreq);
    }

    /**
     * 属性：AdaptiveClip 返回的 lower <= upper。
     */
    @Property(tries = 30)
    void adaptiveClipOrderPreserved(
            @ForAll @DoubleRange(min = 0.5, max = 5.0) double epsilon,
            @ForAll @DoubleRange(min = 0.8, max = 0.99) double quantile) {
        DpApi api = createApi();
        List<Double> values = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            values.add((double) i);
        }
        double[] result = api.adaptiveClip(values, epsilon, quantile, 10, 200.0);
        assertTrue(result[0] <= result[1],
                "lower " + result[0] + " must <= upper " + result[1]);
    }
}
