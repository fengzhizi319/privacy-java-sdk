# 查询混淆使用示例

## 1. 基本查询混淆

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.List;

public class QolExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<String> result = client.obfuscateQuery(
            "高血压用药指南", 3, "medical", null, null);

        System.out.println("混淆结果:");
        result.forEach(q -> System.out.println("  - " + q));
    }
}
```

## 2. 自定义 Dummy 池

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class CustomPoolExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<String> financePool = Arrays.asList(
            "股票开户流程", "基金定投策略", "信用卡还款", "房贷利率计算");

        List<String> result = client.obfuscateQuery(
            "征信报告查询", 3, "generic", null, financePool);

        result.forEach(q -> System.out.println("  - " + q));
    }
}
```

## 3. 批量混淆

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class BatchQolExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<String> queries = Arrays.asList("HIV检测", "抑郁症治疗");
        List<List<String>> results = client.obfuscateQueryBatch(
            queries, 3, "medical", null, null);

        for (int i = 0; i < results.size(); i++) {
            System.out.printf("查询 [%s] 混淆后: %s%n", queries.get(i), results.get(i));
        }
    }
}
```

## 4. 带详情混淆

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import com.github.fengzhizi319.privacy.sdk.api.QolApi;

public class QolDetailExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        QolApi.QoLResult result = client.obfuscateQueryWithDetails(
            "心理咨询预约", 5, "medical", null, null);

        System.out.printf("领域: %s, Dummy数: %d, 策略: %s%n",
            result.domain, result.numDummies, result.strategy);
        System.out.println("查询列表: " + result.queries);
    }
}
```
