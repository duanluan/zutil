# XmlUtil XML 工具类

> 📦 **包路径**：`top.csaf.xml.XmlUtil`
>
> 🔗 **所属模块**：`zutil-all`

**XmlUtil** 是基于 `dom4j` 的轻量级封装工具类。
它提供了从多种源（文件、流、URL 等）读取 XML、将 XML 写入文件以及**XML 与 JSON/JavaBean 互转**的便捷功能。

## ✨ 核心特性

* **多源读取**：支持从 `File`、`InputStream`、`URL`、`Reader`、`InputSource` 或 `String` 路径读取 XML 文档。
* **容错处理**：读取和写入过程中的 IO 异常或解析异常会被捕获并记录日志（返回 `null`），避免中断业务流程。
* **格式化输出**：支持使用 `dom4j.io.OutputFormat` 控制输出格式（缩进、编码等）。
* **XML 转 JSON/对象**：内置了智能的递归算法，可将 XML 节点转为 JSON 字符串、Java Bean 或 List 集合，自动处理列表和单对象的结构差异。

## 🚀 常用方法概览

### 1. 读取与解析 (Read & Parse)

用于将外部资源加载为 dom4j 的 `Document` 对象。所有读取方法均支持传入自定义的 `SAXReader`；若不传，则使用默认配置。

| 方法名                    | 描述                                                                                       |
|:-----------------------|:-----------------------------------------------------------------------------------------|
| `read(SAXReader, ...)` | 从指定源读取 XML。支持 `File`, `URL`, `InputStream`, `Reader`, `InputSource`, `String` (路径/文本) 等。 |
| `parse(String text)`   | 将 **XML 字符串内容** 直接解析为 `Document` 对象。                                                     |

**示例代码**：

```java
// 1. 解析 XML 字符串
String xmlStr = "<root><name>test</name></root>";
Document doc = XmlUtil.parse(xmlStr);

// 2. 读取文件
File file = new File("config.xml");
Document docFromFile = XmlUtil.read(file);

// 3. 读取 URL
Document docFromUrl = XmlUtil.read(new URL("[http://example.com/api.xml](http://example.com/api.xml)"));

// 4. 使用自定义 SAXReader 读取流
SAXReader reader = new SAXReader();
reader.setEncoding("UTF-8");
Document docFromStream = XmlUtil.read(reader, inputStream);
```

### 2. 写入与保存 (Write)

将 `Document` 对象持久化到文件系统。

| 方法名                                   | 描述                              |
|:--------------------------------------|:--------------------------------|
| `toFile(Document, File)`              | 将 Document 写入指定文件（默认格式）。        |
| `toFile(Document, String path)`       | 将 Document 写入指定路径。              |
| `toFile(Document, OutputFormat, ...)` | 使用指定的 **格式化配置**（如缩进、换行、编码）写入文件。 |

**示例代码**：

```java
Document doc = ...;

// 1. 简单保存
XmlUtil.toFile(doc, "/tmp/output.xml");

// 2. 格式化保存 (美化输出)
OutputFormat format = OutputFormat.createPrettyPrint();
format.setEncoding("UTF-8");
XmlUtil.toFile(doc, format, new File("formatted.xml"));
```

### 3. XML 转 JSON / 对象 (Convert)

这是 XmlUtil 的特色功能，能够将 XML 结构转换为 JSON 或 Java 对象。内部算法会自动识别同名子节点并转换为 JSON Array。

| 方法名                                            | 描述                                          |
|:-----------------------------------------------|:--------------------------------------------|
| `toJson(Element, boolean isTrim, Feature...)`  | 将 XML 元素转换为 JSON 字符串。`isTrim` 控制是否去除文本首尾空格。 |
| `parseObject(Element, boolean, Class<T>, ...)` | 将 XML 元素转换为指定的 **Java Bean**。               |
| `parseArray(Element, boolean, Class<T>, ...)`  | 将 XML 元素转换为指定的 **List 集合**。                 |

**转换规则说明**：
* **最子节点**：直接取文本值。
* **单子节点**：转换为 Map (`{"name"："value"}`)。
* **多子节点（同名）**：转换为 List (`[{"name"："v1"}, {"name"："v2"}]`)。
* **多子节点（异名）**：转换为 Map (`{"a"："1", "b"："2"}`)。

**示例代码**：

假设 XML 内容如下：
```xml
<root>
    <user>
        <name>Alice</name>
        <age>18</age>
    </user>
    <user>
        <name>Bob</name>
        <age>20</age>
    </user>
</root>
```

```java
Element root = doc.getRootElement();

// 1. 转 JSON 字符串
// 自动识别两个 <user> 节点，转换为 JSON 数组
String json = XmlUtil.toJson(root, true);
// 结果：[{"name":"Alice","age":"18"}, {"name":"Bob","age":"20"}]

// 2. 转 List 对象
List<User> users = XmlUtil.parseArray(root, true, User.class);

// 3. 转单个对象 (假设 XML 只有一个 user 节点)
User user = XmlUtil.parseObject(singleUserElement, true, User.class);
```

## ⚠️ 异常处理

* **安全策略**：为了保证工具类的易用性，`read` 和 `toFile` 等 IO 操作内部捕获了 `DocumentException` 和 `IOException`。
* **返回值**：如果发生异常，读取方法通常返回 `null`，写入方法会打印错误日志但不抛出异常。
* **参数校验**：传入不支持的参数类型时（如 `read` 的 `arg1` 类型错误），会抛出 `IllegalArgumentException`。
