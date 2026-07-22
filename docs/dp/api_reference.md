# 差分隐私（DP）API 参考

## 1. 概述

DP API 通过 `PrivacyClient.dp()` 获取 `DpApi` 实例，或通过 `PrivacyClient` 上的便捷方法直接调用。所有方法为纯本地计算。

## 2. 基础聚合 API

### 2.1 count

对数据集进行带噪声计数。

```java
public double count(List<Double> values, double epsilon, double delta, String mechanism)
public double count(long trueCount, double epsilon, double delta, String mechanism)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| values | List\<Double\> | 数据集 |
| trueCount | long | 已聚合的真实计数 |
| epsilon | double | 本次查询的 ε |
| delta | double | 本次查询的 δ |
| mechanism | String | `"laplace"` 或 `"gaussian"` |

---

### 2.2 sum

对数据集进行带噪声求和，支持 clipping。

```java
public double sum(List<Double> values, double epsilon, double delta, String mechanism, Double clipLower, Double clipUpper)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| values | List\<Double\> | 数据集 |
| clipLower | Double | 裁剪下界（可为 null） |
| clipUpper | Double | 裁剪上界（可为 null） |

---

### 2.3 mean

对数据集进行带噪声均值计算。

```java
public double mean(List<Double> values, double epsilon, double delta, String mechanism, Double clipLower, Double clipUpper)
```

内部将 ε 拆分为 ε/2（Sum）+ ε/2（Count）。

---

### 2.4 histogram

对分类数据进行带噪声直方图统计。

```java
public Map<String, Double> histogram(List<String> values, List<String> categories, double epsilon, double delta, String mechanism)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| values | List\<String\> | 分类数据 |
| categories | List\<String\> | 类别列表 |

## 3. 已聚合值加噪 API

### 3.1 noisyCount

```java
public double noisyCount(double trueCount, double epsilon, double delta, String mechanism)
```

### 3.2 noisySum

```java
public double noisySum(double trueSum, double sensitivity, double epsilon, double delta, String mechanism)
```

### 3.3 noisyMean

```java
public double noisyMean(double trueSum, double trueCount, double sensitivity, double epsilon, double delta, String mechanism, double minCount)
```

## 4. 高级算子 API

### 4.1 vectorSum

对向量列表按维度求和后加噪。

```java
public double[] vectorSum(List<double[]> vectors, double epsilon, double delta, String mechanism)
```

### 4.2 adaptiveClip

自适应裁剪后求和加噪。

```java
public double adaptiveClip(List<Double> values, double epsilon, double delta, String mechanism)
```

### 4.3 groupBy

分组聚合后对每组加噪。

```java
public Map<String, Double> groupBy(List<Map<String, Object>> rows, String keyCol, String valCol, double epsilon, double delta, String mechanism)
```

## 5. 便捷方法

`PrivacyClient` 上的便捷方法：

| 方法 | 等价调用 |
|---|---|
| `client.dpCount(values, ε, δ, mechanism)` | `client.dp().count(values, ε, δ, mechanism)` |
| `client.dpSum(values, ε, δ, mechanism, lo, hi)` | `client.dp().sum(...)` |
| `client.dpMean(values, ε, δ, mechanism, lo, hi)` | `client.dp().mean(...)` |
| `client.dpHistogram(values, cats, ε, δ, mechanism)` | `client.dp().histogram(...)` |
| `client.dpNoisyCount(count, ε, δ, mechanism)` | `client.dp().noisyCount(...)` |
| `client.dpNoisySum(sum, sens, ε, δ, mechanism)` | `client.dp().noisySum(...)` |
| `client.dpNoisyMean(sum, count, sens, ε, δ, mechanism, minCount)` | `client.dp().noisyMean(...)` |

## 6. 异常

| 异常 | 触发条件 |
|---|---|
| `BudgetExhaustedException` | 预算不足 |
| `IllegalArgumentException` | epsilon ≤ 0 或 mechanism 无效 |

## 7. 线程安全

所有 DpApi 方法为线程安全，可在多线程环境中并发调用。
