# 介绍

## 🚀 简介

**ZUtil** 是一个追求**更快、更全**的 Java 工具类库。

它不仅仅是简单的工具封装，更注重开发体验与性能优化。ZUtil 采用了模块化设计，你可以引入 `zutil-all` 使用所有功能，也可以按需引入独立模块（如 `zutil-io`, `zutil-date` 等）。

* 📄 **API 文档**: [Javadoc](https://apidoc.gitee.com/duanluan/zutil)
* 📊 **性能对比**: [JMH Comparison](https://github.com/duanluan/zutil/tree/main/zutil-all/src/test/java/top/csaf/jmh/comparison) (vs Hutool)

---

## 🛠️ 模块概览

### 1. 独立模块 (Standalone Modules)

以下模块可以单独引入，不依赖其他庞大的组件，保持项目轻量。

| 模块 (ArtifactId) | 功能 | 核心类 | 描述 |
| :--- | :--- | :--- | :--- |
| **`zutil-core`** | **基础核心** | `StrUtil`, `CollUtil`, `MapUtil`, `ArrayUtil`, `ObjUtil`, `NumberUtil` | 最基础的字符串、集合、数组、对象、随机数操作 |
| **`zutil-date`** | **日期时间** | `DateUtil`, `DateFeat` | 基于 Java 8 Time，提供智能解析、区间计算、特性配置 |
| **`zutil-io`** | **文件 IO** | `FileUtil`, `IOUtil` | 文件读写、流拷贝、资源加载、文件类型判断 |
| **`zutil-http`** | **HTTP** | `HttpUtil` | 轻量级 HTTP 请求工具 (Get/Post) |
| **`zutil-json`** | **JSON** | `JsonUtil` | JSON 序列化与反序列化封装 (支持 Gson/Jackson) |
| **`zutil-regex`** | **正则** | `RegExUtil` | 正则匹配、提取、替换，内置常用 Pattern |
| **`zutil-pinyin`** | **拼音** | `PinyinUtil` | 汉字转拼音，支持多音字、声调、自定义格式 |
| **`zutil-img`** | **图片** | `ThumbnailUtil`, `ConvertUtil` | 图片缩放、裁剪、格式转换 |
| **`zutil-sport`** | **运动** | `SportFileUtil` | 运动文件处理 (如 GPX 转 FIT 格式) |
| **`zutil-awt`** | **桌面** | `ClipboardUtil` | 系统剪贴板读写操作 |

### 2. 综合模块 (zutil-all)

以下功能目前**集成在 `zutil-all` 中**。如需使用，请引入 `zutil-all` 依赖。

| 功能分类 | 包路径 (Package) | 核心类 | 描述 |
| :--- | :--- | :--- | :--- |
| **加解密** | `top.csaf.crypto` | `AesUtil`, `Sm4Util`, `Md5Util` | 对称加密 (AES, DES, SM4) 及消息摘要 |
| **Bean** | `top.csaf.bean` | `BeanUtil` | 对象属性拷贝、Bean 转 Map |
| **文本** | `top.csaf.text` | `UnicodeUtil` | Unicode 编码转换 |
| **ID 生成** | `top.csaf.id` | `SnowFlake`, `NanoIdUtil` | 雪花算法、NanoID 生成器 |
| **树结构** | `top.csaf.tree` | `TreeUtil` | 树形结构构建工具 |
| **XML** | `top.csaf.xml` | `XmlUtil` | XML 解析与生成 |
| **YAML** | `top.csaf.yaml` | `YamlUtil` | YAML 配置解析 |
| **线程** | `top.csaf.thread` | `ThreadLocalUtil` | ThreadLocal 便捷管理 |
| **证件** | `top.csaf.idcard` | `IdCardUtil` | 身份证解析校验 |

> 💡 **提示**: 引入 `zutil-all` 会自动包含上述所有独立模块 (`core`, `date`, `io` 等)。

---

## 🔗 资源与社区

如果你在使用过程中遇到问题，或有好的建议，欢迎通过以下渠道联系：

* **GitHub Discussions**: [提问与讨论](https://github.com/duanluan/zutil/discussions)
* **Gitee**: [项目仓库](https://gitee.com/duanluan/zutil) (欢迎 Star ⭐ 或评论)
* **QQ 群**: [点击加入](https://jq.qq.com/?_wv=1027&k=Jzpzg0lc)
* **技术问答**:
    * **SegmentFault**: [ZUtil 问答](https://segmentfault.com/search?q=zutil&type=qa) ([关注作者](https://segmentfault.com/u/duanluan))
    * **开源中国**: [ZUtil 问答](https://www.oschina.net/search?scope=bbs&q=zutil) ([向我提问](https://www.oschina.net/question/ask?user=2353983))
    * **CSDN**: [ZUtil 问答](https://so.csdn.net/so/search?q=zutil&t=ask) ([@作者邀请回答](https://blog.csdn.net/duanluan))
