# 差分隐私（DP）设计文档

## 1. 概述

差分隐私模块为 Java SDK 提供本地 DP 计算能力，支持 Laplace/Gaussian 噪声机制，覆盖 Count、Sum、Mean、Histogram 等聚合操作，并与 BudgetAccountant 集成实现预算自动管控。

## 2. 设计目标

- 提供标准 DP 机制（Laplace、Gaussian）的 Java 实现。
- 支持基础聚合与高级算子（向量求和、自适应裁剪、分组聚合）。
- 与 BudgetAccountant 无缝集成，每次查询自动消耗预算。
- 纯本地计算，无网络依赖。
- 线程安全，支持并发调用。

## 3. 算法原理

### 3.1 Laplace 机制

对敏感度为 Δf 的查询，注入 Laplace(0, Δf/ε) 噪声：

```
noisy_result = true_result + Lap(Δf / ε)
```

采样公式：
```java
double u = ThreadLocalRandom.current().nextDouble() - 0.5;
double noise = -(scale) * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
```

### 3.2 Gaussian 机制

对 (ε, δ)-DP，注入 N(0, σ²) 噪声：

```
σ = Δf * sqrt(2 * ln(1.25/δ)) / ε
```

### 3.3 敏感度计算

| 查询类型 | 敏感度 |
|---|---|
| Count | 1 |
| Sum（有 clipping [lo, hi]） | max(|lo|, |hi|) |
| Mean | 拆分为 Sum + Count 各自消耗 ε/2 |
| Histogram | 每 bin 敏感度 = 1 |

### 3.4 Clipping

对 Sum 查询，将每个值裁剪到 [clipLower, clipUpper]：

```java
double clipped = Math.max(clipLower, Math.min(clipUpper, value));
```

## 4. 架构设计

```
┌─────────────────────────────────────────────┐
│                  DpApi                       │
├─────────────────────────────────────────────┤
│ - budget: BudgetAccountant                   │
├─────────────────────────────────────────────┤
│ + count(values, ε, δ, mechanism): double     │
│ + count(trueCount, ε, δ, mechanism): double  │
│ + sum(values, ε, δ, mechanism, lo, hi)       │
│ + mean(values, ε, δ, mechanism, lo, hi)      │
│ + histogram(values, cats, ε, δ, mechanism)   │
│ + noisyCount(trueCount, ε, δ, mechanism)     │
│ + noisySum(trueSum, sens, ε, δ, mechanism)   │
│ + noisyMean(sum, count, sens, ε, δ, ...)     │
│ + vectorSum(vectors, ε, δ, mechanism)        │
│ + adaptiveClip(values, ε, δ, mechanism)      │
│ + groupBy(rows, key, val, ε, δ, mechanism)   │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│           BudgetAccountant                   │
│  (自动消耗 ε/δ 预算)                         │
└─────────────────────────────────────────────┘
```

## 5. 噪声生成

### 5.1 Laplace 采样

```java
private double laplaceNoise(double scale) {
    double u = ThreadLocalRandom.current().nextDouble() - 0.5;
    return -scale * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
}
```

### 5.2 Gaussian 采样

```java
private double gaussianNoise(double sigma) {
    return ThreadLocalRandom.current().nextGaussian() * sigma;
}
```

## 6. 预算集成

每次 DP 操作前自动调用 `budget.spend(epsilon, delta)`：

```java
public double count(List<Double> values, double epsilon, double delta, String mechanism) {
    budget.spend(epsilon, delta);  // 预算不足时抛出 BudgetExhaustedException
    double trueCount = values.size();
    return addNoise(trueCount, 1.0, epsilon, delta, mechanism);
}
```

## 7. Mean 的预算拆分

Mean 查询将 ε 拆分为两半：
- ε/2 用于 NoisySum
- ε/2 用于 NoisyCount
- 最终结果 = NoisySum / max(NoisyCount, minCount)

## 8. 模块结构

```
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── api/
│   └── DpApi.java              # DP 核心 API
├── util/
│   ├── BudgetAccountant.java   # 预算管控
│   └── NoiseGenerator.java     # 噪声生成工具
└── PrivacyClient.java          # 入口客户端
```

## 9. 线程安全

- DpApi 无可变状态（预算由 BudgetAccountant 管理）
- 噪声生成使用 ThreadLocalRandom，天然线程安全
- BudgetAccountant 内部使用 synchronized 保护

## 10. 扩展性

- 新增机制：实现 `NoiseGenerator` 接口
- 新增聚合：在 DpApi 中添加方法，复用 addNoise 逻辑
- 高级组合：未来可在 BudgetAccountant 中实现 RDP
