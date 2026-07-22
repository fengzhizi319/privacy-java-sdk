# 差分隐私运维手册

## 1. 概述

DP 模块为纯本地计算，无外部依赖。运维重点在于 ε/δ 参数选择、预算管控与结果质量评估。

## 2. 参数选择指南

### 2.1 Epsilon 选择

| ε 值 | 隐私保护强度 | 噪声量 | 适用场景 |
|---|---|---|---|
| 0.01-0.1 | 极强 | 极大 | 高敏感医疗/金融数据 |
| 0.1-1.0 | 强 | 大 | 一般隐私保护 |
| 1.0-5.0 | 中 | 中 | 内部分析 |
| 5.0-10.0 | 弱 | 小 | 低敏感统计 |

### 2.2 Delta 选择

- 推荐值：1/N（N 为数据集大小）
- 典型范围：1e-4 ~ 1e-6
- 必须满足 δ < 1/N

### 2.3 机制选择

| 机制 | 特点 | 适用场景 |
|---|---|---|
| Laplace | 尖峰厚尾，小噪声概率高 | 通用场景 |
| Gaussian | 尾部衰减快，极端噪声少 | 需要 (ε,δ)-DP 的场景 |

### 2.4 Clipping 设置

- 根据业务数据分布确定合理范围
- 过窄：信息损失大，结果偏差大
- 过宽：敏感度大，噪声大
- 建议：覆盖 95% 数据的范围

## 3. 预算管理

### 3.1 预算分配策略

```java
// 按业务分配独立 namespace
PrivacyClient searchClient = PrivacyClient.builder()
    .namespace("search").epsilon(10.0).delta(1e-3).build();

PrivacyClient analyticsClient = PrivacyClient.builder()
    .namespace("analytics").epsilon(20.0).delta(1e-3).build();
```

### 3.2 预算监控

```java
BudgetAccountant budget = client.budget();
double remaining = budget.getRemainingEpsilon();
double total = budget.getTotalEpsilon();
double usageRatio = 1.0 - remaining / total;

if (usageRatio > 0.9) {
    log.warn("隐私预算使用率超过 90%: {}/{}", total - remaining, total);
}
```

## 4. 性能特征

| 操作 | 时间复杂度 | 说明 |
|---|---|---|
| Count | O(1) | 仅计数 |
| Sum | O(N) | 遍历 + clipping |
| Mean | O(N) | Sum + Count |
| Histogram | O(N + C) | C=类别数 |
| GroupBy | O(N) | 分组 + 加噪 |

## 5. 结果质量评估

### 5.1 信噪比

```java
// 评估 DP 结果可用性
double trueValue = 1000.0;
double noisyValue = client.dpNoisyCount(trueValue, 1.0, 1e-5, "laplace");
double relativeError = Math.abs(noisyValue - trueValue) / trueValue;
System.out.printf("相对误差: %.2f%%%n", relativeError * 100);
```

### 5.2 多次查询一致性

同一查询多次执行结果应波动但趋势一致：

```java
for (int i = 0; i < 10; i++) {
    double result = client.dpNoisyCount(100, 1.0, 1e-5, "laplace");
    System.out.printf("第 %d 次: %.2f%n", i + 1, result);
}
```

## 6. 故障排查

| 问题 | 原因 | 解决方案 |
|---|---|---|
| 结果偏差极大 | ε 过小 | 增大 ε 或减少查询次数 |
| BudgetExhaustedException | 预算耗尽 | Reset 或增大初始预算 |
| 结果始终为负 | 真实值小 + 噪声大 | 增大 ε 或对结果取 max(0, x) |
| IllegalArgumentException | 参数非法 | 检查 ε > 0, mechanism 有效 |

## 7. 最佳实践

1. **先确定隐私需求再选参数**：合规要求决定 ε 上界
2. **合理使用 clipping**：限制敏感度是降低噪声的关键
3. **缓存查询结果**：相同查询不要重复消耗预算
4. **批量查询合并**：一次 Histogram 优于多次 Count
5. **监控预算水位**：在耗尽前规划重置
6. **结果后处理**：对计数取 max(0, x)，对比例裁剪到 [0, 1]
