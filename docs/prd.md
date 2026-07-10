# Java SDK 产品需求文档

## 1. 产品定位

为 Java 后端应用提供进程内本地隐私保护能力，让业务代码无需网络调用、无需运维 Agent，即可对单条/小批量数据进行脱敏、K-匿名泛化、差分隐私加噪、查询混淆。

## 2. 目标用户

| 用户 | 场景 |
|---|---|
| Java 后端开发 | 医生工作站、HIS/EMR 系统、数据中台接口实时脱敏 |
| 数据平台开发 | 在 Java 服务中调用 DP/K-Anon 预处理数据 |
| 合规开发 | 在 Java 网关层对查询文本做 QOL 混淆 |

## 3. 功能需求

### FR-1 脱敏（Masking）
- 支持字段类型识别：手机号、身份证号、姓名、银行卡号、通用字段。
- 支持掩码、哈希（HMAC-SHA256）、截断三种策略。
- 支持按上下文选择规则（如医生端保留更多位，外部端全掩码）。

### FR-2 差分隐私（DP）
- 支持 `count`、`sum`、`mean` 三种统计。
- 支持 Laplace 噪声机制。
- 支持隐私预算检查与消耗。

### FR-3 K-匿名（K-Anonymity）
- 支持单条记录基于泛化层次进行泛化。
- 预置年龄、邮编、性别泛化层次。
- 支持自定义泛化层次接口。

### FR-4 查询混淆（QOL）
- 支持为真实查询生成多个语义相似的 Dummy Query。
- 支持医疗领域模板。

### FR-5 参数解析
- 支持从 YAML Profile 加载参数模板。
- 支持运行时请求参数覆盖。
- 参数校验（ε>0、K≥2 等）。

### FR-6 隐私预算
- 按 namespace 维护预算台账。
- 预算耗尽时抛出异常。

### FR-7 异常处理
- 提供统一 `PrivacyException`。
- 预算耗尽专用 `PrivacyBudgetExhaustedException`。

## 4. 非功能性需求

| 维度 | 要求 |
|---|---|
| 性能 | 单值脱敏 < 1ms；K-匿名单条 < 5ms；DP 单次 < 5ms |
| 并发 | 线程安全，预算台账加锁 |
| 依赖 | 仅依赖 snakeyaml + JUnit（测试） |
| JDK | JDK 17+ |
| 构建 | Maven |

## 5. API 交互示例

```java
PrivacyClient client = new PrivacyClient(PrivacyProfile.fromYaml("profile.yaml"));
String masked = client.masking().maskValue("mobile", "13812345678", "doctor_query");
```

## 6. 权限与安全

- 敏感参数（salt/key）不由 SDK 生成，由业务通过 `PrivacyProfile` 或环境变量注入。
- 所有处理在调用方进程内完成，数据不出 JVM。

## 7. 验收标准

- [ ] 四类原语均能通过单元测试。
- [ ] 隐私预算耗尽时正确抛出异常。
- [ ] 支持 YAML Profile 加载。
- [ ] 单线程/多线程调用结果正确且稳定。
