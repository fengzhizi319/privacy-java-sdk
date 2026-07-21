# privacy-java-sdk 设计实现总结

> 本文档总结 privacy-java-sdk 的架构设计、工程实践与注意事项，供其他项目参照。

---

## 一、项目定位

Java 本地隐私保护 SDK，以函数库形式嵌入 Java 后端应用（如 SecretPad），提供六大隐私原语：
脱敏（Masking）、差分隐私（DP）、本地差分隐私（Local DP）、K-匿名、查询混淆（QoL）、数据分类（Classification）。

---

## 二、应该做的（标准实践）

### 2.1 分层架构

```
PrivacyClient (入口 Facade)
├── api/          → 各原语独立 API 类 (DpApi, MaskingApi, KAnonymityApi, QolApi, LocalDpApi)
├── classification/ → 分类引擎 (RuleEngine + SmallNer + LLM 可插拔)
├── model/        → 数据模型 (PrivacyResult, PrivacyContext, classification/*)
├── exception/    → 统一异常体系 (PrivacyException, PrivacyBudgetExhaustedException)
└── util/         → 工具类 (BudgetAccountant, ParameterResolver, PrivacyMetrics, JsonParser)
```

**要点**：
- 入口类 `PrivacyClient` 聚合所有 API，提供 Builder 模式流式配置
- 每个原语独立一个 API 类，职责单一，可独立使用
- 模型与逻辑分离，model 包只放数据结构

### 2.2 隐私预算管控

- `BudgetAccountant` 按 namespace 单例管理，`ConcurrentHashMap` + `synchronized` 保证线程安全
- 每次 DP 操作前 `spend(epsilon, delta)`，耗尽抛 `PrivacyBudgetExhaustedException`
- 支持多 namespace 隔离（不同业务线独立预算）

### 2.3 参数解析优先级

`ParameterResolver` 实现四级参数合并：
```
请求参数 > 上下文参数 > Profile YAML 配置 > 代码默认值
```
支持个性化参数持久化 (`savePersonalizedParams`)。

### 2.4 测试体系

| 层次 | 工具 | 说明 |
|------|------|------|
| 单元测试 | JUnit 5 | 每个 API 类对应一个 Test |
| 属性测试 | jqwik | DP 统计保证验证（噪声均值≈0、方差符合理论） |
| 集成测试 | Failsafe | PrivacyClientIntegrationTest |
| 性能基准 | JMH | PrivacyBenchmark |
| 覆盖率 | JaCoCo | 行覆盖 ≥ 60% 门禁 |

### 2.5 CI/CD 流水线

```yaml
build (Java 17/21 矩阵) → static-analysis (SpotBugs + Checkstyle) → security (OWASP) → publish (Maven Central)
```

### 2.6 发布到 Maven Central

- `maven-source-plugin` + `maven-javadoc-plugin` 生成 source/javadoc jar
- `maven-gpg-plugin` 签名（`-Prelease` 激活）
- `nexus-staging-maven-plugin` 自动发布到 Sonatype OSSRH
- `japicmp-maven-plugin` API 兼容性检查

### 2.7 工程规范文件

| 文件 | 作用 |
|------|------|
| `checkstyle.xml` | 命名/导入/编码/设计规则 |
| `.editorconfig` | 统一编辑器缩进编码 |
| `.pre-commit-config.yaml` | 提交前自动检查 |
| `Makefile` | 本地开发快捷命令 |
| `CONTRIBUTING.md` | 贡献指南 |
| `SECURITY.md` | 安全漏洞报告流程 |
| `CHANGELOG.md` | Keep a Changelog 格式 |
| `LICENSE` | Apache-2.0 |

---

## 三、额外做的优秀设计

### 3.1 PrivacyResult 审计包装

```java
PrivacyResult<T> {
    T data;                    // 核心结果
    Map<String, Object> paramsUsed;  // 实际参数（审计复现）
    Map<String, Object> proof;       // 可审计证明（预算消耗、泛化层级）
    List<String> warnings;           // 警告（预算接近耗尽等）
}
```
**优势**：调用方不仅得到结果，还能知道"用了什么参数、花了多少预算、有什么风险"。

### 3.2 ThreadLocalRandom 零竞争设计

```java
private Random rng() {
    return random != null ? random : ThreadLocalRandom.current();
}
```
- 生产环境使用 `ThreadLocalRandom`，高并发下无锁竞争
- 测试时注入固定种子 `Random`，保证可重复性
- 对比：Go SDK 使用 `sync.Mutex` 保护 `rand.Rand`（Go 标准库限制）

### 3.3 Micrometer 可选可观测性

```java
// 未配置 registry 时所有方法为 no-op，零开销
public <T> T time(String primitive, String operation, Supplier<T> supplier) {
    if (registry == null) return supplier.get();
    return Timer.builder(...).register(registry).record(supplier);
}
```
- `micrometer-core` 标记为 `<optional>true</optional>`，不传递给下游
- 支持 Spring Boot Actuator / Prometheus / Datadog 等任意后端

### 3.4 分类引擎三层可插拔

```
RuleEngine (正则/规则, 确定性) → SmallNerEngine (NER 模型) → LlmClassifier (大模型)
```
- 每层有 NoOp 默认实现，未配置时自动跳过
- 支持 `EngineLayer` 枚举标记结果来源层级
- `SecurityTag` + `AuditInfo` 记录分类决策链路

### 3.5 不可变结果保证

`PrivacyResult` 的 getter 返回 `Collections.unmodifiableMap/List`，防止调用方篡改审计数据。

### 3.6 maven-enforcer 依赖收敛

```xml
<dependencyConvergence/>  <!-- 强制所有传递依赖版本一致 -->
<requireMavenVersion>3.8</requireMavenVersion>
<requireJavaVersion>17</requireJavaVersion>
```

---

## 四、注意事项

### 4.1 预算单例陷阱

`BudgetAccountant.getInstance()` 是 JVM 级单例。同一 namespace 首次创建后，后续调用忽略 epsilon/delta 参数。
**注意**：单元测试中必须使用独立 namespace 避免预算串扰。

### 4.2 Gaussian 机制必须 delta > 0

Laplace 机制 delta 可为 0，但 Gaussian 机制必须 `delta > 0`，否则 `validateParams` 会抛异常。
调用方需注意区分。

### 4.3 Checkstyle/SpotBugs 当前为非阻断

```xml
<failOnViolation>false</failOnViolation>  <!-- checkstyle -->
<failOnError>false</failOnError>          <!-- spotbugs -->
```
当前设为 warning 级别不阻断构建。生产就绪后应逐步改为 `true`。

### 4.4 序列化兼容

Model 类实现 `Serializable` 并声明 `serialVersionUID`。
新增字段时注意向后兼容（不要删除/重命名已有字段）。

### 4.5 无外部运行时依赖

核心 SDK 仅依赖 `slf4j-api` + `snakeyaml`，`micrometer-core` 为 optional。
**原则**：SDK 不应引入 Spring/Guava 等重量级框架，保持嵌入式轻量。

### 4.6 JPMS 模块系统

已添加 `module-info.java`，下游若使用 Java 9+ 模块系统需注意 `requires` 声明。

---

## 五、技术栈速查

| 组件 | 版本 |
|------|------|
| Java | 17 (CI 矩阵含 21) |
| Maven | 3.8+ |
| JUnit | 5.10.2 |
| jqwik | 1.8.4 |
| JMH | 1.37 |
| Micrometer | 1.12.4 (optional) |
| SLF4J | 2.0.12 |
| SnakeYAML | 2.2 |
| JaCoCo | 0.8.11 |
| SpotBugs | 4.8.3.1 |
| Checkstyle | 3.3.1 |
| OWASP dep-check | 9.0.9 |
| japicmp | 0.18.3 |

---

## 六、可复用的设计模式清单

| 模式 | 应用场景 | 本项目实现 |
|------|----------|------------|
| Builder | 复杂对象构建 | `PrivacyClient.Builder` |
| Facade | 统一入口 | `PrivacyClient` 聚合 6 个 API |
| Strategy | 噪声机制切换 | Laplace / Gaussian 按参数选择 |
| Singleton (namespace) | 预算共享 | `BudgetAccountant.getInstance()` |
| Template Method | 参数解析 | `ParameterResolver` 四级合并 |
| NoOp | 可选依赖零开销 | `PrivacyMetrics(null)` / `NoOpLlmClassifier` |
| Wrapper/Audit | 结果增强 | `PrivacyResult<T>` |
| Pluggable Pipeline | 分类引擎 | Rule → NER → LLM 三层 |
