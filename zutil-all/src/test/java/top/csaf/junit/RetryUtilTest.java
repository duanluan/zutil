package top.csaf.junit;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.thread.RetryUtil;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RetryUtil tests")
class RetryUtilTest {

  @Test
  @DisplayName("config validation")
  void testConfigValidation() {
    assertThrows(IllegalArgumentException.class, () -> RetryUtil.config(0, 1L));
    assertThrows(IllegalArgumentException.class, () -> RetryUtil.config(1, -1L));
    assertThrows(IllegalArgumentException.class, () -> RetryUtil.config(0, attempt -> 0L, null, null));
    assertThrows(NullPointerException.class, () -> RetryUtil.config(1, null, null, null));
    assertThrows(NullPointerException.class, () -> RetryUtil.config(1, attempt -> 0L, null, null, String::isEmpty));
  }

  @Test
  @DisplayName("default name and config")
  void testRetryDefaultNameAndConfig() {
    Retry retryDefault = RetryUtil.retry("  ", null);
    assertEquals("RetryUtil", retryDefault.getName());

    RetryConfig config = RetryUtil.config(1, 0L);
    Retry retryCustom = RetryUtil.retry("custom", config);
    assertEquals("custom", retryCustom.getName());
  }

  @Test
  @DisplayName("retry on exception")
  void testExecuteWithRetryOnException() {
    RetryConfig config = RetryUtil.config(3, 0L);
    Retry retry = RetryUtil.retry("exception", config);
    AtomicInteger attempts = new AtomicInteger();
    String result = RetryUtil.execute(retry, () -> {
      int count = attempts.incrementAndGet();
      if (count < 3) {
        throw new IllegalStateException("boom");
      }
      return "ok";
    });
    assertEquals("ok", result);
    assertEquals(3, attempts.get());
  }

  @Test
  @DisplayName("retry on result")
  void testExecuteWithResultPredicate() {
    IntervalFunction intervalFunction = attempt -> 0L;
    RetryConfig config = RetryUtil.config(3, intervalFunction, null, Objects::isNull);
    Retry retry = RetryUtil.retry("result", config);
    AtomicInteger attempts = new AtomicInteger();
    String result = RetryUtil.execute(retry, () -> {
      int count = attempts.incrementAndGet();
      return count < 3 ? null : "ok";
    });
    assertEquals("ok", result);
    assertEquals(3, attempts.get());
  }

  @Test
  @DisplayName("retry on exception predicate")
  void testExecuteWithExceptionPredicate() {
    IntervalFunction intervalFunction = attempt -> 0L;
    RetryConfig config = RetryUtil.config(2, intervalFunction, ex -> ex instanceof IllegalStateException, null);
    Retry retry = RetryUtil.retry("predicate", config);
    AtomicInteger attempts = new AtomicInteger();
    String result = RetryUtil.execute(retry, () -> {
      int count = attempts.incrementAndGet();
      if (count == 1) {
        throw new IllegalStateException("boom");
      }
      return "ok";
    });
    assertEquals("ok", result);
    assertEquals(2, attempts.get());
  }

  @Test
  @DisplayName("no retry when exception predicate is false")
  void testExecuteWithExceptionPredicateFalseNoRetry() {
    IntervalFunction intervalFunction = attempt -> 0L;
    RetryConfig config = RetryUtil.config(3, intervalFunction, ex -> ex instanceof IllegalArgumentException, null);
    Retry retry = RetryUtil.retry("predicate-false", config);
    AtomicInteger attempts = new AtomicInteger();
    assertThrows(IllegalStateException.class, () -> RetryUtil.execute(retry, () -> {
      attempts.incrementAndGet();
      throw new IllegalStateException("boom");
    }));
    assertEquals(1, attempts.get());
  }

  @Test
  @DisplayName("execute with name and null config")
  void testExecuteWithNameAndConfig() {
    AtomicInteger attempts = new AtomicInteger();
    String result = RetryUtil.execute(" ", null, () -> {
      attempts.incrementAndGet();
      return "ok";
    });
    assertEquals("ok", result);
    assertEquals(1, attempts.get());
  }

  @Test
  @DisplayName("name + null config interrupted no retry")
  void testExecuteWithNameAndNullConfigInterruptedNoRetry() {
    Thread.interrupted();
    AtomicInteger attempts = new AtomicInteger();
    try {
      RuntimeException thrown = assertThrows(RuntimeException.class, () -> RetryUtil.execute(" ", null, (Callable<String>) () -> {
        attempts.incrementAndGet();
        throw new InterruptedException("stop");
      }));
      assertInstanceOf(InterruptedException.class, thrown.getCause());
      assertEquals(1, attempts.get());
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  @DisplayName("wrap checked exception")
  void testExecuteWrapCheckedException() {
    RetryConfig config = RetryUtil.config(1, 0L);
    Retry retry = RetryUtil.retry("checked", config);
    RuntimeException thrown = assertThrows(RuntimeException.class,
      () -> RetryUtil.execute(retry, (Callable<String>) () -> {
        throw new IOException("boom");
      }));
    assertInstanceOf(IOException.class, thrown.getCause());
  }

  @Test
  @DisplayName("interrupted exception no retry and interrupt restored")
  void testInterruptedExceptionNotRetriedAndInterruptRestored() {
    Thread.interrupted();
    Retry retry = RetryUtil.retry("interrupt", RetryUtil.config(3, 0L));
    AtomicInteger attempts = new AtomicInteger();
    try {
      RuntimeException thrown = assertThrows(RuntimeException.class, () -> RetryUtil.execute(retry, (Callable<String>) () -> {
        attempts.incrementAndGet();
        throw new InterruptedException("stop");
      }));
      assertInstanceOf(InterruptedException.class, thrown.getCause());
      assertEquals(1, attempts.get());
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  @DisplayName("runtime exception pass through")
  void testExecuteRuntimeExceptionPassThrough() {
    Retry retry = RetryUtil.retry("runtime", RetryUtil.config(1, 0L));
    assertThrows(IllegalArgumentException.class,
      () -> RetryUtil.execute(retry, (Callable<String>) () -> {
        throw new IllegalArgumentException("bad");
      }));
  }

  @Test
  @DisplayName("run and supplier")
  void testRunAndExecuteSupplier() {
    Retry retry = RetryUtil.retry("run", RetryUtil.config(2, 0L));
    AtomicInteger counter = new AtomicInteger();
    RetryUtil.run(retry, counter::incrementAndGet);
    assertEquals(1, counter.get());

    String value = RetryUtil.executeSupplier(retry, () -> "ok");
    assertEquals("ok", value);
  }

  @Test
  @DisplayName("typed result predicate mismatch no retry")
  void testTypedResultPredicateTypeMismatchNoRetry() {
    IntervalFunction intervalFunction = attempt -> 0L;
    RetryConfig config = RetryUtil.config(3, intervalFunction, null, String.class, String::isEmpty);
    Retry retry = RetryUtil.retry("typed-result", config);
    AtomicInteger attempts = new AtomicInteger();
    Object value = RetryUtil.execute(retry, () -> {
      attempts.incrementAndGet();
      return 123;
    });
    assertEquals(123, value);
    assertEquals(1, attempts.get());
  }

  @Test
  @DisplayName("typed result predicate match retries")
  void testTypedResultPredicateMatchRetry() {
    IntervalFunction intervalFunction = attempt -> 0L;
    RetryConfig config = RetryUtil.config(3, intervalFunction, null, String.class, String::isEmpty);
    Retry retry = RetryUtil.retry("typed-result-match", config);
    AtomicInteger attempts = new AtomicInteger();
    String value = RetryUtil.execute(retry, () -> {
      int count = attempts.incrementAndGet();
      return count < 3 ? "" : "ok";
    });
    assertEquals("ok", value);
    assertEquals(3, attempts.get());
  }

  @Test
  @DisplayName("typed result predicate null no retry")
  void testTypedResultPredicateNullNoRetry() {
    IntervalFunction intervalFunction = attempt -> 0L;
    RetryConfig config = RetryUtil.config(3, intervalFunction, null, String.class, String::isEmpty);
    Retry retry = RetryUtil.retry("typed-result-null", config);
    AtomicInteger attempts = new AtomicInteger();
    String value = RetryUtil.execute(retry, () -> {
      attempts.incrementAndGet();
      return null;
    });
    assertNull(value);
    assertEquals(1, attempts.get());
  }
}
