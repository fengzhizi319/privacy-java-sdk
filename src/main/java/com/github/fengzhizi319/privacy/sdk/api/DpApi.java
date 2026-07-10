package com.github.fengzhizi319.privacy.sdk.api;

import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

import java.util.List;
import java.util.Random;

/**
 * 差分隐私 API（Differential Privacy API）。
 * <p>
 * 提供计数（count）、求和（sum）、均值（mean）等聚合统计量的拉普拉斯噪声注入能力，并通过 {@link BudgetAccountant}
 * 记录隐私预算消耗。当前实现为 (epsilon, 0)-DP 的简化演示，适用于本地原型与测试。
 * </p>
 *
 * <p><b>实现注意点：</b></p>
 * <ul>
 *   <li>所有公开聚合方法都会调用 {@link BudgetAccountant#spend(double, double)}，预算耗尽后抛出 {@code PrivacyBudgetExhaustedException}。</li>
 *   <li>{@code mechanism} 参数当前仅保留扩展用途，实际噪声均由 {@link #sampleLaplace(double)} 生成。</li>
 *   <li>求和与均值的敏感度计算做了简化处理，生产环境应根据数据上下界裁剪（clipping）后计算。</li>
 * </ul>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class DpApi {

    /** 隐私预算记账本，用于追踪 epsilon/delta 消耗。 */
    private final BudgetAccountant budget;

    /** 随机数生成器，用于采样拉普拉斯噪声。 */
    private final Random random;

    /**
     * 使用默认预算构造 API。
     * <p>
     * 默认预算 namespace 为 "default"，epsilon=10.0，delta=1e-4。
     * </p>
     */
    public DpApi() {
        this(BudgetAccountant.getInstance("default", 10.0, 1e-4));
    }

    /**
     * 使用指定预算构造 API，随机数生成器使用默认 {@link Random}。
     *
     * @param budget 隐私预算记账本
     */
    public DpApi(BudgetAccountant budget) {
        this(budget, new Random());
    }

    /**
     * 使用指定预算与指定随机数生成器构造 API。
     *
     * @param budget 隐私预算记账本
     * @param random 随机数生成器，便于测试时注入固定种子
     */
    public DpApi(BudgetAccountant budget, Random random) {
        this.budget = budget;
        this.random = random;
    }

    /**
     * 对非零且非空的列表元素进行差分隐私计数。
     *
     * @param values     待统计的数值列表，{@code null} 或 0.0 会被忽略
     * @param epsilon    隐私预算 epsilon，必须大于 0
     * @param mechanism  噪声机制名称（当前保留，未影响实现）
     * @return 加噪后的计数值，结果不小于 0
     */
    public double count(List<Double> values, double epsilon, String mechanism) {
        return count(values.stream().filter(v -> v != null && v != 0.0).count(), epsilon, mechanism);
    }

    /**
     * 对真实计数值注入拉普拉斯噪声。
     *
     * @param trueCount 真实计数值
     * @param epsilon   隐私预算 epsilon，必须大于 0
     * @param mechanism 噪声机制名称（当前保留，未影响实现）
     * @return 加噪后的计数值，结果不小于 0
     * @throws com.github.fengzhizi319.privacy.sdk.exception.PrivacyBudgetExhaustedException 当预算耗尽时抛出
     */
    public double count(long trueCount, double epsilon, String mechanism) {
        budget.spend(epsilon, 0.0);
        double noise = sampleLaplace(1.0 / epsilon);
        return Math.max(0, trueCount + noise);
    }

    /**
     * 对列表元素进行差分隐私求和。
     * <p>
     * 敏感度按元素绝对值最大值近似计算，未做裁剪处理。
     * </p>
     *
     * @param values     待求和的数值列表，{@code null} 按 0.0 处理
     * @param epsilon    隐私预算 epsilon，必须大于 0
     * @param mechanism  噪声机制名称（当前保留，未影响实现）
     * @return 加噪后的求和结果
     * @throws com.github.fengzhizi319.privacy.sdk.exception.PrivacyBudgetExhaustedException 当预算耗尽时抛出
     */
    public double sum(List<Double> values, double epsilon, String mechanism) {
        budget.spend(epsilon, 0.0);
        double trueSum = values.stream().mapToDouble(v -> v == null ? 0.0 : v).sum();
        double sensitivity = values.stream().mapToDouble(v -> Math.abs(v == null ? 0.0 : v)).max().orElse(1.0);
        double scale = sensitivity / epsilon;
        double noise = sampleLaplace(scale);
        return trueSum + noise;
    }

    /**
     * 对列表元素进行差分隐私均值估计。
     * <p>
     * 使用隐私预算分解策略：一半用于计数，一半用于有界求和；若加噪计数不大于 0 则返回 0.0。
     * </p>
     *
     * @param values     待计算均值的数值列表
     * @param epsilon    总隐私预算 epsilon，必须大于 0
     * @param mechanism  噪声机制名称（当前保留，未影响实现）
     * @return 加噪后的均值估计值
     * @throws com.github.fengzhizi319.privacy.sdk.exception.PrivacyBudgetExhaustedException 当预算耗尽时抛出
     */
    public double mean(List<Double> values, double epsilon, String mechanism) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double min = values.stream().mapToDouble(v -> v == null ? 0.0 : v).min().orElse(0.0);
        double max = values.stream().mapToDouble(v -> v == null ? 0.0 : v).max().orElse(0.0);
        double noisyCount = count(values.size(), epsilon / 2.0, mechanism);
        double noisySum = boundedSum(values, max - min, epsilon / 2.0, mechanism);
        return noisyCount > 0 ? noisySum / noisyCount : 0.0;
    }

    /**
     * 带灵敏度上界的差分隐私求和。
     *
     * @param values      待求和数值列表
     * @param sensitivity 灵敏度估计值
     * @param epsilon     隐私预算 epsilon
     * @param mechanism   噪声机制名称（当前保留，未影响实现）
     * @return 加噪后的求和结果
     * @throws com.github.fengzhizi319.privacy.sdk.exception.PrivacyBudgetExhaustedException 当预算耗尽时抛出
     */
    private double boundedSum(List<Double> values, double sensitivity, double epsilon, String mechanism) {
        budget.spend(epsilon, 0.0);
        double trueSum = values.stream().mapToDouble(v -> v == null ? 0.0 : v).sum();
        double scale = Math.max(1.0, sensitivity) / epsilon;
        double noise = sampleLaplace(scale);
        return trueSum + noise;
    }

    /**
     * 从拉普拉斯分布中采样一个随机噪声值。
     * <p>
     * 使用逆变换采样法：若 U 为 (-0.5, 0.5) 均匀随机变量，则噪声为
     * {@code -scale * sign(U) * ln(1 - 2|U|)}。
     * </p>
     *
     * @param scale 拉普拉斯分布尺度参数 b = sensitivity / epsilon
     * @return 采样得到的噪声值
     */
    private double sampleLaplace(double scale) {
        double u = random.nextDouble() - 0.5;
        return -scale * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
    }
}
