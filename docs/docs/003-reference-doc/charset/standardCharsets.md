# StandardCharsets 常用字符集

> 📦 **包路径**：`top.csaf.charset.StandardCharsets`
>
> 🔗 **所属模块**：`zutil-core`

**StandardCharsets** 提供了一组标准的 `Charset` 常量。
它在 JDK `java.nio.charset.StandardCharsets` 的基础上进行了扩展，补充了国内常用的 **GB2312** 字符集，方便在代码中直接引用，避免硬编码字符串或重复调用 `Charset.forName()`。

## ✨ 核心特性

* **标准兼容**：包含了 JDK 标准库中的所有常用字符集（UTF-8, ISO-8859-1, US-ASCII 等），直接引用自 `java.nio.charset.StandardCharsets`。
* **扩展支持**：额外定义了 **GB2312** 常量，填补了 JDK 标准常量的空白。
* **防实例化**：工具类设计，构造方法私有并抛出 `AssertionError`，防止误实例化。

## 🚀 常量概览

| 常量名          | 描述                         | 对应 JDK 常量 / 定义                |
|:-------------|:---------------------------|:------------------------------|
| `UTF_8`      | UTF-8 编码                   | `StandardCharsets.UTF_8`      |
| `US_ASCII`   | 7位 ASCII 字符集               | `StandardCharsets.US_ASCII`   |
| `ISO_8859_1` | ISO Latin Alphabet No. 1   | `StandardCharsets.ISO_8859_1` |
| `UTF_16`     | UTF-16 编码 (带 BOM)          | `StandardCharsets.UTF_16`     |
| `UTF_16BE`   | UTF-16 Big Endian (大端序)    | `StandardCharsets.UTF_16BE`   |
| `UTF_16LE`   | UTF-16 Little Endian (小端序) | `StandardCharsets.UTF_16LE`   |
| `GB2312`     | GB2312 简体中文字符集             | `Charset.forName("GB2312")`   |

## 💡 使用示例

```java
import top.csaf.charset.StandardCharsets;

// 1. 字符串转字节数组 (使用 UTF-8)
byte[] bytes = "你好".getBytes(StandardCharsets.UTF_8);

// 2. 使用 GB2312 (无需处理异常，无需手动输入字符串)
byte[] gbBytes = "你好".getBytes(StandardCharsets.GB2312);

// 3. 配合 ArrayUtil 使用
// ArrayUtil.toBytes(charArray, StandardCharsets.GB2312);
