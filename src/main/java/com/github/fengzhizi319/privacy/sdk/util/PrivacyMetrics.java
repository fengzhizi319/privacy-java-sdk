package com.github.fengzhizi319.privacy.sdk.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 隐私操作指标收集器（Privacy Metrics Collector）。
 * <p>
 * 基于 Micrometer 实现可选的操作指标收集，支持：
 * <ul>
 *   <li>操作计数（按原语类型标签）</li>
 *   <li>操作耗时（Timer）</li>
 *   <li>预算消耗追踪</li>
 * </ul>
 * </p>
 * <p>
 * 当未配置 {@link MeterRegistry} 时，所有方法为空操作（no-op），不会引入运行时开销。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用 SimpleMeterRegistry 或 Spring 注入的 MeterRegistry
 * MeterRegistry registry = new SimpleMeterRegistry();
 * PrivacyMetrics metrics = new PrivacyMetrics(registry);
 *
 * // 记录 DP 操作
 * double result = metrics.time("dp", "noisySum", () -> dpApi.noisySum(values, ...));
 *
 * // 记录预算消耗
 * metrics.recordBudgetConsumed("dp", 0.1);
 * }</pre>
 *
 * @author fengzhizi319
 * @since 0.2.0
 */
public final class PrivacyMetrics {

    private static final Logger log = LoggerFactory.getLogger(PrivacyMetrics.class);

    /** 指标名称前缀。 */
    private static final String PREFIX = "privacy.";

    /** Micrometer 注册表，为 null 时禁用指标。 */
    private final MeterRegistry registry;

    /**
     * 创建禁用指标的实例（no-op）。
     */
    public PrivacyMetrics() {
        this(null);
    }

    /**
     * 创建启用指标的实例。
     *
     * @param registry Micrometer 注册表，为 null 时禁用指标
     */
    public PrivacyMetrics(MeterRegistry registry) {
        this.registry = registry;
        if (registry != null) {
            log.info("Privacy metrics enabled");
        }
    }

    /**
     * 判断指标是否启用。
     *
     * @return 启用时返回 {@code true}
     */
    public boolean isEnabled() {
        return registry != null;
    }

    /**
     * 记录操作计数。
     *
     * @param primitive 原语类型（dp/masking/k anonymity/qol/classification/local_dp）
     * @param operation 操作名称（如 noisySum、maskValue）
     */
    public void count(String primitive, String operation) {
        if (registry == null) {
            return;
        }
        Counter.builder(PREFIX + "operations")
                .tag("primitive", primitive)
                .tag("operation", operation)
                .description("Privacy operation count")
                .register(registry)
                .increment();
    }

    /**
     * 记录操作计数（带结果状态）。
     *
     * @param primitive 原语类型
     * @param operation 操作名称
     * @param success   是否成功
     */
    public void count(String primitive, String operation, boolean success) {
        if (registry == null) {
            return;
        }
        Counter.builder(PREFIX + "operations")
                .tag("primitive", primitive)
                .tag("operation", operation)
                .tag("status", success ? "success" : "failure")
                .description("Privacy operation count")
                .register(registry)
                .increment();
    }

    /**
     * 测量操作耗时并执行。
     *
     * @param primitive 原语类型
     * @param operation 操作名称
     * @param supplier  要执行的操作
     * @param <T>       返回值类型
     * @return 操作结果
     */
    public <T> T time(String primitive, String operation, Supplier<T> supplier) {
        if (registry == null) {
            return supplier.get();
        }
        Timer timer = Timer.builder(PREFIX + "operation.duration")
                .tag("primitive", primitive)
                .tag("operation", operation)
                .description("Privacy operation duration")
                .register(registry);
        return timer.record(supplier);
    }

    /**
     * 记录操作耗时。
     *
     * @param primitive 原语类型
     * @param operation 操作名称
     * @param duration  耗时
     */
    public void recordDuration(String primitive, String operation, Duration duration) {
        if (registry == null) {
            return;
        }
        Timer.builder(PREFIX + "operation.duration")
                .tag("primitive", primitive)
                .tag("operation", operation)
                .description("Privacy operation duration")
                .register(registry)
                .record(duration);
    }

    /**
     * 记录预算消耗量。
     *
     * @param primitive 原语类型
     * @param epsilon   消耗的 epsilon 值
     */
    public void recordBudgetConsumed(String primitive, double epsilon) {
        if (registry == null) {
            return;
        }
        Counter.builder(PREFIX + "budget.consumed")
                .tag("primitive", primitive)
                .description("Privacy budget consumed (epsilon)")
                .register(registry)
                .increment(epsilon);
    }

    /**
     * 记录预算耗尽事件。
     *
     * @param namespace 预算命名空间
     */
    public void recordBudgetExhausted(String namespace) {
        if (registry == null) {
            return;
        }
        Counter.builder(PREFIX + "budget.exhausted")
                .tag("namespace", namespace)
                .description("Privacy budget exhausted event")
                .register(registry)
                .increment();
    }
}
