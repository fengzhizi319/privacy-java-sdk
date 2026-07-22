# 隐私预算（Budget）API 参考

## 1. 概述

预算模块通过 `BudgetAccountant.getInstance` 获取按 namespace 隔离的单例实例。

## 2. 核心 API

### 2.1 getInstance

```java
public static BudgetAccountant getInstance(String namespace, double epsilon, double delta)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| namespace | String | 预算命名空间 |
| epsilon | double | 总 ε 预算 |
| delta | double | 总 δ 预算 |

---

### 2.2 spend

```java
public synchronized void spend(double epsilon, double delta)
```

预算不足时抛出 `BudgetExhaustedException`。

---

### 2.3 getRemainingEpsilon / getRemainingDelta

```java
public synchronized double getRemainingEpsilon()
public synchronized double getRemainingDelta()
```

---

### 2.4 reset

```java
public synchronized void reset()
```

## 3. 通过 Client 使用

```java
PrivacyClient client = PrivacyClient.builder()
    .namespace("my-app")
    .epsilon(10.0)
    .delta(1e-4)
    .build();

// 访问预算
BudgetAccountant budget = client.budget();
double remaining = budget.getRemainingEpsilon();
```

## 4. 异常

| 异常 | 触发条件 |
|---|---|
| `BudgetExhaustedException` | ε 或 δ 预算不足 |

## 5. 线程安全

所有方法为线程安全（synchronized）。
