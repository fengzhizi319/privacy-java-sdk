# Java SDK 测试文档

## 1. 测试策略

采用 JUnit 5 进行单元测试，覆盖四类原语、参数解析、隐私预算、异常路径。

## 2. 测试用例

### MaskingApiTest
- `testMaskMobile`：手机号掩码格式验证。
- `testMaskIdCard`：身份证掩码格式验证。
- `testMaskName`：姓名掩码格式验证。
- `testHashValue`：哈希一致性、盐敏感性、长度验证。
- `testTruncate`：地址截断验证。

### DpApiTest
- `testDpCount`：DP 计数结果在真实值附近。
- `testDpSum`：DP 求和结果在真实值附近。
- `testDpMean`：DP 均值结果在真实值附近。
- `testBudgetExhausted`：预算耗尽时抛出异常。

### KAnonymityApiTest
- `testAnonymizeRecord`：年龄区间化、邮编前缀化、性别泛化为 `*`。

### QolApiTest
- `testObfuscateQuery`：返回结果包含真实查询且总数为 `numDummies+1`。

## 3. 运行测试

```bash
mvn test
```

## 4. 覆盖率目标

- 行覆盖率 ≥ 70%
- 核心 API 方法覆盖率 100%

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
