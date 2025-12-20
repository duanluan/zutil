# UuidUtil 统一 UUID 工具类

> 📦 **包路径**: `top.csaf.id.UuidUtil`
>
> 🔗 **所属模块**: `zutil-all`

`UuidUtil` 是基于 `f4b6a3/uuid-creator` 的全功能 UUID 工具箱。它不仅支持 RFC 9562 (V7) 等全版本生成，还提供了强大的格式转换（Base62/Base64）和时间戳回溯能力。

## ✨ 核心特性

* **V7 首选**: 默认推荐使用 V7 版本，**单调递增**，包含**时间戳**，是数据库主键的完美选择。
* **多格式支持**: 支持 Hex (32位)、Base62 (短链友好)、Base64 (URL安全)、URN 等多种格式互转。
* **时间回溯**: 可以直接从 V7/V1 UUID 中提取生成时间，无需查询数据库 `create_time`。
* **高性能**: 底层采用无锁设计和位运算优化。

## 🚀 快速开始

### 1. 生成 ID (Generation)

```java
import top.csaf.id.UuidUtil;

// [推荐] 生成 V7 (有序、含时间、数据库友好)
UUID v7 = UuidUtil.v7();

// 生成 V4 (完全随机)
UUID v4 = UuidUtil.v4();

// 生成 32 位无横线字符串 (基于 V7)
String simpleId = UuidUtil.nextSimple(); 
// -> "018e6b121c2d741193d3123456789abc"
```

### 2. 格式转换 (Conversion)

利用 `Base62` 可以将 UUID 压缩到 22 位，非常适合作为**短链接**或**对外暴露的 ID**。

```java
UUID uuid = UuidUtil.v7();

// 转为 Base62 (22 chars)
String shortId = UuidUtil.toBase62(uuid); 
// -> "05W6p3l447Q5L3237E4321"

// 还原
UUID original = UuidUtil.parseBase62(shortId);
```

### 3. 时间回溯 (Time Extraction)

V7 UUID 自带毫秒级时间戳。

```java
UUID id = UuidUtil.v7();

// 直接获取创建时间
Instant createTime = UuidUtil.getInstant(id);
System.out.println(createTime); 
// -> 2025-12-20T12:00:00.123Z
```

## 📊 版本选型指南

| 版本 | 描述 | 适用场景 | 排序性 | 包含时间 |
| :--- | :--- | :--- | :--- | :--- |
| **V7** | **Unix Epoch 时间戳** | **数据库主键 (最佳)**、分布式系统 | ✅ 严格 | ✅ |
| **V4** | 完全随机 | 临时 Token、无需排序的标识 | ❌ 无 | ❌ |
| **V1** | Gregorian 时间 + MAC | 需要回溯生成机器的场景 | ❌ 无 | ✅ |
| **V3/V5**| 基于名称 Hash | 输入相同名称需得到相同 ID | ❌ 无 | ❌ |
