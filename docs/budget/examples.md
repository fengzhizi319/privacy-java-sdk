# 隐私预算使用示例

## 1. 基本预算管控

```java
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

public class BudgetExample {
    public static void main(String[] args) {
        BudgetAccountant budget = BudgetAccountant.getInstance("my-app", 5.0, 1e-3);

        System.out.printf("初始预算: ε=%.2f%n", budget.getRemainingEpsilon());

        budget.spend(1.0, 1e-4);
        System.out.printf("消耗后: ε=%.2f%n", budget.getRemainingEpsilon());
    }
}
```

## 2. 预算耗尽处理

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class BudgetExhaustExample {
    public static void main(String[] args) {
        PrivacyClient client = PrivacyClient.builder()
            .namespace("limited").epsilon(2.0).delta(1e-4).build();

        List<Double> data = Arrays.asList(1.0, 2.0, 3.0);

        for (int i = 1; i <= 5; i++) {
            try {
                double result = client.dpCount(data, 1.0, 1e-5, "laplace");
                System.out.printf("第 %d 次: %.2f%n", i, result);
            } catch (Exception e) {
                System.out.printf("第 %d 次失败: %s%n", i, e.getMessage());
                break;
            }
        }
    }
}
```

## 3. 多 Namespace 隔离

```java
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

public class MultiNamespaceExample {
    public static void main(String[] args) {
        BudgetAccountant search = BudgetAccountant.getInstance("search", 10.0, 1e-3);
        BudgetAccountant ad = BudgetAccountant.getInstance("ad", 5.0, 1e-4);

        search.spend(3.0, 0);

        System.out.printf("搜索剩余: ε=%.2f%n", search.getRemainingEpsilon());
        System.out.printf("广告剩余: ε=%.2f%n", ad.getRemainingEpsilon());
    }
}
```

## 4. 手动重置

```java
BudgetAccountant budget = BudgetAccountant.getInstance("reset-demo", 5.0, 1e-4);
budget.spend(5.0, 1e-4);
System.out.printf("消耗后: ε=%.2f%n", budget.getRemainingEpsilon()); // 0.00

budget.reset();
System.out.printf("重置后: ε=%.2f%n", budget.getRemainingEpsilon()); // 5.00
```
