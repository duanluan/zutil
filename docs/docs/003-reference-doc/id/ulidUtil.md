# UlidUtil ULID 生成器

> 📦 **包路径**: `top.csaf.id.UlidUtil`
>
> 🔗 **所属模块**: `zutil-all`

**UlidUtil** 是对高性能库 `ulid-creator` 的封装，提供 **ULID** (Universally Unique Lexicographically Sortable Identifier) 的生成与校验功能。

它结合了 UUID 的唯一性和时间戳的有序性，生成的 ID 是 26 位 URL 安全的字符串，非常适合作为 **数据库主键**、**NoSQL 键** 或需要 **按时间排序** 的业务 ID。

## ✨ 核心特性

* **字典序有序**: 前 48 位为毫秒级时间戳，生成的 ID 按字符串排序即按时间排序。
* **严格单调 (可选)**: 提供 `nextMonotonicUlid()`，在同一毫秒内生成的 ID 也能保证顺序递增，对数据库 B+ 树索引极度友好。
* **URL 安全与容错**: 使用 Crockford's Base32 编码。为了防止人工输入错误，**I, L 会被自动识别为 1，O 会被识别为 0**。
* **高性能**: 基于 `ulid-creator` 库，采用优化的位运算和随机数生成策略。

## ❄️ ID 结构说明

ULID 总长 128 位 (26 个 Base32 字符)，结构如下：

```text
 01AN4Z07BY      79KA1307SR9X4MV3
|          |    |                |
+----------+    +----------------+
 10个字符(48bit)    16个字符(80bit)
    时间戳             随机数
```

* **时间戳**: 精确到毫秒，可延续至公元 10889 年。
* **随机数**: 80 位。在单调模式下，如果时间戳未变，随机数部分会自动 `+1`。

## 🚀 快速开始

### 3.1 基础用法 (标准 ULID)

适用于大多数场景，性能最高。

```java
import top.csaf.id.UlidUtil;

String ulid = UlidUtil.nextUlid();
// -> "01AN4Z07BY79KA1307SR9X4MV3"
```

### 3.2 单调递增 ULID (Monotonic)

适用于需要**严格排序**或**高频写入数据库主键**的场景。
在同一毫秒内，它会利用随机数空间进行递增，防止索引页分裂。

```java
String id1 = UlidUtil.nextMonotonicUlid();
String id2 = UlidUtil.nextMonotonicUlid();
// id2 必定大于 id1 (字典序)
```

### 3.3 格式校验与解析

```java
// 校验格式 (支持 L/I/O 容错)
boolean isValid = UlidUtil.isValid("01AN4Z07BY79KA1307SR9X4MVL"); // -> true (L被视为1)

// 提取时间戳
long timestamp = UlidUtil.getTimestamp("01AN4Z07BY79KA1307SR9X4MV3");
System.out.println(new Date(timestamp));
```

## 📊 选型建议

| 特性 | SnowFlake | UUID v7 | ULID |
| :--- | :--- | :--- | :--- |
| **长度** | 64-bit (Long) | 128-bit | 128-bit (26 chars) |
| **类型** | 数字 | 对象/字符串 | 字符串 |
| **可读性** | 差 | 一般 (带横线) | **优 (短字符串)** |
| **数据库** | **MySQL 主键首选** | 通用主键 | NoSQL / 分布式数据库 |
| **依赖** | 需配置机器 ID | 无 | 无 |
| **有序性** | ✅ 严格有序 | ✅ 趋势有序 | ✅ 趋势/严格有序 |

## 🛡️ 常见问题

**Q: 为什么 isValid 对包含 L 的字符串返回 true?**
A: ULID 标准虽然不包含 I, L, O, U，但许多实现（包括本工具使用的库）为了鲁棒性，允许在解码/校验时将易混淆字符映射回数字（I/L->1, O->0）。

**Q: 单调 ID 在多实例部署时还有效吗？**
A: 单调性（Monotonicity）仅在**单 JVM 实例**内保证。多实例之间依赖毫秒级时间戳排序，整体依然是趋势有序的，足以满足大多数分布式场景需求。
