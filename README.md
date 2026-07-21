# privacy-java-sdk

[![CI](https://github.com/fengzhizi319/privacy-java-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/fengzhizi319/privacy-java-sdk/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.fengzhizi319/privacy-java-sdk.svg)](https://search.maven.org/artifact/com.github.fengzhizi319/privacy-java-sdk)

> Java 本地隐私保护 SDK，直接以函数库形式集成到 Java 后端应用，支持脱敏、K-匿名、差分隐私、查询混淆四类处理原语，以及独立的数据分类能力。

## Processing Primitives（处理原语）

### 快速开始

#### Maven 依赖

```xml
<dependency>
  <groupId>com.github.fengzhizi319</groupId>
  <artifactId>privacy-java-sdk</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

#### 代码示例

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import com.github.fengzhizi319.privacy.sdk.api.KAnonymityApi;

PrivacyClient client = new PrivacyClient();

// 脱敏
String masked = client.masking().maskValue("mobile", "13812345678", "doctor_query");

// 差分隐私计数
List<Double> values = List.of(1.0, 0.0, 1.0, 1.0, 0.0);
double noisyCount = client.dp().count(values, 1.0, "laplace");

// K-匿名单记录泛化
Map<String, Object> record = Map.of(
    "age", "28", "zipcode", "518057", "gender", "女", "disease", "胃癌"
);
Map<String, Object> anon = client.kAnonymity().anonymizeRecord(
    record,
    List.of("age", "zipcode", "gender"),
    Map.of(
        "age", KAnonymityApi.ageHierarchy(),
        "zipcode", KAnonymityApi.zipcodeHierarchy(),
        "gender", KAnonymityApi.genderHierarchy()
    ),
    5
);

// 查询混淆
List<String> queries = client.qol().obfuscateQuery("糖尿病患者用药趋势", 3, "medical");
```

## Data Classification（数据分类）

数据分类通过独立的 `ClassificationClient` 入口使用，与处理原语完全解耦：

```java
import com.github.fengzhizi319.privacy.sdk.classification.ClassificationClient;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;

ClassificationClient client = new ClassificationClient();

FieldClassificationResult classified = client.classification()
    .classifyField("id_card", "110101199001011237", null);
System.out.println(classified.getFinalLevel()); // L3
System.out.println(classified.getTags().get(0).toTagString()); // L3_PII_ID_CARD
```

## 运行测试

```bash
mvn test
```

## 文档

### 处理原语

- [PRD 产品需求文档](./docs/prd.md)
- [Design 设计文档](./docs/design.md)
- [Implementation 实现文档](./docs/implementation.md)
- [Testing 测试文档](./docs/testing.md)
- [User Manual 使用手册](./docs/user-manual.md)

### 数据分类

- [Classification PRD](./docs/classification/prd.md)
- [Classification Design](./docs/classification/design.md)
- [Classification Ops](./docs/classification/ops.md)
- [Classification Testing](./docs/classification/testing.md)
