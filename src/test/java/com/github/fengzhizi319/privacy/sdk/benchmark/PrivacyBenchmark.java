package com.github.fengzhizi319.privacy.sdk.benchmark;

import com.github.fengzhizi319.privacy.sdk.api.DpApi;
import com.github.fengzhizi319.privacy.sdk.api.LocalDpApi;
import com.github.fengzhizi319.privacy.sdk.api.MaskingApi;
import com.github.fengzhizi319.privacy.sdk.api.QolApi;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 隐私原语性能基准测试（Privacy Primitives JMH Benchmark）。
 * <p>
 * 测量各隐私原语在不同数据规模下的吞吐量与延迟。
 * </p>
 *
 * <h3>运行方式</h3>
 * <pre>{@code
 * // 运行所有基准测试
 * mvn test-compile
 * java -cp target/test-classes:target/classes:$(mvn dependency:build-classpath -q -DincludeScope=test -Dmdep.outputFile=/dev/stdout) \
 *     com.github.fengzhizi319.privacy.sdk.benchmark.PrivacyBenchmark
 *
 * // 或在 IDE 中直接运行 main 方法
 * }</pre>
 *
 * @author fengzhizi319
 * @since 0.2.0
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PrivacyBenchmark {

    private DpApi dpApi;
    private LocalDpApi localDpApi;
    private MaskingApi maskingApi;
    private QolApi qolApi;

    private List<String> smallCategories;
    private List<String> categories;

    @Setup(Level.Trial)
    public void setup() {
        // 使用独立命名空间避免预算耗尽
        dpApi = new DpApi(BudgetAccountant.getInstance("benchmark", 1e9, 1.0));
        localDpApi = new LocalDpApi();
        maskingApi = new MaskingApi();
        qolApi = new QolApi();

        categories = List.of("A", "B", "C", "D", "E");
        smallCategories = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            smallCategories.add(categories.get(i % categories.size()));
        }
    }

    // ==================== DP Benchmarks ====================

    @Benchmark
    public void dpNoisySum(Blackhole bh) {
        // noisySum takes pre-computed sum
        bh.consume(dpApi.noisySum(5000.0, 1.0, 1.0, 1e-5, "laplace"));
    }

    @Benchmark
    public void dpNoisyMean(Blackhole bh) {
        // noisyMean(trueSum, trueCount, sensitivity, epsilon, delta, mechanism, minCount)
        bh.consume(dpApi.noisyMean(5000.0, 100.0, 1.0, 1.0, 1e-5, "laplace", 1.0));
    }

    @Benchmark
    public void dpHistogram(Blackhole bh) {
        bh.consume(dpApi.histogram(smallCategories, categories, 1.0, 1e-5, "laplace"));
    }

    @Benchmark
    public void dpVectorSum(Blackhole bh) {
        List<double[]> vectors = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            vectors.add(new double[]{i, i * 2.0, i * 3.0});
        }
        bh.consume(dpApi.vectorSum(vectors, 1000.0, 1.0, 1e-5, "laplace"));
    }

    // ==================== Local DP Benchmarks ====================

    @Benchmark
    public void localDpBinaryBatch(Blackhole bh) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            values.add(i % 2);
        }
        bh.consume(localDpApi.perturbBinaryBatch(values, 1.0));
    }

    @Benchmark
    public void localDpCategoricalBatch(Blackhole bh) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            values.add(categories.get(i % categories.size()));
        }
        bh.consume(localDpApi.perturbCategoricalBatch(values, categories, 1.0));
    }

    // ==================== Masking Benchmarks ====================

    @Benchmark
    public void maskingMobile(Blackhole bh) {
        bh.consume(maskingApi.maskValue("mobile", "13812345678", ""));
    }

    @Benchmark
    public void maskingIdCard(Blackhole bh) {
        bh.consume(maskingApi.maskValue("id_card", "110101199001011234", ""));
    }

    @Benchmark
    public void maskingBatch(Blackhole bh) {
        List<String> fieldNames = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            fieldNames.add("mobile");
            values.add("13812345678");
        }
        bh.consume(maskingApi.maskBatch(fieldNames, values, ""));
    }

    // ==================== QoL Benchmarks ====================

    @Benchmark
    public void qolObfuscate(Blackhole bh) {
        bh.consume(qolApi.obfuscateQuery("感冒", 3, "medical", null, null));
    }

    @Benchmark
    public void qolObfuscateWithDetails(Blackhole bh) {
        bh.consume(qolApi.obfuscateQueryWithDetails("感冒", 3, "medical", null, null));
    }

    // ==================== Runner ====================

    /**
     * 运行基准测试的入口。
     *
     * @param args 命令行参数
     * @throws RunnerException 运行异常
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PrivacyBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
