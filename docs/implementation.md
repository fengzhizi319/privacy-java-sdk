# Java SDK 实现文档

## 1. 依赖

- JDK 17+
- Maven 3.8+
- `org.yaml:snakeyaml`：YAML Profile 解析
- `org.junit.jupiter:junit-jupiter`：单元测试

## 2. 构建

```bash
mvn clean package -DskipTests
```

产物：`target/privacy-java-sdk-0.1.0-SNAPSHOT.jar`

## 3. 核心实现说明

### 3.1 MaskingApi

- `maskValue(fieldName, value, context)`：根据字段名关键词识别类型并返回掩码结果。
- `hashValue(value, salt)`：使用 `HmacSHA256` 生成 16 位 Base64 截断结果。
- `truncate(value, keepPrefix)`：保留前缀，其余替换为 `*`。

### 3.2 DpApi

- `count(trueCount, epsilon, mechanism)`：先 `budget.spend(epsilon, 0)`，再加 Laplace 噪声。
- Laplace 采样：`u ~ U(-0.5, 0.5)`，返回 `-scale * sign(u) * log(1 - 2|u|)`。
- `mean()` 采用计数与求和均分隐私预算的朴素组合。

### 3.3 KAnonymityApi

- `anonymizeRecord(record, qiCols, hierarchies, k)`：对每条 QI 列调用对应层次泛化。
- 泛化级别 `chooseLevel(k, h)` 按 `k/5` 简单递增，实际生产应基于数据分布动态计算。
- 预置层次：
  - `ageHierarchy`：精确 → 5 岁区间 → 10 岁区间 → 20 岁区间 → `*`
  - `zipcodeHierarchy`：精确 → 3 位前缀 → 2 位前缀 → 1 位前缀 → `*`
  - `genderHierarchy`：精确 → `*`

### 3.4 QolApi

- `obfuscateQuery(query, numDummies, domain)`：从领域模板中随机选择 Dummy，并把真实查询随机插入其中。

### 3.5 ParameterResolver

- 使用 SnakeYAML 加载 `privacy-profile.yaml`。
- `resolve()` 按优先级合并参数并校验。

### 3.6 BudgetAccountant

- 使用 `ConcurrentHashMap<String, BudgetAccountant>` 保存单例。
- `spend()` 使用 `synchronized` 保证原子性。

## 4. 代码目录

```
src/main/java/com/secretflow/privacy/sdk/
├── PrivacyClient.java
├── PrivacyProfile.java
├── api/
│   ├── DpApi.java
│   ├── KAnonymityApi.java
│   ├── MaskingApi.java
│   └── QolApi.java
├── model/
│   ├── PrivacyContext.java
│   └── PrivacyResult.java
├── util/
│   ├── BudgetAccountant.java
│   └── ParameterResolver.java
└── exception/
    ├── PrivacyException.java
    └── PrivacyBudgetExhaustedException.java
```

## 5. 已知限制与后续优化

- DP 当前为简化实现，未使用 RDP 组合定理。
- `Random` 非线程安全，高并发建议替换为 `ThreadLocalRandom`。
- K-匿名未实现 Mondrian 多维分区，当前为单记录泛化演示。
