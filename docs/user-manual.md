# Java SDK 使用手册

## 1. 安装

将 JAR 安装到本地 Maven 仓库：

```bash
cd privacy-java-sdk
mvn clean install -DskipTests
```

在业务项目 `pom.xml` 中添加依赖：

```xml
<dependency>
  <groupId>com.github.fengzhizi319</groupId>
  <artifactId>privacy-java-sdk</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 2. 初始化

```java
// 无 Profile，使用默认值
PrivacyClient client = new PrivacyClient();

// 使用 YAML Profile
PrivacyClient client = new PrivacyClient(PrivacyProfile.fromYaml("privacy-profile.yaml"));
```

## 3. 脱敏

```java
String maskedMobile = client.masking().maskValue("mobile", "13812345678", "doctor_query");
// 138****5678

String hashed = client.masking().hashValue("13812345678", "${SALT}");
// 16位哈希
```

## 4. 差分隐私

```java
List<Double> values = List.of(1.0, 0.0, 1.0, 1.0);
double noisyCount = client.dp().count(values, 1.0, "laplace");
```

## 5. K-匿名

```java
Map<String, Object> record = Map.of(
    "age", "28",
    "zipcode", "518057",
    "gender", "女",
    "disease", "胃癌"
);

Map<String, KAnonymityApi.GeneralizationHierarchy> hierarchies = Map.of(
    "age", KAnonymityApi.ageHierarchy(),
    "zipcode", KAnonymityApi.zipcodeHierarchy(),
    "gender", KAnonymityApi.genderHierarchy()
);

Map<String, Object> anon = client.kAnonymity().anonymizeRecord(
    record, List.of("age", "zipcode", "gender"), hierarchies, 5
);
```

## 6. 查询混淆

```java
List<String> queries = client.qol().obfuscateQuery(
    "糖尿病患者用药趋势", 3, "medical"
);
```

## 7. YAML Profile 示例

```yaml
version: "1.0"
namespace: hospital_a

primitives:
  dp:
    epsilon: 1.0
    delta: 1.0e-5
    mechanism: laplace
  k_anonymity:
    k: 5
    l: 2
    t: 0.2
```

## 8. 隐私预算

默认每个 namespace 总预算 ε=10.0, δ=1e-4。可通过自定义 `BudgetAccountant` 调整：

```java
BudgetAccountant budget = BudgetAccountant.getInstance("my-ns", 100.0, 1e-3);
PrivacyClient client = new PrivacyClient(PrivacyProfile.empty(), budget);
```

## 9. 异常处理

```java
try {
    client.dp().count(10L, 1.0, "laplace");
} catch (PrivacyBudgetExhaustedException e) {
    // 预算耗尽，拒绝处理或降级
}
```

## 10. 最佳实践

- 在网关层统一做脱敏，避免各业务重复实现。
- DP 参数 ε 越小隐私越强，但噪声越大；建议按数据用途选择模板。
- K-匿名泛化层次应结合业务可接受的信息损失定制。
