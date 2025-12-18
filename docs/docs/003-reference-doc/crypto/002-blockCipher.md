# BlockCipher 分组加密

> 📦 **包路径**: `top.csaf.crypto`
>
> 🔒 **底层实现**: 基于 Bouncy Castle 的 `BlockCipher` 封装

本模块提供对称加密（分组密码）的工具类支持，涵盖了国际标准算法 **AES**、**DES** 以及国密算法 **SM4**。

所有工具类均支持自定义 **工作模式 (Mode)**、**填充方式 (Padding)** 以及 **密钥/IV 编码格式**。

## 🚀 工具类概览

| 工具类名 | 算法 | 描述 |
| :--- | :--- | :--- |
| `AesUtil` | **AES** | 高级加密标准，目前最常用的对称加密算法 |
| `Sm4Util` | **SM4** | 国密分组密码算法，国内金融/政务领域标准 |
| `DesUtil` | **DES** | 数据加密标准，安全性较低，通常用于兼容旧系统 |
| `BlockCipherUtil` | **通用** | 支持动态传入 `BlockCipherType` 指定算法 |

## 📝 常用方法 (以 AesUtil 为例)

`AesUtil`、`Sm4Util`、`DesUtil` 的方法签名完全一致。以下以 `AesUtil` 为例进行说明。

### 1. 加密 (Encrypt)

支持输出为 **Base64** 或 **Hex** 字符串。

```java
// 准备参数
String content = "Hello World";
String key = "1234567812345678"; // AES-128 需 16 位
String iv = "1234567812345678";  // CBC 模式通常需 IV

// 方式 1: 加密为 Base64
String b64 = AesUtil.encryptBase64(content, key, iv, Mode.CBC, Padding.PKCS7);
// -> "SGVsbG8..."

// 方式 2: 加密为 Hex
String hex = AesUtil.encryptHex(content, key, iv, Mode.CBC, Padding.PKCS7);
// -> "48656c6c6f..."
```

> 💡 **提示**: 使用 `Sm4Util` 或 `DesUtil` 时，只需将类名替换即可，参数用法完全相同。

### 2. 解密 (Decrypt)

```java
// 解密 Base64
String plain1 = AesUtil.decryptBase64(b64, key, iv, Mode.CBC, Padding.PKCS7);

// 解密 Hex
String plain2 = AesUtil.decryptHex(hex, key, iv, Mode.CBC, Padding.PKCS7);
```

### 3. 高级用法：自定义 Key/IV 编码

默认情况下，`key` 和 `iv` 被视为普通字符串（UTF-8）。如果你的密钥本身是 **Hex** 或 **Base64** 格式，需指定 `EncodingType`。

```java
// 假设密钥是 Hex 格式字符串
String hexKey = "31323334353637383132333435363738"; // "1234567812345678" 的 Hex

String result = AesUtil.encryptBase64(
    "Hello", 
    hexKey, EncodingType.HEX, // 👈 指定 Key 为 Hex
    iv, EncodingType.UTF_8,   // IV 仍为普通字符串
    Mode.CBC, 
    Padding.PKCS7
);
```

---

## 🛠️ 通用工具 BlockCipherUtil

如果你需要在运行时动态决定加密算法，可以使用 `BlockCipherUtil`。它比上述工具类多一个 `BlockCipherType` 参数。

```java
// 动态指定为 SM4 算法
String result = BlockCipherUtil.encryptBase64(
    BlockCipherType.SM4, // 👈 指定算法
    "Hello", 
    key, 
    iv, 
    Mode.CBC, 
    Padding.PKCS7
);
```

---

## ⚙️ 算法参数参考

不同算法对 **密钥 (Key)** 和 **偏移量 (IV)** 的长度有严格要求（单位：字节/Byte）：

| 算法 | 密钥长度 (Byte) | IV 长度 (Byte) | 说明 |
| :--- | :--- | :--- | :--- |
| **AES** | 16, 24, 32 | 16 | 对应 AES-128, AES-192, AES-256 |
| **SM4** | 16 | 16 | 固定 128 位 |
| **DES** | 8 | 8 | 实际有效位 56 位 |

> **注意**: 如果 Key/IV 长度不符合要求，会抛出异常。
