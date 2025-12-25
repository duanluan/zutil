# JsonUtil JSON 工具类

> 📦 **包路径**: `top.csaf.json.JsonUtil`
> 
> 🔗 **所属模块**: `zutil-json`

**JsonUtil** 是基于 [Fastjson2](https://github.com/alibaba/fastjson2) 封装的 JSON 处理工具类。

它旨在简化日常开发中最常用的序列化与反序列化操作，同时保留了 Fastjson2 强大的特性（Feature）配置能力。默认情况下，它优化了序列化策略（如保留 Null 值字段），更贴合前后端交互场景。

## ✨ 核心特性

* **基于 Fastjson2**: 继承了 Fastjson2 的极致性能和标准兼容性。
* **默认保留 Null 值**: 与 Fastjson 默认过滤 Null 值不同，`JsonUtil.toJson` 默认保留 Null 字段，方便前端处理。
* **灵活配置**: 所有的读写方法均支持传入 `JSONWriter.Feature` 或 `JSONReader.Feature` 可变参数，实现高度定制。
* **精简 API**: 统一了 `toJson`, `parseObject`, `parseArray` 入口，降低心智负担。

## 🚀 常用方法概览

### 1. 序列化 (Object -> JSON) 📤

将 Java 对象转换为 JSON 字符串。

| 方法名 | 描述 | 特性说明 |
| --- | --- | --- |
| `toJson(Object)` | **推荐**。转 JSON 字符串。 | **默认开启** `WriteMapNullValue` (保留值为 null 的字段)。 |
| `toJson(Object, Feature...)` | 转 JSON 字符串，自定义特性。 | 如果 `features` 为空，行为同上（保留 Null）；否则仅应用传入的特性。 |
| `toJsonNoFeature(Object)` | 转 JSON 字符串，无额外特性。 | 等同于原生 `JSON.toJSONString(obj)`，通常**不保留** Null 值。 |

**示例代码**:

假设有一个 User 对象：`User(name="张三", age=null)`

```java
User user = new User("张三", null);

// 1. 默认行为 (保留 Null)
String json1 = JsonUtil.toJson(user);
// -> {"name":"张三", "age":null}

// 2. 无特性行为 (原生 Fastjson2 默认行为，过滤 Null)
String json2 = JsonUtil.toJsonNoFeature(user);
// -> {"name":"张三"}

// 3. 自定义特性 (例如：格式化输出 PrettyFormat)
String json3 = JsonUtil.toJson(user, JSONWriter.Feature.PrettyFormat);
// -> 
// {
//   "name":"张三"
// }
```

### 2. 反序列化 (JSON -> Object) 📥

将 JSON 字符串转换为 Java 对象或集合。

| 方法名 | 描述 |
| --- | --- |
| `parseObject` | 解析为单体对象 (POJO, Map 等) |
| `parseArray` | 解析为列表 (`List<T>`) |

**示例代码**:

```java
String jsonStr = "{\"name\":\"李四\", \"age\":18}";
String arrayStr = "[{\"name\":\"王五\"}, {\"name\":\"赵六\"}]";

// 1. 转普通 Bean
User user = JsonUtil.parseObject(jsonStr, User.class);

// 2. 转 List
List<User> userList = JsonUtil.parseArray(arrayStr, User.class);

// 3. 带特性反序列化 (例如：忽略不存在的字段等，视 Fastjson2 支持而定)
User user2 = JsonUtil.parseObject(jsonStr, User.class, JSONReader.Feature.SupportSmartMatch);
```

## ⚙️ 高级配置 (Features)

`JsonUtil` 的方法参数支持 Fastjson2 的 `Feature` 枚举，用于控制序列化和反序列化的具体行为。

更多详细配置请参考 Fastjson2 官方文档：
🔗 [序列化和反序列化行为 (Features)](https://alibaba.github.io/fastjson2/features_cn.html)

**常用序列化特性 (JSONWriter.Feature)**:

* `WriteMapNullValue`: 输出值为 null 的字段 (JsonUtil 默认已开启)。
* `PrettyFormat`: 格式化输出 JSON (美化)。
* `WriteNullListAsEmpty`: 将 null 的 List 字段输出为 `[]`。
* `WriteNullStringAsEmpty`: 将 null 的 String 字段输出为 `""`。

**常用反序列化特性 (JSONReader.Feature)**:

* `SupportSmartMatch`: 支持智能匹配（如驼峰下划线自动转换）。
* `IgnoreNotMatch`: 忽略不匹配的字段。
