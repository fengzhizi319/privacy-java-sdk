# 数据脱敏使用示例

## 1. 单字段脱敏

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

public class MaskValueExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        System.out.println(client.maskValue("mobile", "13812345678", "api"));
        // 输出: 138****5678

        System.out.println(client.maskValue("id_card", "110101199001011234", "api"));
        // 输出: 110101********1234

        System.out.println(client.maskValue("user_name", "张三丰", "api"));
        // 输出: 张**丰

        System.out.println(client.maskValue("email", "zhangsan@example.com", "api"));
        // 输出: z***n@example.com

        System.out.println(client.maskValue("address", "北京市朝阳区某某街道123号", "api"));
        // 输出: 北京市朝阳区****

        System.out.println(client.maskValue("bank_card", "6222021234567890123", "api"));
        // 输出: 6222 **** **** 0123
    }
}
```

## 2. 整记录脱敏

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.HashMap;
import java.util.Map;

public class MaskRecordExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        Map<String, Object> record = new HashMap<>();
        record.put("name", "李四");
        record.put("phone", "13987654321");
        record.put("email", "lisi@test.com");
        record.put("age", 30);  // 非字符串字段不脱敏

        Map<String, Object> masked = client.maskRecord(record, "response");

        System.out.println("原始: " + record);
        System.out.println("脱敏: " + masked);
        // 脱敏: {name=李*, phone=139****4321, email=l***@test.com, age=30}
    }
}
```

## 3. 批量脱敏

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class MaskBatchExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<String> fields = Arrays.asList("mobile", "email", "name");
        List<String> values = Arrays.asList("13800001111", "test@abc.com", "王五");

        List<String> masked = client.maskBatch(fields, values, "batch");
        masked.forEach(v -> System.out.println("  " + v));
        // 输出:
        //   138****1111
        //   t***@abc.com
        //   王*
    }
}
```

## 4. HMAC 哈希

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

public class HashExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        String hash1 = client.hashValue("13812345678", "my-secret-salt");
        String hash2 = client.hashValue("13812345678", "my-secret-salt");
        String hash3 = client.hashValue("13812345678", "other-salt");

        System.out.println("hash1: " + hash1);
        System.out.println("hash2: " + hash2);
        System.out.println("相同输入+相同盐 = 相同输出: " + hash1.equals(hash2));
        System.out.println("不同盐 = 不同输出: " + !hash1.equals(hash3));
    }
}
```

## 5. 字符串截断

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

public class TruncateExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        System.out.println(client.truncate("110101199001011234", 6));
        // 输出: 110101

        System.out.println(client.truncate("短文本", 10));
        // 输出: 短文本（长度不足时返回原值）
    }
}
```

## 6. API 响应脱敏中间件

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Map;

public class ResponseMaskingFilter {
    private final PrivacyClient client = new PrivacyClient();

    /**
     * 在 Controller 返回前对响应体脱敏
     */
    public Map<String, Object> maskResponse(Map<String, Object> responseBody) {
        return client.maskRecord(responseBody, "http-response");
    }
}
```
