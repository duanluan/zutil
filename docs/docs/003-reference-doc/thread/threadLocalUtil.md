# ThreadLocalUtil 线程变量工具

> 📦 **包路径**：`top.csaf.thread.ThreadLocalUtil`
>
> 🔗 **所属模块**：`zutil-all`

**ThreadLocalUtil** 是一个基于阿里巴巴 **TransmittableThreadLocal (TTL)** 封装的线程上下文管理工具。
它主要解决了在使用**线程池**等会池化线程的执行组件情况下，`ThreadLocal` 值无法正确传递给子线程的问题。同时，它通过重写拷贝逻辑，确保了父子线程间的数据**隔离性**（Snapshot Copy）。

## ✨ 核心特性

* **跨线程池传递**：基于 TTL 实现，支持在使用线程池时将父线程的上下文传递给子任务。
* **数据隔离**：实现了类似 "快照" 的传递机制。子线程/子任务在创建时会**深拷贝**父线程的变量 Map。之后父子线程对各自变量的修改**互不影响**，彻底解决了并发修改同一 Map 导致的线程安全问题。
* **便捷管理**：内部维护了一个 `Map<String, Object>`，提供了类似 Redis 的 Key-Value 操作 API。

## 🚀 常用方法概览

### 1. 变量操作 (CRUD)

| 方法名                             | 描述                                          |
|:--------------------------------|:--------------------------------------------|
| `set(String key, Object value)` | 在当前线程上下文中存储一个键值对。                           |
| `get(String key)`               | 获取当前线程上下文中指定 Key 的值。                        |
| `getAll()`                      | 获取当前线程所有变量的**副本**（Map）。修改返回的 Map 不会影响当前上下文。 |
| `remove(String key)`            | 删除当前线程上下文中指定的 Key。                          |
| `clear()`                       | **清空**当前线程的所有上下文变量，并移除 ThreadLocal 本身。      |

### 2. 基础使用示例

```java
// 1. 设置变量
ThreadLocalUtil.set("userId", "1001");
ThreadLocalUtil.set("traceId", "abc-123");

// 2. 获取变量
String userId = (String) ThreadLocalUtil.get("userId");
// -> "1001"

// 3. 获取所有
Map<String, Object> all = ThreadLocalUtil.getAll();
// -> {userId=1001, traceId=abc-123}

// 4. 清理 (建议在 finally 块中调用)
ThreadLocalUtil.clear();
```

## 🧵 跨线程传递与隔离

**场景说明**

在微服务或高并发场景中，我们经常需要将`TraceId`或`UserInfo`传递给异步线程。

- **普通 ThreadLocal**：无法传递给线程池中的线程。
- **InheritableThreadLocal**：可以传递，但通常是引用传递（线程不安全），且在复用线程时会导致数据污染。
- **ThreadLocalUtil（本工具）**：配合 TTL 组件，完美解决上述问题。

**示例代码**

要实现跨线程池传递，需配合 TtlRunnable 或 TtlExecutors 使用。

```java
// 1. 主线程设置上下文
ThreadLocalUtil.set("context", "parent-value");

ExecutorService executor = Executors.newFixedThreadPool(1);

// 2. 使用 TtlRunnable 包装任务 (关键步骤)
Runnable task = TtlRunnable.get(() -> {
    // 子线程可以获取到父线程的值
    System.out.println(ThreadLocalUtil.get("context")); // -> "parent-value"
    
    // 3. 验证隔离性：子线程修改值
    ThreadLocalUtil.set("context", "child-modified");
    System.out.println(ThreadLocalUtil.get("context")); // -> "child-modified"
});

executor.submit(task).get();

// 4. 验证隔离性：主线程不受子线程影响
System.out.println(ThreadLocalUtil.get("context")); // -> "parent-value"
```

## ⚠️ 注意事项

1. **内存泄漏防范**：与所有`ThreadLocal`及其变种一样，务必确保在请求结束或任务完成后调用`ThreadLocalUtil.clear()`，特别是在 Web 容器（如 Tomcat）的线程池环境中使用时。
2. **TTL 依赖**：本工具类仅负责存储和隔离策略。要真正实现“跨线程池传递”，必须确保你的 Runnable/Callable 被 TTL 包装过（如使用`TtlRunnable.get()`），或者使用了 TTL Agent 方式启动应用。 
3. **Snapshot 机制**：传递给子线程的是数据的**拷贝**。如果在子线程启动后，父线程又新增了变量，这些**新变量不会同步**给已启动的子线程；反之亦然。
