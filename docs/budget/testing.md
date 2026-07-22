# 隐私预算测试策略

## 1. 测试概览

| 测试类型 | 覆盖范围 | 文件 |
|---|---|---|
| 单元测试 | spend/remaining/reset | `BudgetAccountantTest.java` |
| 并发测试 | 多线程竞争 | `BudgetAccountantTest.java` |
| 边界测试 | 零预算、耗尽 | `BudgetAccountantTest.java` |

## 2. 核心测试

```java
@Test
void testSpendAndRemaining() {
    BudgetAccountant b = BudgetAccountant.getInstance("test-spend", 10.0, 1e-3);
    b.reset();
    b.spend(3.0, 1e-4);
    assertEquals(7.0, b.getRemainingEpsilon(), 1e-9);
}

@Test
void testBudgetExhausted() {
    BudgetAccountant b = BudgetAccountant.getInstance("test-exhaust", 2.0, 1e-4);
    b.reset();
    b.spend(2.0, 1e-4);
    assertThrows(Exception.class, () -> b.spend(1.0, 0));
}

@Test
void testSingleton() {
    BudgetAccountant b1 = BudgetAccountant.getInstance("singleton", 10.0, 1e-3);
    BudgetAccountant b2 = BudgetAccountant.getInstance("singleton", 99.0, 1.0);
    assertSame(b1, b2);
}

@Test
void testReset() {
    BudgetAccountant b = BudgetAccountant.getInstance("test-reset", 5.0, 1e-4);
    b.spend(3.0, 0);
    b.reset();
    assertEquals(5.0, b.getRemainingEpsilon(), 1e-9);
}

@Test
void testConcurrentSpend() throws InterruptedException {
    BudgetAccountant b = BudgetAccountant.getInstance("concurrent", 100.0, 1.0);
    b.reset();
    AtomicInteger success = new AtomicInteger(0);
    ExecutorService pool = Executors.newFixedThreadPool(10);
    for (int i = 0; i < 200; i++) {
        pool.submit(() -> {
            try { b.spend(1.0, 0); success.incrementAndGet(); }
            catch (Exception ignored) {}
        });
    }
    pool.shutdown();
    pool.awaitTermination(5, TimeUnit.SECONDS);
    assertEquals(100, success.get());
}
```

## 3. 运行测试

```bash
mvn test -Dtest=BudgetAccountantTest
```

## 4. 测试检查清单

- [ ] spend 正确减少剩余
- [ ] 耗尽时抛出异常
- [ ] reset 恢复初始值
- [ ] 同 namespace 单例
- [ ] 不同 namespace 隔离
- [ ] 并发 spend 正确
