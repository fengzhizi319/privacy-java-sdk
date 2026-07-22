# 隐私预算（Budget）设计文档

## 1. 概述

预算模块为 DP 操作提供 ε/δ 管控，通过 namespace 隔离不同业务的预算，支持内存与 SQLite 两种后端。

## 2. 核心概念

- **ε (epsilon)**：隐私损失上界
- **δ (delta)**：允许超出 ε 的概率上界
- **Namespace**：预算隔离单元
- **顺序组合**：总消耗 = 各次消耗之和

## 3. 架构设计

```
┌─────────────────────────────────────┐
│         BudgetAccountant            │
├─────────────────────────────────────┤
│ - namespace: String                 │
│ - totalEpsilon: double              │
│ - totalDelta: double                │
│ - spentEpsilon: double              │
│ - spentDelta: double                │
├─────────────────────────────────────┤
│ + getInstance(ns, ε, δ)             │
│ + spend(ε, δ)                       │
│ + getRemainingEpsilon(): double     │
│ + getRemainingDelta(): double       │
│ + reset()                           │
└─────────────────────────────────────┘
```

## 4. 单例管理

```java
private static final Map<String, BudgetAccountant> INSTANCES = new ConcurrentHashMap<>();

public static BudgetAccountant getInstance(String namespace, double epsilon, double delta) {
    return INSTANCES.computeIfAbsent(namespace,
        ns -> new BudgetAccountant(ns, epsilon, delta));
}
```

## 5. 线程安全

- 使用 `synchronized` 保护 spend/reset 操作
- 单例注册使用 ConcurrentHashMap
- 可在多线程环境中安全并发调用

## 6. 模块结构

```
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── util/
│   └── BudgetAccountant.java   # 预算管控核心
└── PrivacyClient.java          # 入口客户端
```
