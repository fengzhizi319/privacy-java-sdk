# 差分隐私（DP）模块

提供 Laplace 与 Gaussian 两种噪声机制，支持 Count、Sum、Mean、Histogram 等基础聚合查询，以及向量求和、自适应裁剪、分组聚合等高级算子。与 BudgetAccountant 集成实现隐私预算自动管控。

## 文档索引

| 文档 | 说明 |
|---|---|
| [产品需求](prd.md) | 功能需求与验收标准 |
| [设计文档](design.md) | 算法原理与架构设计 |
| [API 参考](api_reference.md) | 接口签名与参数说明 |
| [使用示例](examples.md) | 典型场景代码示例 |
| [测试策略](testing.md) | 测试方法与检查清单 |
| [运维手册](ops.md) | 参数选择与故障排查 |

## 快速开始

### 基本计数

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

PrivacyClient client = PrivacyClient.builder()
    .namespace("my-app")
    .epsilon(10.0)
    .delta(1e-4)
    .build();

List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
double noisyCount = client.dpCount(values, 1.0, 1e-5, "laplace");
```

### 带 Clipping 的求和

```java
double noisySum = client.dpSum(values, 1.0, 1e-5, "laplace", 0.0, 100.0);
```

### 均值

```java
double noisyMean = client.dpMean(values, 1.0, 1e-5, "laplace", 0.0, 100.0);
```

## API 概览

| 方法 | 说明 |
|---|---|
| `dp().count(...)` | 带噪声计数 |
| `dp().sum(...)` | 带噪声求和 |
| `dp().mean(...)` | 带噪声均值 |
| `dp().histogram(...)` | 带噪声直方图 |
| `dp().noisyCount(...)` | 对已聚合计数加噪 |
| `dp().noisySum(...)` | 对已聚合求和加噪 |
| `dp().noisyMean(...)` | 对已聚合均值加噪 |
| `dp().vectorSum(...)` | 向量维度求和加噪 |
| `dp().adaptiveClip(...)` | 自适应裁剪 |
| `dp().groupBy(...)` | 分组聚合加噪 |

## 支持的噪声机制

| 机制 | 参数 | 说明 |
|---|---|---|
| `"laplace"` | ε | Laplace 机制，ε-DP |
| `"gaussian"` | ε, δ | Gaussian 机制，(ε,δ)-DP |
