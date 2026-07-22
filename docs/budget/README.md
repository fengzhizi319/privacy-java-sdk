# 隐私预算（Budget）模块

按 namespace 维护 epsilon/delta 预算，为差分隐私操作提供统一的预算记账与管控。支持内存与 SQLite 两种存储后端，支持时间窗口自动重置。

## 文档索引

| 文档 | 说明 |
|---|---|
| [产品需求](prd.md) | 功能需求与验收标准 |
| [设计文档](design.md) | 架构设计与单例管理 |
| [API 参考](api_reference.md) | 接口签名与参数说明 |
| [使用示例](examples.md) | 典型场景代码示例 |
| [测试策略](testing.md) | 测试方法与检查清单 |
| [运维手册](ops.md) | 配置、监控与故障排查 |

## 快速开始

```java
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

// 获取预算实例（单例）
BudgetAccountant budget = BudgetAccountant.getInstance("my-app", 10.0, 1e-3);

// 消耗预算
budget.spend(1.0, 1e-4);

// 查看剩余
double remaining = budget.getRemainingEpsilon();
```

## API 概览

| 方法 | 说明 |
|---|---|
| `getInstance(namespace, ε, δ)` | 获取/创建预算实例 |
| `spend(ε, δ)` | 消耗预算 |
| `getRemainingEpsilon()` | 剩余 ε |
| `getRemainingDelta()` | 剩余 δ |
| `reset()` | 重置预算 |

## 配置

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `PRIVACY_BUDGET_DB` | 空（内存） | SQLite 文件路径 |
| `PRIVACY_BUDGET_WINDOW_SECONDS` | 0 | 自动重置窗口 |
