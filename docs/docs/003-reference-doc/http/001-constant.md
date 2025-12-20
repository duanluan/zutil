# HTTP 常量类文档

> 📦 **包路径**: `top.csaf.http.constant`
>
> 💡 **说明**: 本模块提供了 HTTP 请求中常用的请求方法、请求头名称以及 MIME 类型（Content-Type）的静态常量定义，旨在避免硬编码字符串，提高代码的可维护性和安全性。

## 1. 请求方法常量 (`ReqMethodConst`)

该类定义了标准的 HTTP 请求方法字符串。

| 常量名 | 值 | 说明 |
| :--- | :--- | :--- |
| `GET` | `"GET"` | 获取资源 |
| `POST` | `"POST"` | 提交资源 |
| `PUT` | `"PUT"` | 替换资源 |
| `DELETE` | `"DELETE"` | 删除资源 |
| `PATCH` | `"PATCH"` | 局部更新资源 |
| `HEAD` | `"HEAD"` | 获取报头 |

**使用示例**:
```java
import top.csaf.http.constant.ReqMethodConst;

// 在构建请求时使用
if (ReqMethodConst.GET.equals(method)) {
        // ...
        }
```

---

## 2. 请求头常量 (`HeaderConst`)

该类定义了常用的 HTTP Header 键名（Key）。

| 常量名 | 值 | 说明 |
| :--- | :--- | :--- |
| `CONTENT_TYPE` | `"Content-Type"` | 内容类型 |
| `CONTENT_LENGTH` | `"Content-Length"` | 内容长度 |
| `USER_AGENT` | `"User-Agent"` | 用户代理 |
| `ACCEPT` | `"Accept"` | 客户端可接收的内容类型 |
| `AUTHORIZATION` | `"Authorization"` | 授权认证信息 |
| `COOKIE` | `"Cookie"` | Cookie 信息 |
| `REFERER` | `"Referer"` | 来源页面 |
| `HOST` | `"Host"` | 目标主机 |
| `CONNECTION` | `"Connection"` | 连接管理（如 keep-alive） |
| ... | ... | 更多标准 Header 请查看源码 |

**特殊常量**:
* `USER_AGENT_X`: 一个通用的模拟浏览器 User-Agent 字符串（Mozilla/5.0 ... Chrome/xx...），用于伪装请求。

**使用示例**:
```java
import top.csaf.http.constant.HeaderConst;

Map<String, String> headers = new HashMap<>();
// 设置 Content-Type
headers.put(HeaderConst.CONTENT_TYPE, "application/json");
// 使用内置的模拟 User-Agent
headers.put(HeaderConst.USER_AGENT, HeaderConst.USER_AGENT_X);
```

---

## 3. 内容类型常量 (`ContentTypeConst`)

该类收录了极其详尽的 IANA 标准 MIME Type（媒体类型）。

> 🔗 **参考标准**: [IANA Media Types](https://www.iana.org/assignments/media-types/media-types.xhtml)

由于常量数量巨大（涵盖 Application, Audio, Font, Image, Message, Model, Multipart, Text, Video 等分类），此处仅列出最常用的几个，完整列表请在 IDE 中通过代码提示查看。

### 常用类型

| 常量名 | 值 | 说明 |
| :--- | :--- | :--- |
| `JSON` | `"application/json"` | JSON 数据 |
| `XML_APPLICATION` | `"application/xml"` | XML 数据 |
| `FORM_DATA` | `"multipart/form-data"` | 表单文件上传 |
| `X_WWW_FORM_URLENCODED` | `"application/x-www-form-urlencoded"` | 普通表单提交 |
| `HTML` | `"text/html"` | HTML 页面 |
| `TEXT_PLAIN` | `"text/plain"` | 纯文本 |
| `OCTET_STREAM` | `"application/octet-stream"` | 二进制流（文件下载） |
| `PDF` | `"application/pdf"` | PDF 文档 |
| `JPEG` / `JPG` | `"image/jpeg"` | JPEG 图片 |
| `PNG` | `"image/png"` | PNG 图片 |

**命名规则**:
* 常量名通常为大写，将 `/`、`.`、`+`、`-` 等符号转换为下划线 `_`。
* 例如：`application/vnd.ms-excel` -> `VND_MS_EXCEL`。

**使用示例**:
```java
import top.csaf.http.constant.ContentTypeConst;
import top.csaf.http.HttpUtil;

// 发送 JSON 请求
HttpUtil.post(url, ContentTypeConst.JSON, params);

// 发送文件上传请求
HttpUtil.post(url, ContentTypeConst.FORM_DATA, fileParams);
```
