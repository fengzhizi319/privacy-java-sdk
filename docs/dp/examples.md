# 差分隐私使用示例

## 1. 基本计数

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class DpCountExample {
    public static void main(String[] args) {
        PrivacyClient client = PrivacyClient.builder()
            .namespace("my-app")
            .epsilon(10.0)
            .delta(1e-4)
            .build();

        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);

        // Laplace 机制
        double noisyCount = client.dpCount(values, 1.0, 1e-5, "laplace");
        System.out.printf("DP Count (Laplace): %.2f (真实值: %d)%n", noisyCount, values.size());

        // Gaussian 机制
        double noisyCountG = client.dpCount(values, 1.0, 1e-5, "gaussian");
        System.out.printf("DP Count (Gaussian): %.2f%n", noisyCountG);
    }
}
```

## 2. 带 Clipping 的求和

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class DpSumExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<Double> salaries = Arrays.asList(
            5000.0, 8000.0, 12000.0, 15000.0, 100000.0  // 含异常值
        );

        // 裁剪到 [0, 20000] 后求和
        double noisySum = client.dpSum(salaries, 1.0, 1e-5, "laplace", 0.0, 20000.0);
        System.out.printf("DP Sum (clipped): %.2f%n", noisySum);
    }
}
```

## 3. 均值计算

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;

public class DpMeanExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<Double> ages = Arrays.asList(25.0, 30.0, 35.0, 40.0, 45.0);

        double noisyMean = client.dpMean(ages, 1.0, 1e-5, "laplace", 0.0, 100.0);
        System.out.printf("DP Mean: %.2f (真实均值: %.2f)%n",
            noisyMean, ages.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }
}
```

## 4. 直方图

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DpHistogramExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<String> cities = Arrays.asList(
            "北京", "上海", "北京", "广州", "上海", "北京", "深圳"
        );
        List<String> categories = Arrays.asList("北京", "上海", "广州", "深圳");

        Map<String, Double> hist = client.dpHistogram(cities, categories, 1.0, 1e-5, "laplace");
        hist.forEach((city, count) ->
            System.out.printf("  %s: %.2f%n", city, count));
    }
}
```

## 5. 对已聚合值加噪

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

public class NoisyAggExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        // 数据库已聚合出真实计数
        long trueCount = 1500;
        double noisyCount = client.dpNoisyCount(trueCount, 0.5, 1e-5, "laplace");
        System.out.printf("真实计数: %d, DP计数: %.2f%n", trueCount, noisyCount);

        // 已聚合求和
        double trueSum = 50000.0;
        double sensitivity = 1000.0; // 单条记录最大贡献
        double noisySum = client.dpNoisySum(trueSum, sensitivity, 0.5, 1e-5, "laplace");
        System.out.printf("真实总和: %.0f, DP总和: %.2f%n", trueSum, noisySum);
    }
}
```

## 6. 预算管控

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import java.util.Arrays;
import java.util.List;

public class BudgetExample {
    public static void main(String[] args) {
        PrivacyClient client = PrivacyClient.builder()
            .namespace("budget-demo")
            .epsilon(3.0)  // 总预算 ε=3
            .delta(1e-4)
            .build();

        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);

        // 每次消耗 ε=1.0，最多查询 3 次
        for (int i = 1; i <= 5; i++) {
            try {
                double result = client.dpCount(data, 1.0, 1e-5, "laplace");
                System.out.printf("第 %d 次查询: %.2f%n", i, result);
            } catch (Exception e) {
                System.out.printf("第 %d 次查询失败: %s%n", i, e.getMessage());
                break;
            }
        }

        // 查看剩余预算
        BudgetAccountant budget = client.budget();
        System.out.printf("剩余预算: ε=%.2f%n", budget.getRemainingEpsilon());
    }
}
```

## 7. 分组聚合

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;
import java.util.*;

public class GroupByExample {
    public static void main(String[] args) {
        PrivacyClient client = new PrivacyClient();

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("dept", "工程", "salary", 15000.0));
        rows.add(Map.of("dept", "工程", "salary", 18000.0));
        rows.add(Map.of("dept", "市场", "salary", 12000.0));
        rows.add(Map.of("dept", "市场", "salary", 13000.0));

        Map<String, Double> result = client.dp()
            .groupBy(rows, "dept", "salary", 1.0, 1e-5, "laplace");

        result.forEach((dept, sum) ->
            System.out.printf("  %s 部门 DP 薪资总和: %.2f%n", dept, sum));
    }
}
```
