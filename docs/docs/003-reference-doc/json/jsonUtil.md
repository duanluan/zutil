# JsonUtil JSON 工具类

> 📦 **包路径**: `top.csaf.json.JsonUtil`
>
> 🔗 **所属模块**: `zutil-json`

**JsonUtil** 是基于 [Fastjson2](https://github.com/alibaba/fastjson2) 封装的 JSON 处理工具类。

它旨在简化日常开发中最常用的序列化与反序列化操作，同时提供了格式化、压缩、校验等实用功能。默认情况下，它优化了序列化策略（如保留 Null 值字段），更贴合前后端交互场景。

## ✨ 核心特性

* **基于 Fastjson2**: 继承了 Fastjson2 的极致性能和标准兼容性。
* **默认保留 Null 值**: 与 Fastjson 默认过滤 Null 值不同，`JsonUtil.toJson` 默认保留 Null 字段，方便前端处理。
* **灵活配置**: 所有的读写方法均支持传入 `JSONWriter.Feature` 或 `JSONReader.Feature` 可变参数。
* **实用增强**: 内置了 JSON 格式化、压缩、有效性校验及 URL 参数转 JSON 功能。

## 🚀 常用方法概览

### 1. 序列化 (Object -> JSON) 📤

将 Java 对象转换为 JSON 字符串。

| 方法名 | 描述 | 特性说明 |
| --- | --- | --- |
| `toJson(Object)` | **推荐**。转 JSON 字符串。 | **默认开启** `WriteMapNullValue` (保留值为 null 的字段)。 |
| `toJson(Object, Feature...)` | 转 JSON 字符串，自定义特性。 | 如果 `features` 为空，行为同上（保留 Null）；否则仅应用传入的特性。 |
| `toJsonNoFeature(Object)` | 转 JSON 字符串，无额外特性。 | 等同于原生 `JSON.toJSONString(obj)`，通常**不保留** Null 值。 |

**示例代码**:

```java
User user = new User("张三", null);

// 1. 默认行为 (保留 Null) -> {"name":"张三", "age":null}
String json1 = JsonUtil.toJson(user);

// 2. 无特性行为 (过滤 Null) -> {"name":"张三"}
String json2 = JsonUtil.toJsonNoFeature(user);
```

### 2. 反序列化 (JSON -> Object) 📥

将 JSON 字符串转换为 Java 对象或集合。

| 方法名 | 描述 |
| --- | --- |
| `parseObject` | 解析为单体对象 (POJO, Map 等) |
| `parseArray` | 解析为列表 (`List<T>`) |
| `parse` | 解析为通用对象 (`JSONObject`, `JSONArray` 等)，常用于未知结构的解析。 |

**示例代码**:

```java
String jsonStr = "{\"name\":\"李四\", \"age\":18}";

// 1. 转普通 Bean
User user = JsonUtil.parseObject(jsonStr, User.class);

// 2. 转 List
List<User> userList = JsonUtil.parseArray("[{...}]", User.class);

// 3. 通用解析 (支持单引号等非标准 JSON)
Object obj = JsonUtil.parse("{'a':1}"); // 返回 JSONObject
```

### 3. 工具方法 (Utils) 🛠️

处理 JSON 字符串的辅助功能。

| 方法名 | 描述 | 示例 |
| --- | --- | --- |
| `format` | 格式化 (美化) JSON 字符串 | 输出带缩进和换行的 JSON。 |
| `minify` | 压缩 JSON 字符串 | 去除空格和换行，减小体积。 |
| `isValid` | 验证字符串是否为有效 JSON | 返回 `true` / `false`。 |
| `paramsToJson` | URL 参数转 JSON | `a=1&b=2` -> `{"a":"1","b":"2"}` |

**示例代码**:

```java
// 格式化
String pretty = JsonUtil.format("{\"a\":1}");
/* 输出:
{
\t"a": 1
}
*/

// URL 参数转 JSON
String jsonParams = JsonUtil.paramsToJson("id=100&name=%E5%BC%A0%E4%B8%89");
// -> {"id":"100", "name":"张三"}
```

## ⚙️ 高级配置 (Features)

`JsonUtil` 的方法参数支持 Fastjson2 的 `Feature` 枚举。

**常用序列化特性 (JSONWriter.Feature)**:
* `WriteMapNullValue`: 输出值为 null 的字段 (默认开启)。
* `PrettyFormat`: 格式化输出。
* `WriteNullListAsEmpty`: 将 null 的 List 输出为 `[]`。

**常用反序列化特性 (JSONReader.Feature)**:
* `SupportSmartMatch`: 支持智能匹配（如驼峰下划线自动转换）。
* `InitStringFieldAsEmpty`: 将 null 的 String 字段初始化为 `""`。
* `AllowUnQuotedFieldNames`: 允许不带引号的字段名 (宽松模式)。
