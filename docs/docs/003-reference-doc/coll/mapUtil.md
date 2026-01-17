# MapUtil Map 工具类

> 📦 **包路径**：`top.csaf.coll.MapUtil`
>
> 🔗 **所属模块**：`zutil-core`

**MapUtil** 是对 `org.apache.commons.collections4.MapUtils` 的扩展与增强。
它完整代理了 Commons Collections 的基础功能（如类型安全获取、空判断、打印等），并重点补充了针对 **多 Key 探测**、**批量取值** 和 **优先取值** 的便捷方法。

## ✨ 核心特性

* **Commons Collections 增强**：包含 `getString`, `getInteger`, `safeAddToMap` 等所有标准操作，无需额外引入依赖。
* **多 Key 探测**：支持一次性判断多个 Key 是否存在，或获取第一个存在的 Key。
* **批量取值**：支持根据 Key 数组批量获取 Value，支持自定义是否填充 Null 或过滤 Null。
* **优先取值**：类似于“备选策略”，根据 Key 数组顺序，返回第一个存在的 Value 或第一个非空的 Value。

## 🚀 常用方法概览

### 1. 基础操作 (Commons Proxy)

直接复用了 Commons Collections 的成熟能力，提供类型安全的获取方法和集合操作。

| 方法名                              | 描述                    |
|:---------------------------------|:----------------------|
| `isEmpty(Map)`                   | 判断 Map 是否为 null 或空。   |
| `isNotEmpty(Map)`                | 判断 Map 是否不为 null 且非空。 |
| `getString(Map, key, [default])` | 获取 String 类型的值。       |
| `getInteger`, `getLong`, ...     | 获取各种基本数据类型的值。         |
| `safeAddToMap(Map, key, value)`  | 安全添加（避免空指针）。          |
| `toProperties(Map)`              | 转为 Properties 对象。     |
| `invertMap(Map)`                 | Key-Value 反转。         |

### 2. Key 存在性探测 (Key Existence)

用于在不知道具体哪个 Key 有效时，探测存在的 Key。

| 方法名                               | 描述                               |
|:----------------------------------|:---------------------------------|
| `containsKeys(Map, keys...)`      | 返回 Map 中**实际存在**的所有 Key 的列表。     |
| `containsKeysFirst(Map, keys...)` | 返回 Key 数组中**第一个在 Map 中存在**的 Key。 |

```java
Map<String, Object> map = new HashMap<>();
map.put("name", "zhangsan");
map.put("age", 18);

// 探测哪些 key 存在
List<String> keys = MapUtil.containsKeys(map, "name", "gender", "age");
// -> ["name", "age"] ("gender" 不存在)

// 获取第一个有效的 key
String firstKey = MapUtil.containsKeysFirst(map, "nickname", "name", "id");
// -> "name" ("nickname" 不存在，直接返回了第二个命中的 "name")
```

### 3. 批量取值 (Bulk Retrieval)

根据一组 Key 批量提取 Value，常用于数据清洗或 DTO 转换。

| 方法名                               | 描述                                                  |
|:----------------------------------|:----------------------------------------------------|
| `getAll(Map, keys...)`            | 获取存在的 Key 对应的 Value 列表（忽略不存在的 Key）。                 |
| `getAll(Map, isAddNull, keys...)` | 获取 Value 列表。`isAddNull=true` 时，若 Key 不存在则填充 `null`。 |
| `getAllNotNull(Map, keys...)`     | 获取 Value 列表，**排除**值为 `null` 的项。                     |

```java
Map<String, String> map = new HashMap<>();
map.put("a", "1");
map.put("b", null);
// map 中不存在 "c"

// 1. 默认获取 (忽略不存在的 key "c"，保留 null 值)
List<String> list1 = MapUtil.getAll(map, "a", "b", "c");
// -> ["1", null]

// 2. 填充 Null (不存在的 key "c" 填充 null)
List<String> list2 = MapUtil.getAll(map, true, "a", "b", "c");
// -> ["1", null, null]

// 3. 排除 Null (排除 value 为 null 的项)
List<String> list3 = MapUtil.getAllNotNull(map, "a", "b", "c");
// -> ["1"]
```

### 4. 优先取值 (Priority Retrieval)

按顺序查找，一旦找到符合条件的值立即返回。适用于“降级策略”或“多字段选一”场景。

| 方法名                           | 描述                                            |
|:------------------------------|:----------------------------------------------|
| `getAny(Map, keys...)`        | 返回**第一个存在 Key** 的 Value（即使 Value 是 null 也返回）。 |
| `getAnyNotNull(Map, keys...)` | 返回**第一个 Value 不为 null** 的值。                   |

```java
Map<String, String> config = new HashMap<>();
config.put("server_host", null);
config.put("default_host", "127.0.0.1");

// 场景：获取配置，优先取 server_host，没有则取 default_host

// getAny：只要 Key 存在就返回 (server_host 存在，值为 null)
String v1 = MapUtil.getAny(config, "server_host", "default_host");
// -> null

// getAnyNotNull：跳过 null 值，直到找到非空值
String v2 = MapUtil.getAnyNotNull(config, "server_host", "default_host");
// -> "127.0.0.1"
```

## ⚠️ 注意事项

1. **关于 `getAll`**：默认的 `getAll(map, keys)` 方法**不会**为不存在的 Key 填充 `null` 占位符，返回 List 的长度可能小于 Keys 数组的长度。
2. **关于 `getAny`**：该方法判断的是 `containsKey`。如果 Map 中显式存入了 `null` 值（如 `map.put("key", null)`），该方法会返回 `null` 并停止后续查找。如果需要忽略 `null` 值，请使用 `getAnyNotNull`。
