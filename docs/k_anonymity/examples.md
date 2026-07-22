# K-匿名使用示例

## 1. 单记录泛化

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import com.github.fengzhizi319.privacy.sdk.api.KAnonymityApi;
import java.util.*;

public class KAnonRecordExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        Map<String, Object> record = new HashMap<>();
        record.put("age", "28");
        record.put("zipcode", "518057");
        record.put("gender", "女");
        record.put("disease", "流感");

        Map<String, KAnonymityApi.GeneralizationHierarchy> hierarchies = Map.of(
            "age", KAnonymityApi.ageHierarchy(),
            "zipcode", KAnonymityApi.zipcodeHierarchy(),
            "gender", KAnonymityApi.genderHierarchy()
        );

        Map<String, Object> result = client.kAnonymizeRecord(
            record, Arrays.asList("age", "zipcode", "gender"), hierarchies, 5);

        System.out.println("泛化结果: " + result);
        // 输出: {age=20-30, zipcode=518***, gender=*, disease=流感}
    }
}
```

## 2. Mondrian 表格泛化

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.*;

public class KAnonTableExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("age", 25, "zipcode", "100001", "disease", "A"));
        rows.add(Map.of("age", 26, "zipcode", "100002", "disease", "B"));
        rows.add(Map.of("age", 55, "zipcode", "200001", "disease", "C"));
        rows.add(Map.of("age", 56, "zipcode", "200002", "disease", "D"));

        List<Map<String, Object>> result = client.kAnonymizeTable(
            rows, Arrays.asList("age", "zipcode"), 2, 10);

        result.forEach(row -> System.out.println("  " + row));
    }
}
```

## 3. 自定义泛化层次

```java
import com.github.fengzhizi319.privacy.sdk.api.KAnonymityApi;

public class CustomHierarchy implements KAnonymityApi.GeneralizationHierarchy {
    @Override
    public String generalize(String value, int level) {
        switch (level) {
            case 0: return value;
            case 1:
                if (value.contains("工程师")) return "IT从业者";
                if (value.contains("医生")) return "医疗从业者";
                return "其他";
            default: return "*";
        }
    }

    @Override
    public int maxLevel() { return 2; }
}
```

## 4. 错误处理

```java
try {
    client.kAnonymizeTable(rows, qiCols, 10, 10); // 数据不足 k
} catch (IllegalArgumentException e) {
    System.out.println("错误: " + e.getMessage());
}
```
