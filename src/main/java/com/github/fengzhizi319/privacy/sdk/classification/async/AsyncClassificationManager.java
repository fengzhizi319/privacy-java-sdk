package com.github.fengzhizi319.privacy.sdk.classification.async;

import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationJob;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationJobStatus;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationResult;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步分类任务管理器。
 * <p>
 * 基于内存线程池执行分类任务，任务状态保存在内存中，支持 TTL 清理。
 * 任务状态流转：PENDING → RUNNING → DONE/FAILED。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class AsyncClassificationManager {

    /** 默认最大工作线程数。 */
    private static final int DEFAULT_MAX_WORKERS = 4;

    /** 默认任务保留时间（秒）。 */
    private static final long DEFAULT_TTL_SECONDS = 3600;

    /** 默认最大并发任务数。 */
    private static final int DEFAULT_MAX_JOBS = 1000;

    /** 清理间隔（秒）。 */
    private static final long CLEANUP_INTERVAL_SECONDS = 60;

    private final int maxWorkers;
    private final long ttlSeconds;
    private final int maxJobs;
    private final ExecutorService executor;
    private final ScheduledExecutorService cleanupScheduler;
    private final Map<String, ClassificationJob> jobs = new ConcurrentHashMap<>();
    private final AtomicInteger activeRunning = new AtomicInteger(0);

    /**
     * 使用默认配置构造管理器。
     */
    public AsyncClassificationManager() {
        this(DEFAULT_MAX_WORKERS, DEFAULT_TTL_SECONDS, DEFAULT_MAX_JOBS);
    }

    /**
     * 使用指定配置构造管理器。
     *
     * @param maxWorkers 最大工作线程数
     * @param ttlSeconds 已完成任务保留时间（秒）
     * @param maxJobs    最大并发任务数
     */
    public AsyncClassificationManager(int maxWorkers, long ttlSeconds, int maxJobs) {
        this.maxWorkers = maxWorkers > 0 ? maxWorkers : DEFAULT_MAX_WORKERS;
        this.ttlSeconds = ttlSeconds > 0 ? ttlSeconds : DEFAULT_TTL_SECONDS;
        this.maxJobs = maxJobs > 0 ? maxJobs : DEFAULT_MAX_JOBS;
        this.executor = Executors.newFixedThreadPool(this.maxWorkers, r -> {
            Thread t = new Thread(r, "cls-async-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            t.setDaemon(true);
            return t;
        });
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cls-async-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupScheduler.scheduleAtFixedRate(this::cleanup, CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 提交一个异步分类任务。
     *
     * @param task 任务逻辑，执行后返回 {@link ClassificationResult}
     * @return 任务 ID
     * @throws RuntimeException 当并发任务数超过最大限制时
     */
    public String submit(Callable<ClassificationResult> task) {
        if (activeRunning.get() >= maxJobs) {
            throw new RuntimeException("async classification job queue is full");
        }

        String jobId = "cls-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ClassificationJob job = new ClassificationJob();
        job.setJobId(jobId);
        job.setStatus(ClassificationJobStatus.PENDING);
        job.setCreatedAt(Instant.now().toString());
        jobs.put(jobId, job);

        executor.submit(() -> {
            activeRunning.incrementAndGet();
            try {
                job.setStatus(ClassificationJobStatus.RUNNING);
                ClassificationResult result = task.call();
                job.setResult(result);
                job.setStatus(ClassificationJobStatus.DONE);
                job.setFinishedAt(Instant.now().toString());
            } catch (Exception e) {
                job.setStatus(ClassificationJobStatus.FAILED);
                job.setError(e.getMessage());
                job.setFinishedAt(Instant.now().toString());
            } finally {
                activeRunning.decrementAndGet();
            }
        });
        return jobId;
    }

    /**
     * 查询异步任务状态与结果。
     *
     * @param jobId 任务 ID
     * @return 异步任务实例
     * @throws IllegalArgumentException 当任务不存在时
     */
    public ClassificationJob get(String jobId) {
        ClassificationJob job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("job not found: " + jobId);
        }
        return job;
    }

    /**
     * 关闭线程池与清理调度器。
     *
     * @param wait 是否等待正在执行的任务完成
     */
    public void shutdown(boolean wait) {
        if (wait) {
            executor.shutdown();
        } else {
            executor.shutdownNow();
        }
        cleanupScheduler.shutdown();
    }

    /**
     * 清理已过期的已完成任务。
     */
    private void cleanup() {
        Instant cutoff = Instant.now().minusSeconds(ttlSeconds);
        jobs.entrySet().removeIf(entry -> {
            ClassificationJob job = entry.getValue();
            String finishedAt = job.getFinishedAt();
            if (finishedAt == null) {
                return false;
            }
            try {
                return Instant.parse(finishedAt).isBefore(cutoff);
            } catch (Exception e) {
                return false;
            }
        });
    }
}
