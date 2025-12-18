# Md5Util MD5 摘要工具类

> 📦 **包路径**: `top.csaf.crypto.Md5Util`
>
> 🔒 **底层实现**: 基于 `Bouncy Castle` 的 `MessageDigest`

**Md5Util** 是一个便捷的 MD5 消息摘要工具类。它用于生成数据的数字指纹，通常用于校验数据完整性或简单的密码存储（虽然生产环境建议使用更安全的算法如 BCrypt）。

该工具类支持输出 **32 位**（标准）或 **16 位**（截取）的十六进制字符串，并可灵活选择 **大写** 或 **小写** 格式。

## ✨ 核心特性

* **Bouncy Castle 支持**: 底层注册并使用了 Bouncy Castle Provider，确保算法实现的稳定性。
* **灵活输出**: 支持 32 位标准长度和 16 位截取长度（即截取 32 位的中间 16 位）。
* **大小写转换**: 提供直接输出大写或小写 Hex 字符串的方法。
* **多类型支持**: 支持 `String` 和 `byte[]` 类型的输入。

## 🚀 方法概览

| 方法名 | 描述 | 输出示例 (输入 "123456") |
| :--- | :--- | :--- |
| `toLowerCase` | 生成 **32 位** 小写 MD5 | `"e10adc3949ba59abbe56e057f20f883e"` |
| `toUpperCase` | 生成 **32 位** 大写 MD5 | `"E10ADC3949BA59ABBE56E057F20F883E"` |
| `toLowerCaseShort` | 生成 **16 位** 小写 MD5 | `"49ba59abbe56e057"` |
| `toUpperCaseShort` | 生成 **16 位** 大写 MD5 | `"49BA59ABBE56E057"` |

## 📝 详细用法

### 1. 标准 32 位 MD5

最常用的 MD5 生成方式，结果为 32 个字符的十六进制字符串。

```java
String data = "123456";

// 1. 生成小写 (推荐)
// 结果: e10adc3949ba59abbe56e057f20f883e
String lower = Md5Util.toLowerCase(data);

// 2. 生成大写
// 结果: E10ADC3949BA59ABBE56E057F20F883E
String upper = Md5Util.toUpperCase(data);
```

### 2. 16 位 MD5 (Short)

有些旧系统或特定场景只需要 16 位的 MD5。
这实际上是截取了 32 位结果的中间部分（第 8 到 24 位）。

```java
String data = "123456";

// 1. 生成 16 位小写
// 结果: 49ba59abbe56e057
String shortLower = Md5Util.toLowerCaseShort(data);

// 2. 生成 16 位大写
// 结果: 49BA59ABBE56E057
String shortUpper = Md5Util.toUpperCaseShort(data);
```

### 3. 处理字节数组

除了字符串，所有方法都重载支持 `byte[]` 输入，适用于文件流或非文本数据的摘要计算。

```java
byte[] fileData = "FileContent".getBytes();

// 计算字节数组的 MD5
String hex = Md5Util.toLowerCase(fileData);
```
