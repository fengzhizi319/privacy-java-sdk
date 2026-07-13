# Data Classification 设计文档

## 1. 总体架构

```text
┌─────────────────────────────────────────────┐
│  Java Business Application                  │
│  ├─ ClassificationClient                    │
│  │   └─ ClassificationApi                   │
│  │       ├─ RuleEngine (DefaultRuleEngine)  │
│  │       ├─ SmallNerEngine (No-Op)          │
│  │       └─ LlmClassifier (No-Op)           │
│  ├─ PrivacyProfile (YAML)                   │
│  └─ BudgetAccountant                        │
└─────────────────────────────────────────────┘
```

## 2. 包结构

| 包 | 职责 |
|---|---|
| `com.github.fengzhizi319.privacy.sdk.classification` | `ClassificationClient`、`ClassificationApi` |
| `com.github.fengzhizi319.privacy.sdk.classification.engine` | 规则引擎、NER、LLM 接口与默认实现 |
| `com.github.fengzhizi319.privacy.sdk.classification.util` | 身份证号/ICD-10/基因组校验器 |
| `com.github.fengzhizi319.privacy.sdk.model.classification` | 分类相关数据模型 |

## 3. 核心类设计

### ClassificationClient
- 数据分类独立入口，持有 `ClassificationApi` 实例。
- 支持默认 Profile 与自定义 Profile。

### ClassificationApi
- 统一入口，提供 `classifyField`、`classifyRecord`、`classifyTable`、`classifyJson`。
- 协调三层引擎：Rule → NER → LLM fallback。
- 调用 `ParameterResolver` 完成参数治理。

### DefaultRuleEngine
- 实现全部 Layer 1 规则：
  - 字段名规则：brca/tp53、rs/snp/cnv/genome、gene/mutation/variant、bam/vcf/fastq。
  - 值规则：身份证、手机号、上海医保卡、ICD-10、BAM/VCF/FASTQ 头、基因序列、公开报表、运营统计。
- 所有命中标签置信度 1.0，sourceEngine = RULE。

### NoOpSmallNerEngine / NoOpLlmClassifier
- 默认无操作实现，不引入外部模型依赖。
- NER 接口预留升级能力；LLM 默认在置信度不足时保守处理并标记人工复核。

### 模型类
- `SensitivityLevel`：五级枚举，支持比较与取最大。
- `SecurityTag`：标签原子，支持 `toTagString()`。
- `FieldClassificationResult` / `RecordClassificationResult` / `TableClassificationResult`：分层结果。
- `ClassificationResult`：统一包装器。
- `AuditInfo`：审计信息。
- `ClassificationParams`：参数对象，支持从 `Map` 解析。

## 4. 数据流

```text
业务调用 -> ClassificationApi -> ParameterResolver -> RuleEngine -> NER -> LLM -> 人工覆盖 -> 聚合 -> ClassificationResult
```

## 5. 参数解析优先级

1. SDK 内置默认值
2. Profile YAML 中 `primitives.classification`
3. 方法请求参数
4. `manual_override` 字段级覆盖

## 6. 线程安全

- `ClassificationApi`、`DefaultRuleEngine`、校验器均为无状态。
- 可安全在多线程环境共享同一个 `ClassificationApi` 实例。

## 7. 扩展点

- 替换 `RuleEngine` 可定制规则集。
- 替换 `SmallNerEngine` 可接入本地/远程 NER 模型。
- 替换 `LlmClassifier` 可接入 LLM 服务。
- 通过 `ClassificationParams` 动态配置 ICD-10 区间与关键词。


