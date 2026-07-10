# Java SDK 设计文档

## 1. 总体架构

```
┌─────────────────────────────────────────────┐
│  Java Business Application                  │
│  ├─ PrivacyClient                           │
│  │   ├─ MaskingApi                          │
│  │   ├─ DpApi                               │
│  │   ├─ KAnonymityApi                       │
│  │   └─ QolApi                              │
│  ├─ PrivacyProfile (YAML)                   │
│  └─ BudgetAccountant                        │
└─────────────────────────────────────────────┘
```

## 2. 包结构

| 包 | 职责 |
|---|---|
| `com.github.fengzhizi319.privacy.sdk` | 入口 `PrivacyClient`、`PrivacyProfile` |
| `api` | 四类原语 API |
| `model` | `PrivacyContext`、`PrivacyResult` |
| `util` | 参数解析、预算台账 |
| `exception` | 统一异常 |

## 3. 核心类设计

### PrivacyClient
- 统一入口，持有四个 API 实例和 `BudgetAccountant`。
- 支持默认 Profile 与自定义 Profile。

### MaskingApi
- 字段类型识别 → 策略路由 → 掩码/哈希/截断。
- 无状态，线程安全。

### DpApi
- 调用 `BudgetAccountant.spend()` 消耗预算。
- 使用 `Random` 生成 Laplace 噪声。

### KAnonymityApi
- 通过 `GeneralizationHierarchy` 接口泛化字段。
- 预置年龄、邮编、性别层次。

### QolApi
- 基于领域模板生成 Dummy Query。

### ParameterResolver
- 合并默认值、Profile、请求参数。
- 执行参数校验。

### BudgetAccountant
- 单例 per namespace，使用 `synchronized` 保证预算更新原子性。

## 4. 数据流

```
业务调用 -> PrivacyClient -> API -> ParameterResolver -> BudgetAccountant -> 算法执行 -> PrivacyResult
```

## 5. 参数解析优先级

1. 请求显式参数
2. Profile YAML 中的 primitive 配置
3. 算法默认值

## 6. 线程安全

- `MaskingApi`、`QolApi` 无状态。
- `DpApi` 依赖 `BudgetAccountant`，其 `spend()` 为 `synchronized`。
- `Random` 实例非线程安全，当前 MVP 中每个 `DpApi` 实例独立持有；高并发场景建议使用 `ThreadLocalRandom` 改进。

## 7. 扩展点

- 新增字段类型：在 `MaskingApi.guessFieldType()` 中扩展。
- 新增泛化层次：实现 `GeneralizationHierarchy`。
- 新增 DP 机制：在 `DpApi` 中扩展 `mechanism` 分支。
