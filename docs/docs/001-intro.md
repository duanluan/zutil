# 介绍

## 🚀 简介

**ZUtil** 是一个追求**更快、更全**的 Java 工具类库。

它不仅仅是简单的工具封装，更注重开发体验与性能优化。本文档主要介绍核心常用及特色功能，更详细的 API 说明请查阅 Javadoc。

* 📄 **API 文档**: [Javadoc](https://apidoc.gitee.com/duanluan/zutil)
* 📊 **性能对比**: [JMH Comparison](https://github.com/duanluan/zutil/tree/main/zutil-all/src/test/java/top/csaf/jmh/comparison) (vs Hutool)

---

## 🌟 核心模块

ZUtil 提供了丰富的工具模块，涵盖了日常开发的方方面面：

| 模块 | 描述 | 核心类 |
| :--- | :--- | :--- |
| **Date** | 强大的时间处理，支持 Java 8 Time，提供智能解析、区间计算、特性配置等 | `DateUtil`, `DateFeat` |
| **Pinyin** | 汉字转拼音工具，支持多音字、声调、首字母大写等自定义格式 | `PinyinUtil`, `PinyinFeat` |
| **RegEx** | 正则表达式增强，简化匹配、提取、替换操作 | `RegExUtil` |
| **Text** | 文本处理，如 Unicode/Hex 编码转换等 | `UnicodeUtil` |
| **Base** | 基础工具 (Lang, Coll, Bean, IO 等) | `StrUtil`, `CollUtil`, `BeanUtil`... |

> 💡 **提示**: 许多工具类都支持 `Feat` (Feature) 特性配置，可灵活调整全局或线程级的行为（如解析模式、时区等）。

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
