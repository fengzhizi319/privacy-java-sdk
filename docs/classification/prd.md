# Data Classification 产品需求文档

## 1. 产品定位

在 `privacy-java-sdk` 中提供**数据分类（Data Classification）**独立入口，为 Java 后端提供进程内、无外部依赖的敏感数据识别能力。识别结果可直接用于脱敏策略路由、访问控制、合规审计以及 SecretFlow 组件输入。

## 2. 目标用户

| 用户 | 场景 |
|---|---|
| Java 后端开发 | 接口入参/出参自动识别身份证号、手机号、ICD-10 编码、基因组数据 |
| 数据平台开发 | 对批量记录/表进行敏感度定级，生成分类报告 |
| 合规开发 | 识别高敏感字段并标记人工复核 |

## 3. 功能需求

### FR-1 多层级分类引擎
- **Layer 1 规则引擎**：基于字段名与字段值的多规则匹配，覆盖 PII、医疗、基因组、运营统计等类别。
- **Layer 2 Small NER**：预留命名实体识别接口，默认无操作实现。
- **Layer 3 LLM 兜底**：预留大模型分类接口，默认按上游结果保守处理。

### FR-2 敏感度等级
- 支持 L1~L5 五级敏感度，等级可比较、取最大值。
- 输出 `SecurityTag`，包含 level、category、confidence、sourceEngine、ruleId、version、needsHumanReview。

### FR-3 多粒度输出
- 字段级 `FieldClassificationResult`
- 记录级 `RecordClassificationResult`
- 表级 `TableClassificationResult`
- 统一包装 `ClassificationResult` + `AuditInfo`

### FR-4 多格式输入
- 单字段、单条记录 `Map<String, Object>`、表 `schema + rows`。
- JSON 字符串（对象或数组）。

### FR-5 参数治理
- 优先级：SDK 默认值 → YAML Profile → 请求参数 → 人工覆盖。
- 支持开关各引擎、配置 ICD-10 L4 区间、基因组关键词、白名单、运营统计模式以及 `manualOverride`。

### FR-6 可审计
- 每次分类输出 `AuditInfo`，记录版本、时间戳、规则引擎版本、参数来源。

## 4. 非功能性需求

| 维度 | 要求 |
|---|---|
| 性能 | 单字段分类 < 1ms；百行表 < 50ms |
| 依赖 | 仅 JDK 17 内置类库 + 已有 snakeyaml（参数解析复用） |
| 并发 | API 无状态，线程安全 |
| 准确率 | 20 个通用测试用例全部通过 |

## 5. API 交互示例

```java
import com.github.fengzhizi319.privacy.sdk.classification.ClassificationClient;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;

ClassificationClient client = new ClassificationClient();
FieldClassificationResult r = client.classification()
    .classifyField("id_card", "110101199001011237", null);
System.out.println(r.getFinalLevel()); // L3
```

## 6. 权限与安全

- 分类过程完全在 JVM 进程内完成，数据不出进程。
- LLM 与 NER 默认关闭，避免意外调用外部服务。
- 高敏感字段（L4/L5）默认建议人工复核。

## 7. 验收标准

- [x] 全部 20 个通用测试用例通过。
- [x] 参数覆盖、表聚合、JSON 输入有独立测试。
- [x] `ClassificationClient.classification()` 访问器可用。
- [x] `mvn test` 全项目通过。
