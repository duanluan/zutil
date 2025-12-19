# SnowFlake 雪花算法工具类

> 📦 **包路径**: `top.csaf.id.SnowFlake`
>
> 🔗 **所属模块**: `zutil-all`

`SnowFlake` 是基于 Twitter 雪花算法（Snowflake）实现的分布式唯一 ID 生成器。

它能够在分布式系统中生成唯一的、有序的、纯数字的 ID，且不依赖数据库，具有极高的生成性能。

## ✨ 核心特性

* **全局唯一**：结合时间戳、数据中心 ID、机器 ID 和序列号，确保 ID 不重复。
* **趋势有序**：ID 基于时间戳生成，整体呈递增趋势，对数据库索引友好。
* **高性能**：纯内存位运算，无数据库网络开销，支持每秒生成数百万个 ID。
* **时钟回拨支持**：
    * **小幅回拨 (≤ 5ms)**：自动阻塞等待，直至时间追上。
    * **大幅回拨 (> 5ms)**：抛出异常，防止生成重复 ID，保障数据安全。
* **自定义纪元 (Epoch)**：支持自定义起始时间戳，延长 ID 可用年限（默认支持约 69 年）。

## ❄️ ID 结构说明

生成的 ID 为 `long` 类型（64-bit），结构如下：

```text
0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
|   |                                             |       |       |
|   +---------------------------------------------+       |       +--> 序列号 (12位)
|   |                                                     |
+-->+ 符号位 (1位，固定为0)                                 +--> 机器 ID (5位)
    |                                                     |
    +--> 时间戳差值 (41位)                                  +--> 数据中心 ID (5位)
         (当前时间 - 起始时间)
```

* **1 位符号位**：固定为 0，表示正数。
* **41 位时间戳**：记录毫秒级时间差。可用约 69 年 `(2^41 / (1000*3600*24*365))`。
* **5 位数据中心 ID**：支持 $2^5 = 32$ 个数据中心（0-31）。
* **5 位机器 ID**：每个数据中心支持 $2^5 = 32$ 台机器（0-31）。
* **12 位序列号**：同一毫秒内支持生成 $2^{12} = 4096$ 个 ID。如果溢出，算法会自动等待下一毫秒。

## 🚀 快速开始

### 3.1 基础用法

最简单的用法是指定 `datacenterId` 和 `machineId` 直接创建实例。

```java
import top.csaf.id.SnowFlake;

public class IdDemo {
  public static void main(String[] args) {
    // 构造参数：(datacenterId, machineId)
    // 范围均为 0 ~ 31
    SnowFlake idWorker = new SnowFlake(1, 1);

    // 生成 ID
    long id = idWorker.next();
    System.out.println(id);
  }
}
```

### 3.2 推荐用法：单例模式

由于 `SnowFlake` 是有状态的（需要维护 `lastTimestamp` 和 `sequence`），**必须**在应用中作为**单例**使用。严禁每次生成 ID 时 `new SnowFlake(...)`，否则极大概率产生重复 ID。

建议将其封装在工具类或 Spring Bean 中：

```java
// 封装示例：IdUtil
public class IdUtil {
  // 实际场景中，机器ID应通过配置文件或环境变量注入，避免冲突
  // 建议设置一个固定的起始时间 (startTimeMillis)，例如项目立项时间
  private static final SnowFlake WORKER = new SnowFlake(1, 1, 1767225600000L); // 2026-01-01

  public static long nextId() {
    return WORKER.next();
  }
}
```

## ⚙️ 高级配置

### 4.1 自定义起始时间 (Epoch)

为了最大化利用 69 年的使用寿命，建议在项目初始化时设置一个**固定的、近期的**起始时间（Twepoch）。

```java
// 设定起始时间为 2026-01-01 00:00:00 UTC (1767225600000L)
long projectStartTime = 1767225600000L; 

SnowFlake idWorker = new SnowFlake(1, 1, projectStartTime);
```

> **⚠️ 警告**：
> 一旦项目上线并生成了 ID，**严禁修改起始时间**。
> * 改早：可能导致 ID 排序混乱。
> * 改晚：会导致生成的 ID 变小，极大概率与旧 ID 冲突（重复）。
> * 时间不能晚于当前系统时间。

### 4.2 机器 ID 分配策略

在集群部署时，必须保证每台服务实例的 `(datacenterId, machineId)` 组合是唯一的。常用策略包括：

1.  **静态配置**：在 `application.yml` 或启动脚本中手动指定。
2.  **Redis 分配**：应用启动时利用 Redis 的 `INCR` 命令获取一个唯一的 WorkerID。
3.  **Zookeeper/Etcd**：利用临时顺序节点获取 ID。
4.  **IP哈希**：取 IP 地址最后一段取模（适合开发测试环境，生产环境有冲突风险）。

## 🛡️ 异常处理

`SnowFlake` 内部对时钟回拨进行了严格校验：

* **回拨时间 ≤ 5ms**：
  线程会自动阻塞（`wait`），等待系统时钟追上来，然后继续生成 ID。这对业务透明，无感知。
* **回拨时间 > 5ms**：
  直接抛出 `RuntimeException("Clock moved backwards...")`。此时应检查服务器时间同步服务（如 NTP）是否异常。

## 📊 性能压测参考

* **并发量**：理论上限为 `4096 ID/ms`，即 **409.6万 ID/秒**。
* **线程安全**：`next()` 方法已使用 `synchronized` 关键字修饰，多线程并发安全。

## 📚 API 参考

### 构造方法

| 方法签名 | 描述 |
| :--- | :--- |
| `SnowFlake(long datacenterId, long machineId)` | 使用默认起始时间创建实例。 |
| `SnowFlake(long datacenterId, long machineId, long startTimeMillis)` | 使用自定义起始时间创建实例。 |

### 核心方法

| 方法签名 | 返回值 | 描述 | 异常 |
| :--- | :--- | :--- | :--- |
| `next()` | `long` | 获取下一个唯一 ID | `RuntimeException` (时钟回拨严重时) |
