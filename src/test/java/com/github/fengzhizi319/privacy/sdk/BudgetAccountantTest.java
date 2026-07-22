package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.exception.PrivacyBudgetExhaustedException;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link BudgetAccountant} 的单元测试。
 * <p>
 * 覆盖内存模式、SQLite 持久化以及时间窗口重置三类场景。
 * </p>
 */
class BudgetAccountantTest {

    private static final double EPS = 1e-9;

    @BeforeEach
    void setUp() {
        System.clearProperty("PRIVACY_BUDGET_DB");
        System.clearProperty("PRIVACY_BUDGET_WINDOW_SECONDS");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("PRIVACY_BUDGET_DB");
        System.clearProperty("PRIVACY_BUDGET_WINDOW_SECONDS");
    }

    /**
     * 测试同一 namespace 返回单例实例。
     */
    @Test
    void testSingleton() {
        BudgetAccountant first = BudgetAccountant.getInstance("singleton-ns", 10.0, 1e-4);
        BudgetAccountant second = BudgetAccountant.getInstance("singleton-ns", 5.0, 1e-5);
        assertSame(first, second);
    }

    /**
     * 测试内存模式下的 spend、remaining 与预算耗尽。
     */
    @Test
    void testInMemorySpendAndRemaining() {
        assumeTrue(
            System.getenv("PRIVACY_BUDGET_DB") == null,
            "PRIVACY_BUDGET_DB is set, in-memory mode is not active"
        );

        BudgetAccountant budget = BudgetAccountant.getInstance("mem-ns", 10.0, 1e-4);
        budget.spend(2.5, 1e-5);

        Map<String, Double> remaining = budget.remaining();
        assertEquals(7.5, remaining.get("epsilon"), EPS);
        assertEquals(9e-5, remaining.get("delta"), EPS);

        assertThrows(PrivacyBudgetExhaustedException.class, () -> budget.spend(8.0, 0.0));
        assertEquals(7.5, budget.remaining().get("epsilon"), EPS);
    }

    /**
     * 测试 SQLite 持久化：先在一个实例中消耗预算，再创建新实例仍能读取到持久化状态。
     */
    @Test
    void testSQLitePersistence() throws Exception {
        assumeTrue(
            System.getenv("PRIVACY_BUDGET_DB") == null,
            "PRIVACY_BUDGET_DB is set in environment, cannot override with a temp file"
        );

        Path dbFile = Files.createTempFile("budget-test", ".db");
        try {
            System.setProperty("PRIVACY_BUDGET_DB", dbFile.toString());
            String ns = "sqlite-persist-ns";

            BudgetAccountant first = new BudgetAccountant(ns, 10.0, 1e-4);
            first.spend(3.0, 1e-5);
            assertEquals(7.0, first.remaining().get("epsilon"), EPS);

            BudgetAccountant second = new BudgetAccountant(ns, 10.0, 1e-4);
            Map<String, Double> remaining = second.remaining();
            assertEquals(7.0, remaining.get("epsilon"), EPS);
            assertEquals(9e-5, remaining.get("delta"), EPS);

            assertThrows(PrivacyBudgetExhaustedException.class, () -> second.spend(8.0, 0.0));
        } finally {
            System.clearProperty("PRIVACY_BUDGET_DB");
            Files.deleteIfExists(dbFile);
        }
    }

    /**
     * 测试内存模式下的时间窗口重置：窗口到期后已消耗预算清零。
     */
    @Test
    void testTimeWindowResetInMemory() throws InterruptedException {
        assumeTrue(
            System.getenv("PRIVACY_BUDGET_DB") == null,
            "PRIVACY_BUDGET_DB is set, in-memory mode is not active"
        );

        BudgetAccountant budget = new BudgetAccountant("window-mem-ns", 10.0, 1e-4, 0.05);
        budget.spend(2.0, 1e-5);
        assertEquals(8.0, budget.remaining().get("epsilon"), EPS);

        Thread.sleep(100);
        assertEquals(10.0, budget.remaining().get("epsilon"), EPS);

        budget.spend(2.0, 0.0);
        assertEquals(8.0, budget.remaining().get("epsilon"), EPS);
    }

    /**
     * 测试 SQLite 模式下的时间窗口重置：窗口信息持久化，新实例仍能在窗口到期后重置。
     */
    @Test
    void testTimeWindowResetInSQLite() throws Exception {
        assumeTrue(
            System.getenv("PRIVACY_BUDGET_DB") == null,
            "PRIVACY_BUDGET_DB is set in environment, cannot override with a temp file"
        );

        Path dbFile = Files.createTempFile("budget-window-test", ".db");
        try {
            System.setProperty("PRIVACY_BUDGET_DB", dbFile.toString());
            String ns = "window-sqlite-ns";

            BudgetAccountant first = new BudgetAccountant(ns, 10.0, 1e-4, 0.05);
            first.spend(2.5, 1e-5);
            assertEquals(7.5, first.remaining().get("epsilon"), EPS);

            Thread.sleep(100);

            BudgetAccountant second = new BudgetAccountant(ns, 10.0, 1e-4, 0.05);
            Map<String, Double> remaining = second.remaining();
            assertEquals(10.0, remaining.get("epsilon"), EPS);
            assertEquals(1e-4, remaining.get("delta"), EPS);
        } finally {
            System.clearProperty("PRIVACY_BUDGET_DB");
            Files.deleteIfExists(dbFile);
        }
    }
}
