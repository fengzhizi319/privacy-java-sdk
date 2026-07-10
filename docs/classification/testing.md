# Data Classification 测试文档

## 1. 测试策略

采用 JUnit 5 进行单元测试，覆盖：

- 20 个通用测试用例（字段级规则命中与未命中）。
- 记录级与表级聚合逻辑。
- 参数治理（默认值、请求参数、人工覆盖）。
- JSON 输入解析。
- `ClassificationClient` 访问器。

## 2. 测试用例

### ClassificationApiTest

| 测试方法 | 说明 |
|---|---|
| `testIdCardValid` | 合法身份证识别为 PII_ID_CARD（L3） |
| `testIdCardInvalidChecksum` | 校验和错误的身份证不识别 |
| `testMobileValid` | 合法手机号识别为 PII_MOBILE（L3） |
| `testMobileInvalidPrefix` | 非法前缀手机号不识别 |
| `testShanghaiMedicalCardValid` | 上海医保卡号识别为 PII_MEDICAL_CARD（L3） |
| `testIcd10Hiv` | B21.1 识别为 MEDICAL_ICD10_HIV（L4） |
| `testIcd10Psychiatric` | F25 识别为 MEDICAL_ICD10_PSYCHIATRIC（L4） |
| `testIcd10Cancer` | C78.0 识别为 MEDICAL_ICD10_CANCER（L4） |
| `testIcd10General` | J18.9 识别为 MEDICAL_ICD10_GENERAL（L3） |
| `testGenomicBrca1` | brca1_status 字段识别为 GENOMIC_BRCA_TP53（L5） |
| `testGenomicRsNumber` | rs12345 识别为 GENOMIC_VARIANT（L5） |
| `testGenomicBamHeader` | BAM 文件头识别为 GENOMIC_BAM（L5） |
| `testGenomicVcfHeader` | VCF 文件头识别为 GENOMIC_VCF（L5） |
| `testGenomicSamHeader` | @SQ 头识别为 GENOMIC_BAM（L5） |
| `testGenomicSequence` | 长序列识别为 GENOMIC_SEQUENCE（L5） |
| `testPublicReport` | public_report 识别为 PUBLIC_REPORT（L1） |
| `testOperationalStat` | turnover_rate 识别为 OPERATIONAL_STAT（L2） |
| `testNameFallback` | 普通姓名无高敏感标签，fallback 到默认等级 |
| `testRecordAggregation` | 记录聚合最终等级为 L4 |
| `testTableAggregation` | 表聚合最终等级为 L5 |
| `testManualOverride` | manual_override 降级 id_card 到 L1 |
| `testDisableRuleEngine` | 关闭规则引擎后 fallback 到默认等级 |
| `testClassifyJsonObject` | JSON 对象解析为记录 |
| `testClassifyJsonArray` | JSON 数组解析为表 |
| `testClassificationClientAccessor` | `ClassificationClient.classification()` 可用 |
| `testClassificationClientWithProfile` | `ClassificationClient` 支持传入 `PrivacyProfile` |
| `testTagStringRepresentation` | 标签字符串为 L3_PII_MOBILE |

## 3. 运行测试

```bash
cd /home/charles/code/sfwork/privacy-java-sdk
mvn test
```

## 4. 覆盖率目标

- 行覆盖率 ≥ 70%
- `ClassificationApi` 公共方法覆盖率 100%

## 5. CI 建议

```yaml
# .github/workflows/java-test.yml
name: Java SDK Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn test
```
