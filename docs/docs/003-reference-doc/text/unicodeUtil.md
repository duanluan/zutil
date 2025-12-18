# UnicodeUtil 编码工具类

> 📦 **包路径**: `top.csaf.text.UnicodeUtil`

**Unicode 工具类**, 主要用于处理 String 字符串与 Unicode 编码 (以及 16 进制字符串) 之间的相互转换. 🛠️

## ✨ 特性说明

* **双向转换**: 支持字符串到 Unicode/Hex 的编码, 以及反向解码.
* **混合解析**: `toString` 方法支持包含普通文本和 Unicode 编码的混合字符串解析.
* **空值安全**: 所有方法均对空字符串做了处理, 避免 NPE (依赖 Lombok `@NonNull`).

## 🚀 方法概览

| 方法类型 | 方法名 | 描述 | 输入示例 | 输出示例 |
| :--- | :--- | :--- | :--- | :--- |
| **编码** | `toUnicode` | 转 Unicode (带 `\u`) | `"你好"` | `"\u4f60\u597d"` |
| **编码** | `toHex` | 转 16 进制 (无前缀) | `"你好"` | `"4f60597d"` |
| **解码** | `toString` | Unicode 转字符串 | `"\u4f60\u597d"` | `"你好"` |
| **解码** | `fromHex` | 16 进制转字符串 | `"4f60597d"` | `"你好"` |

---

## 📝 详细用法

### 1. 字符串转 Unicode (`toUnicode`)

将普通字符串转换为标准的 Unicode 编码格式 (即 `\u` 开头). 🔡 ➡️ 🔢

```java
// 示例
String result = UnicodeUtil.toUnicode("你好");
System.out.println(result); 
// 输出: \u4f60\u597d
```

### 2. 字符串转 16 进制 (`toHex`)

将字符串转换为 Unicode 对应的 16 进制字符串, **不带** `\u` 前缀. 适合需要纯 Hex 数据传输的场景. 🔢

```java
// 示例
String result = UnicodeUtil.toHex("你好");
System.out.println(result);
// 输出: 4f60597d
```

### 3. Unicode 转字符串 (`toString`)

将 Unicode 编码还原为原始字符串. 🔄

💡 **亮点**: 该方法支持 **混合内容** 解析. 如果字符串中包含非 Unicode 内容, 会按原样保留.

```java
// 场景 1: 纯 Unicode
UnicodeUtil.toString("\u4f60\u597d"); 
// -> "你好"

// 场景 2: 混合内容 (例如日志或混合文本)
UnicodeUtil.toString("Hello\u4f60\u597d"); 
// -> "Hello你好"

// 场景 3: 格式错误的 Unicode (会被原样输出)
UnicodeUtil.toString("\\uGGGG"); 
// -> "\\uGGGG"
```

### 4. 16 进制转字符串 (`fromHex`)

将纯 16 进制字符串还原为原始字符串. 🔙

⚠️ **注意**:
* 输入必须是纯 16 进制字符串.
* 字符串长度必须是 **4 的倍数**, 否则会抛出 `IllegalArgumentException`.

```java
// 示例
try {
  String result = UnicodeUtil.fromHex("4f60597d");
  System.out.println(result);
  // 输出: 你好
} catch (IllegalArgumentException e) {
  // 处理长度非 4 倍数或非法字符的情况
  e.printStackTrace();
}
```

---

## ⚙️ 异常处理

* **`UnsupportedOperationException`**: 尝试实例化 `UnicodeUtil` 类时抛出 (工具类禁止实例化). 🚫
* **`IllegalArgumentException`**: `fromHex` 方法输入长度不是 4 的倍数时抛出. ❌
* **`NullPointerException`**: 入参为 `null` 时抛出 (由 Lombok `@NonNull` 触发). 🔍
