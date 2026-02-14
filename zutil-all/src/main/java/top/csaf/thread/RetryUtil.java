package top.csaf.thread;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Retry 重试工具类（基于 Resilience4j）。
 */
public class RetryUtil {

  private RetryUtil() {
  }

  /**
   * 默认 Retry 名称。
   */
  private static final String DEFAULT_NAME = "RetryUtil";

  /**
   * 默认配置：沿用 Resilience4j 默认值，并显式忽略中断异常重试。
   */
  private static final RetryConfig DEFAULT_CONFIG = RetryConfig.from(RetryConfig.ofDefaults())
    .ignoreExceptions(InterruptedException.class)
    .build();

  /**
   * 创建固定间隔的重试配置。
   *
   * @param maxAttempts 最大尝试次数（包含首次调用）
   * @param waitMillis 每次重试间隔（毫秒）
   * @return RetryConfig
   */
  public static RetryConfig config(int maxAttempts, long waitMillis) {
    if (maxAttempts <= 0) {
      throw new IllegalArgumentException("maxAttempts must be > 0");
    }
    if (waitMillis < 0) {
      throw new IllegalArgumentException("waitMillis must be >= 0");
    }
    return RetryConfig.custom()
      .maxAttempts(maxAttempts)
      .waitDuration(Duration.ofMillis(waitMillis))
      .ignoreExceptions(InterruptedException.class)
      .build();
  }

  /**
   * 创建自定义间隔与条件的重试配置。
   *
   * @param maxAttempts 最大尝试次数（包含首次调用）
   * @param intervalFunction 间隔函数
   * @param retryOnException 异常重试条件（可为 null）
   * @param retryOnResult 结果重试条件（可为 null）
   * @return RetryConfig
   */
  public static RetryConfig config(int maxAttempts, IntervalFunction intervalFunction,
                                   Predicate<Throwable> retryOnException, Predicate<?> retryOnResult) {
    if (maxAttempts <= 0) {
      throw new IllegalArgumentException("maxAttempts must be > 0");
    }
    Objects.requireNonNull(intervalFunction, "intervalFunction");

    RetryConfig.Builder<Object> builder = RetryConfig.custom()
      .maxAttempts(maxAttempts)
      .intervalFunction(intervalFunction)
      .ignoreExceptions(InterruptedException.class);

    if (retryOnException != null) {
      builder.retryOnException(retryOnException);
    }
    if (retryOnResult != null) {
      builder.retryOnResult(result -> testResultPredicate(retryOnResult, result));
    }
    return builder.build();
  }

  /**
   * 创建带结果类型校验的重试配置。
   *
   * @param maxAttempts 最大重试次数（包含首次调用）
   * @param intervalFunction 间隔函数
   * @param retryOnException 异常重试条件（可为 null）
   * @param resultType 结果类型（当 retryOnResult 非空时不能为空）
   * @param retryOnResult 结果重试条件（可为 null）。类型化重载下 null 结果固定不重试。
   * @param <T> 结果类型
   * @return RetryConfig
   */
  public static <T> RetryConfig config(int maxAttempts, IntervalFunction intervalFunction, Predicate<Throwable> retryOnException, Class<T> resultType, Predicate<? super T> retryOnResult) {
    if (retryOnResult == null) {
      return config(maxAttempts, intervalFunction, retryOnException, null);
    }
    Objects.requireNonNull(resultType, "resultType");

    return config(maxAttempts, intervalFunction, retryOnException, result -> {
      // 类型化重载中，null 结果默认不触发重试，避免把 null 传给不接收 null 的谓词。
      if (result == null) {
        return false;
      }
      if (!resultType.isInstance(result)) {
        return false;
      }
      return retryOnResult.test(resultType.cast(result));
    });
  }

  /**
   * 创建 Retry 实例。
   *
   * @param name Retry 名称
   * @param config RetryConfig（为 null 时使用默认值）
   * @return Retry
   */
  public static Retry retry(String name, RetryConfig config) {
    String useName = normalizeName(name);
    RetryConfig useConfig = config == null ? DEFAULT_CONFIG : config;
    return Retry.of(useName, useConfig);
  }

  /**
   * 使用配置执行带重试的 Callable。
   *
   * @param name Retry 名称
   * @param config RetryConfig（为 null 时使用默认值）
   * @param callable 任务
   * @param <T> 返回类型
   * @return 执行结果
   */
  public static <T> T execute(String name, RetryConfig config, Callable<T> callable) {
    return execute(retry(name, config), callable);
  }

  /**
   * 执行带重试的 Callable。
   *
   * @param retry Retry 实例
   * @param callable 任务
   * @param <T> 返回类型
   * @return 执行结果
   */
  public static <T> T execute(Retry retry, Callable<T> callable) {
    Objects.requireNonNull(retry, "retry");
    Objects.requireNonNull(callable, "callable");
    try {
      return retry.executeCallable(callable);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw wrapThrowable(e);
    }
  }

  /**
   * 执行带重试的 Supplier（无受检异常）。
   *
   * @param retry Retry 实例
   * @param supplier 任务
   * @param <T> 返回类型
   * @return 执行结果
   */
  public static <T> T executeSupplier(Retry retry, Supplier<T> supplier) {
    Objects.requireNonNull(retry, "retry");
    Objects.requireNonNull(supplier, "supplier");
    return retry.executeSupplier(supplier);
  }

  /**
   * 执行带重试的 Runnable。
   *
   * @param retry Retry 实例
   * @param runnable 任务
   */
  public static void run(Retry retry, Runnable runnable) {
    Objects.requireNonNull(retry, "retry");
    Objects.requireNonNull(runnable, "runnable");
    retry.executeRunnable(runnable);
  }

  /**
   * 将受检异常包装为运行时异常。
   */
  private static RuntimeException wrapThrowable(Exception e) {
    return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
  }

  private static boolean testResultPredicate(Predicate<?> retryOnResult, Object result) {
    try {
      return testResultPredicateTyped(retryOnResult, result);
    } catch (ClassCastException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> boolean testResultPredicateTyped(Predicate<T> retryOnResult, Object result) {
    return retryOnResult.test((T) result);
  }

  /**
   * 规范化名称，空名称回退到默认值。
   */
  private static String normalizeName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return DEFAULT_NAME;
    }
    return name;
  }
}
