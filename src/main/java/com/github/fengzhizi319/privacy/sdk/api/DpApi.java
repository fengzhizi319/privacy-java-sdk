package com.github.fengzhizi319.privacy.sdk.api;

import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

import java.util.List;
import java.util.Random;

/**
 * 差分隐私 API（Differential Privacy API）。
 * <p>
 * 提供计数（count）、求和（sum）、均值（mean）等聚合统计量的加噪能力，支持拉普拉斯（Laplace）与高斯（Gaussian）机制。
 * 并通过 {@link BudgetAccountant} 记录隐私预算消耗。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class DpApi {

    /** 隐私预算记账本，用于追踪 epsilon/delta 消耗。 */
    private final BudgetAccountant budget;

    /** 随机数生成器，用于采样随机噪声。 */
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
     */
    public double count(List<Double> values, double epsilon, double delta, String mechanism) {
        long count = values.stream().filter(v -> v != null).count();
        return count(count, epsilon, delta, mechanism);
    }

    /**
     * 对真实计数值注入噪声。
     */
    public double count(long trueCount, double epsilon, double delta, String mechanism) {
        if (epsilon <= 0) {
            throw new IllegalArgumentException("epsilon must be positive, got " + epsilon);
        }
        String mech = mechanism == null ? "laplace" : mechanism.trim().toLowerCase();
        if (!"laplace".equals(mech) && !"gaussian".equals(mech)) {
            throw new IllegalArgumentException("unsupported mechanism: " + mechanism);
        }
        if ("gaussian".equals(mech) && delta <= 0) {
            throw new IllegalArgumentException("delta must be positive for Gaussian mechanism");
        }

        budget.spend(epsilon, delta);

        double noise;
        if ("laplace".equals(mech)) {
            noise = sampleLaplace(1.0 / epsilon);
        } else {
            double sigma = Math.sqrt(2.0 * Math.log(1.25 / delta)) * 1.0 / epsilon;
            noise = sampleGaussian(sigma);
        }
        return Math.max(0.0, trueCount + noise);
    }

    /**
     * 对列表元素进行差分隐私求和。
     */
    public double sum(List<Double> values, double epsilon, double delta, String mechanism, Double clipLower, Double clipUpper) {
        if (epsilon <= 0) {
            throw new IllegalArgumentException("epsilon must be positive, got " + epsilon);
        }
        String mech = mechanism == null ? "laplace" : mechanism.trim().toLowerCase();
        if (!"laplace".equals(mech) && !"gaussian".equals(mech)) {
            throw new IllegalArgumentException("unsupported mechanism: " + mechanism);
        }
        if ("gaussian".equals(mech) && delta <= 0) {
            throw new IllegalArgumentException("delta must be positive for Gaussian mechanism");
        }

        budget.spend(epsilon, delta);

        double lower = 0;
        double upper = 0;
        if (clipLower != null && clipUpper != null) {
            if (clipLower > clipUpper) {
                throw new IllegalArgumentException("clipLower must be <= clipUpper");
            }
            lower = clipLower;
            upper = clipUpper;
        } else {
            if ("gaussian".equals(mech)) {
                throw new IllegalArgumentException("clipLower and clipUpper are required for Gaussian mechanism");
            }
            if (values != null && !values.isEmpty()) {
                lower = values.stream().mapToDouble(v -> v == null ? 0.0 : v).min().orElse(0.0);
                upper = values.stream().mapToDouble(v -> v == null ? 0.0 : v).max().orElse(0.0);
            }
        }

        double trueSum = 0.0;
        for (Double v : values) {
            double val = v == null ? 0.0 : v;
            if (val < lower) {
                val = lower;
            }
            if (val > upper) {
                val = upper;
            }
            trueSum += val;
        }

        double sensitivity = upper - lower;
        if (sensitivity <= 0) {
            sensitivity = 0.0;
        }

        double noise;
        if ("laplace".equals(mech)) {
            double scale = epsilon > 0 ? sensitivity / epsilon : 0.0;
            noise = sampleLaplace(scale);
        } else {
            if (sensitivity > 0) {
                double sigma = Math.sqrt(2.0 * Math.log(1.25 / delta)) * sensitivity / epsilon;
                noise = sampleGaussian(sigma);
            } else {
                noise = 0.0;
            }
        }
        return trueSum + noise;
    }

    /**
     * 对列表元素进行差分隐私均值估计。
     */
    public double mean(List<Double> values, double epsilon, double delta, String mechanism, Double clipLower, Double clipUpper) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        String mech = mechanism == null ? "laplace" : mechanism.trim().toLowerCase();
        if (!"laplace".equals(mech) && !"gaussian".equals(mech)) {
            throw new IllegalArgumentException("unsupported mechanism: " + mechanism);
        }
        if ("gaussian".equals(mech) && delta <= 0) {
            throw new IllegalArgumentException("delta must be positive for Gaussian mechanism");
        }

        double noisyCount = count(values.size(), epsilon / 2.0, delta / 2.0, mechanism);
        double noisySum = sum(values, epsilon / 2.0, delta / 2.0, mechanism, clipLower, clipUpper);
        return noisyCount > 0 ? noisySum / noisyCount : 0.0;
    }

    /**
     * 从拉普拉斯分布中采样一个随机噪声值。
     */
    private double sampleLaplace(double scale) {
        double u = random.nextDouble() - 0.5;
        return -scale * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
    }

    /**
     * 从高斯分布中采样一个随机噪声值。
     */
    private double sampleGaussian(double sigma) {
        return random.nextGaussian() * sigma;
    }
}
