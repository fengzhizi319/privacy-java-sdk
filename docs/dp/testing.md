# 差分隐私测试策略

## 1. 测试概览

DP 模块测试覆盖噪声机制正确性、聚合操作、预算集成及边界条件。

| 测试类型 | 覆盖范围 | 文件 |
|---|---|---|
| 单元测试 | 噪声生成、聚合计算 | `DpApiTest.java` |
| 统计测试 | 噪声分布验证 | `DpApiTest.java` |
| 集成测试 | 预算消耗联动 | `DpApiTest.java` |
| 边界测试 | 空数据、极端参数 | `DpApiTest.java` |

## 2. 噪声机制测试

```java
@Test
void testLaplaceNoiseCenteredAroundZero() {
    DpApi dp = new DpApi(BudgetAccountant.getInstance("test", 1000.0, 1.0));
    double sum = 0;
    int n = 10000;
    for (int i = 0; i < n; i++) {
        sum += dp.noisyCount(0, 1.0, 1e-5, "laplace");
    }
    double mean = sum / n;
    // 均值应接近 0（真实值为 0）
    assertThat(Math.abs(mean)).isLessThan(0.5);
}

@Test
void testGaussianNoiseCenteredAroundZero() {
    DpApi dp = new DpApi(BudgetAccountant.getInstance("test-g", 1000.0, 1.0));
    double sum = 0;
    int n = 10000;
    for (int i = 0; i < n; i++) {
        sum += dp.noisyCount(0, 1.0, 1e-5, "gaussian");
    }
    double mean = sum / n;
    assertThat(Math.abs(mean)).isLessThan(0.5);
}
```

## 3. 聚合操作测试

```java
@Test
void testCountReturnsNearTrueValue() {
    PrivacyClient client = new PrivacyClient();
    List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);

    double result = client.dpCount(values, 1.0, 1e-5, "laplace");
    // 高 ε 下结果应接近真实值 5
    assertThat(result).isBetween(3.0, 7.0);
}

@Test
void testSumWithClipping() {
    PrivacyClient client = new PrivacyClient();
    List<Double> values = Arrays.asList(10.0, 20.0, 100.0); // 100 会被裁剪

    double result = client.dpSum(values, 1.0, 1e-5, "laplace", 0.0, 50.0);
    // 裁剪后真实和 = 10+20+50 = 80
    assertThat(result).isBetween(60.0, 100.0);
}

@Test
void testMeanCalculation() {
    PrivacyClient client = new PrivacyClient();
    List<Double> values = Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0);

    double result = client.dpMean(values, 2.0, 1e-5, "laplace", 0.0, 100.0);
    // 真实均值 = 30
    assertThat(result).isBetween(20.0, 40.0);
}

@Test
void testHistogram() {
    PrivacyClient client = new PrivacyClient();
    List<String> values = Arrays.asList("A", "A", "A", "B", "B", "C");
    List<String> categories = Arrays.asList("A", "B", "C");

    Map<String, Double> hist = client.dpHistogram(values, categories, 1.0, 1e-5, "laplace");
    assertThat(hist).containsKeys("A", "B", "C");
    assertThat(hist.get("A")).isGreaterThan(hist.get("C"));
}
```

## 4. 预算集成测试

```java
@Test
void testBudgetExhaustion() {
    PrivacyClient client = PrivacyClient.builder()
        .namespace("exhaust-test")
        .epsilon(2.0)
        .delta(1e-4)
        .build();

    List<Double> values = Arrays.asList(1.0, 2.0, 3.0);

    // 前两次成功
    assertThatNoException().isThrownBy(() -> client.dpCount(values, 1.0, 1e-5, "laplace"));
    assertThatNoException().isThrownBy(() -> client.dpCount(values, 1.0, 1e-5, "laplace"));

    // 第三次应失败
    assertThatThrownBy(() -> client.dpCount(values, 1.0, 1e-5, "laplace"))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("exhausted");
}
```

## 5. 边界测试

```java
@Test
void testEmptyValues() {
    PrivacyClient client = new PrivacyClient();
    double result = client.dpCount(Collections.emptyList(), 1.0, 1e-5, "laplace");
    // 真实计数为 0，结果应在 0 附近
    assertThat(Math.abs(result)).isLessThan(5.0);
}

@Test
void testInvalidEpsilon() {
    PrivacyClient client = new PrivacyClient();
    assertThatThrownBy(() -> client.dpCount(Arrays.asList(1.0), 0.0, 1e-5, "laplace"))
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void testInvalidMechanism() {
    PrivacyClient client = new PrivacyClient();
    assertThatThrownBy(() -> client.dpCount(Arrays.asList(1.0), 1.0, 1e-5, "invalid"))
        .isInstanceOf(IllegalArgumentException.class);
}
```

## 6. 运行测试

```bash
# Maven
mvn test -Dtest=DpApiTest

# Gradle
./gradlew test --tests "*.DpApiTest"
```

## 7. 测试检查清单

- [ ] Laplace 噪声均值接近 0
- [ ] Gaussian 噪声均值接近 0
- [ ] Count 结果在合理范围内
- [ ] Sum clipping 正确生效
- [ ] Mean 内部预算拆分正确
- [ ] Histogram 各类别独立加噪
- [ ] 预算耗尽时抛出异常
- [ ] 空数据集不崩溃
- [ ] 非法参数抛出 IllegalArgumentException
- [ ] 并发调用线程安全
