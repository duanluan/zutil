# HttpUtil HTTP 工具类

> 📦 **包路径**: `top.csaf.http.HttpUtil`
>
> 🔗 **说明**: 本工具类基于 `OkHttps` 封装，已内置 Fastjson2 支持，开箱即用。

**HttpUtil** 是一个轻量级、静态方法的 HTTP 同步请求工具类。它旨在简化常见的 HTTP 请求操作，提供“宽容模式”的 API 设计，支持多种 JSON 解析方式，让网络请求变得简单直观。

## ✨ 特性

* **开箱即用**: 全静态方法，无需实例化。
* **宽容模式**: 对 `null` 参数进行宽容处理（如 Content-Type、ResultClass），减少调用侧的判空代码。
* **智能解析**: 自动识别并支持 Jackson (`JsonNode`)、Fastjson2 (`JSONObject`)、Gson (`JsonObject`) 以及 POJO 对象的自动转换。
* **全方法支持**: 覆盖 GET, POST, PUT, PATCH, DELETE 等主流 HTTP 方法。
* **便捷工具**: 提供 URL 参数拼接 (`toUrlParams`) 和解析 (`toMapParams`) 等实用方法。

## 🛠️ 引入依赖

请在项目中引入 `zutil-http` 即可，**无需**额外手动引入 `okhttps` 或 `fastjson2`（已自动包含）。

```xml
<dependency>
    <groupId>top.csaf</groupId>
    <artifactId>zutil-http</artifactId>
    <version>最新版本</version>
</dependency>
```

## 🚀 快速开始

> 💡 **提示**：为了代码规范，建议配合 `HeaderConst` 和 `ContentTypeConst` 使用，详见 [常量类文档](./001-constant.md)。

### 1. 发起 GET 请求

```java
// 1. 最简单的请求，返回 String
String html = HttpUtil.get("[https://example.com](https://example.com)");

// 2. 带参数，返回 Fastjson2 JSONObject
Map<String, Object> params = new HashMap<>();
params.put("keyword", "java");
JSONObject json = HttpUtil.get("[https://api.example.com/search](https://api.example.com/search)", params, JSONObject.class);

// 3. 返回 POJO 对象
User user = HttpUtil.get("[https://api.example.com/user/1](https://api.example.com/user/1)", User.class);
```

### 2. 发起 POST 请求

```java
// 1. 提交 JSON 数据 (Content-Type 默认为 application/json 或由 OkHttps 决定)
Map<String, Object> body = new HashMap<>();
body.put("username", "admin");
HttpResult result = HttpUtil.post("[https://api.example.com/login](https://api.example.com/login)", body);

// 2. 指定 Content-Type
HttpUtil.post("[https://api.example.com/upload](https://api.example.com/upload)", "multipart/form-data", params);
```

### 3. 自定义 Header

```java
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token123");

// 使用 getByHeader / postByHeader 等系列方法
String response = HttpUtil.getByHeader("[https://api.example.com/secure](https://api.example.com/secure)", headers, String.class);
```

## 📚 API 详解

### 基础工具方法

| 方法名 | 描述 | 示例 |
| :--- | :--- | :--- |
| `toUrlParams(Map)` | 将 Map 转为 URL 查询字符串（如 `?a=1&b=2`） | `HttpUtil.toUrlParams(map)` |
| `toMapParams(String)` | 将 URL 查询字符串转为 Map | `HttpUtil.toMapParams("http://x?a=1")` |
| `getContentLength(Map)` | 计算请求体参数转为 JSON 后的长度（UrlEncoded） | `HttpUtil.getContentLength(map)` |

### HTTP 请求方法

所有请求方法均支持以下重载形式，以 `get` 为例，`post/put/patch/delete` 同理：

1.  **最简形式**:
    `HttpResult get(String url)`
2.  **带参数**:
    `HttpResult get(String url, Map<String, Object> params)`
3.  **带返回类型**:
    `<T> T get(String url, Class<T> resultClass)`
4.  **带参数和返回类型**:
    `<T> T get(String url, Map<String, Object> params, Class<T> resultClass)`
5.  **全参数 (指定 Content-Type)**:
    `<T> T get(String url, String contentType, Map<String, Object> params, Class<T> resultClass)`

**Header 增强版**:
使用 `getByHeader`, `postByHeader` 等方法可额外传入 `Map<String, String> headers`。

### 🔄 响应类型自动转换

`resultClass` 参数支持多种类型，工具类会自动处理解析逻辑：

| 传入类型 (`resultClass`) | 返回结果 | 说明 |
| :--- | :--- | :--- |
| `null` | `HttpResult` | 返回原始 OkHttps 结果对象 (含状态码、Header 等) |
| `String.class` | `String` | 返回响应体字符串 |
| `HttpResult.Body.class` | `Body` | 返回原始 Body 对象 |
| **Jackson** | | |
| `JsonNode.class` | `JsonNode` | Jackson 树节点 |
| `ObjectNode.class` | `ObjectNode` | Jackson 对象节点 |
| `ArrayNode.class` | `ArrayNode` | Jackson 数组节点 |
| **Fastjson2** | | |
| `JSONObject.class` | `JSONObject` | Fastjson2 对象 |
| `JSONArray.class` | `JSONArray` | Fastjson2 数组 |
| `JSON.class` | `JSONObject/Array` | 通用 JSON 对象 |
| **Gson** | | |
| `JsonObject.class` | `JsonObject` | Gson 对象 |
| `JsonArray.class` | `JsonArray` | Gson 数组 |
| **POJO** | `T` | 任意自定义 Bean 类型 |

## ⚙️ 异常处理

* **参数校验**: `url` 为空时抛出 `IllegalArgumentException`。
* **网络异常**: 连接超时、断网等情况会抛出 `OkHttpsException` 或 `ConnectException`，**不会返回 null**，请在调用侧捕获处理。
* **解析异常**: JSON 格式错误时会记录 `log.error` 并尝试返回 null 或抛出异常（视具体 JSON 库行为而定）。

## 📝 最佳实践

1.  **推荐使用 POJO**: 直接传入 `User.class` 获取对象，代码更优雅。
2.  **处理异常**: 建议在外部使用 `try-catch` 包裹网络请求，以应对不稳定的网络环境。
3.  **宽容模式**: 除了 `url`，其他参数（如 `contentType`, `headers`）如果不需要，直接传 `null` 即可，无需构造空对象。
