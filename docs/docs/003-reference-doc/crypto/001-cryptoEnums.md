# Crypto 枚举常量

> 📦 **包路径**: `top.csaf.crypto.enums`

这里列出了加密模块中使用的所有枚举常量，包括算法类型、工作模式、填充方式以及编码格式。

## BlockCipherType - 分组密码算法

`BlockCipherType` 定义了支持的对称加密算法类型。

| 枚举名 | 算法名称 | 描述 |
| :--- | :--- | :--- |
| `DES` | DES | 数据加密标准 (Data Encryption Standard) |
| `AES` | AES | 高级加密标准 (Advanced Encryption Standard) |
| `SM4` | SM4 | 国密 SM4 分组密码算法 ([国家标准](https://std.samr.gov.cn/gb/search/gbDetailed?id=71F772D81199D3A7E05397BE0A0AB82A)) |

## Mode - 工作模式

`Mode` 定义了分组密码的工作模式 (Mode of Operation)。
来源于 `org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher`。

| 枚举名 | 模式代码 | 全称 / 描述 |
| :--- | :--- | :--- |
| `ECB` | `ECB` | 电子密码本模式 (Electronic Codebook) |
| `CBC` | `CBC` | 密码块链接模式 (Cipher Block Chaining) |
| `CTR` | `CTR` | 计数器模式 (Counter) |
| `OFB` | `OFB` | 输出反馈模式 (Output Feedback) |
| `CFB` | `CFB` | 密文反馈模式 (Cipher FeedBack) |
| `CTS` | `CTS` | 密文窃取模式 (Cipher Text Stealing) |
| `GCM` | `GCM` | 伽罗瓦/计数器模式 (Galois/Counter Mode) |
| `CCM` | `CCM` | Counter with CBC-MAC |
| `EAX` | `EAX` | EAX Mode |
| `OCB` | `OCB` | OCB Mode |
| `SIC` | `SIC` | Segmented Integer Counter (通常同 CTR) |
| `PGPCFB` | `PGPCFB` | OpenPGP 密码反馈模式 |
| `PGPCFBWITHIV` | `PGPCFBWITHIV` | 带 IV 的 OpenPGP 密码反馈模式 |
| `OPENPGPCFB` | `OPENPGPCFB` | OpenPGP CFB |
| `FF1` | `FF1` | Format-Preserving Encryption FF1 |
| `FF3_1` | `FF3-1` | Format-Preserving Encryption FF3-1 |
| `GOFB` | `GOFB` | GOST 28147 OFB |
| `GCFB` | `GCFB` | GOST 28147 CFB |

## Padding - 填充方式

`Padding` 定义了块加密中使用的填充方案。
来源于 `org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher`。

| 枚举名 | 填充代码 | 描述 |
| :--- | :--- | :--- |
| `NO` | `NoPadding` | 不填充 |
| `ZERO` | `ZEROBYTEPADDING` | 零字节填充 |
| `PKCS5` | `PKCS5PADDING` | PKCS #5 填充 (常用于 AES/DES) |
| `PKCS7` | `PKCS7PADDING` | PKCS #7 填充 |
| `ISO_10126` | `ISO10126PADDING` | ISO 10126 填充 |
| `ISO_10126_2` | `ISO10126-2PADDING` | ISO 10126-2 填充 |
| `ANSI_X923` | `X923PADDING` | ANSI X.923 填充 |
| `ISO_7816_4` | `ISO7816-4PADDING` | ISO/IEC 7816-4 填充 |
| `ISO_9797_1` | `ISO9797-1PADDING` | ISO/IEC 9797-1 填充 |
| `TBCPADDING` | `TBCPADDING` | Trailing Bit Complement Padding |

## EncodingType - 编码类型

`EncodingType` 用于指定密文、密钥 (Key) 或 偏移量 (IV) 的字符串编码格式。

| 枚举名 | 描述 |
| :--- | :--- |
| `UTF_8` | UTF-8 编码 (通常用于明文) |
| `HEX` | 十六进制编码 (Hexadecimal) |
| `BASE_64` | Base64 编码 |
