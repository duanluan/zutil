# RetryUtil 重试工具

> 📦 **包路径**：`top.csaf.thread.RetryUtil`
>
> 🔗 **所属模块**：`zutil-all`

`RetryUtil`是基于 **Resilience4j Retry** 的轻量封装，提供了统一的重试配置与执行入口，重点处理了以下问题：

- 固定间隔或自定义策略重试
- 异常重试与结果重试
- 线程中断语义保护（`InterruptedException`默认不重试）
- 受检异常包装与 API 简化

## ✨ 核心特性

- 默认配置安全：默认忽略`InterruptedException`，避免中断被重复重试吞掉。
- 统一执行入口：支持`Callable`、`Supplier`、`Runnable`。
- 名称规范化：空白名称自动回退到默认名`RetryUtil`。
- 条件化重试：支持按异常类型、结果值、间隔函数组合策略。
- 类型化结果谓词：提供`Class<T> + Predicate<? super T>`重载，避免结果类型不匹配时抛出转换异常。

## 🚀 常用方法概览

### 1. 配置方法

| 方法                                                                                    | 说明                                            |
|:--------------------------------------------------------------------------------------|:----------------------------------------------|
| `config(int maxAttempts, long waitMillis)`                                            | 固定间隔配置。要求`maxAttempts > 0`、`waitMillis >= 0`。 |
| `config(int, IntervalFunction, Predicate<Throwable>, Predicate<?>)`                   | 自定义间隔、异常谓词、结果谓词。                              |
| `config(int, IntervalFunction, Predicate<Throwable>, Class<T>, Predicate<? super T>)` | 类型化结果谓词配置。`null`结果固定不重试。                      |

### 2. 执行方法

| 方法                                                               | 说明                                     |
|:-----------------------------------------------------------------|:---------------------------------------|
| `retry(String name, RetryConfig config)`                         | 创建`Retry`实例；`config == null`时使用内置默认配置。 |
| `execute(String name, RetryConfig config, Callable<T> callable)` | 直接按名称+配置执行。                            |
| `execute(Retry retry, Callable<T> callable)`                     | 按`Retry`实例执行。                          |
| `executeSupplier(Retry retry, Supplier<T> supplier)`             | 执行无受检异常任务。                             |
| `run(Retry retry, Runnable runnable)`                            | 执行无返回值任务。                              |

## 🧪 使用示例

### 1. 固定间隔 + 异常重试

```java
RetryConfig config = RetryUtil.config(3, 200L);
Retry retry = RetryUtil.retry("http-call", config);

String result = RetryUtil.execute(retry, () -> {
  // 前两次抛异常，第三次成功
  return remoteCall();
});
```

### 2. 结果重试（返回值不满足时重试）

```java
IntervalFunction interval = attempt -> 100L;
RetryConfig config = RetryUtil.config(
  3,
  interval,
  null,
  result -> result == null
);

String value = RetryUtil.execute(RetryUtil.retry("result-retry", config), () -> fetchMaybeNull());
```

### 3. 类型化结果谓词

```java
IntervalFunction interval = attempt -> 0L;
RetryConfig config = RetryUtil.config(
  3,
  interval,
  null,
  String.class,
  String::isEmpty
);

String value = RetryUtil.execute(RetryUtil.retry("typed", config), () -> "");
```

行为说明：

- 返回值是`String`且满足`String::isEmpty`时会重试
- 返回值类型不匹配时不会重试（不会抛`ClassCastException`）
- 返回值为`null`时不会重试

## ⚠️ 异常与中断语义

### 1. 中断异常处理

- 默认配置已忽略`InterruptedException`重试
- 执行层捕获`InterruptedException`后会调用`Thread.currentThread().interrupt()`恢复中断标记，再抛出`RuntimeException`

### 2. 异常透传与包装

- 运行时异常：保持原异常类型向外抛出
- 受检异常：包装为`RuntimeException`（`cause`保留原始异常）

## 📏 参数约束

-`maxAttempts`必须大于`0`
-`waitMillis`必须大于等于`0`
- 自定义配置时`intervalFunction`不能为空
- 类型化重载中：当`retryOnResult != null`时，`resultType`不能为空
