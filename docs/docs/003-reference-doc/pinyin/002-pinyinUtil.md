# PinyinUtil 拼音工具类

> 📦 **包路径**: `top.csaf.pinyin.PinyinUtil`

**拼音工具类**，用于将汉字转换为拼音。
数据来源：[mozillazg/pinyin-data](https://github.com/mozillazg/pinyin-data)。

## ✨ 核心功能

* **多模式转换**: 支持获取全部拼音（多音字）、首个拼音。
* **声调控制**: 支持保留声调或移除声调。
* **格式化**: 支持自定义分隔符，配合 `PinyinFeat` 可控制首字母大写、非汉字间隔等。
* **多音字检测**: 简单的多音字判断。

## 🚀 方法概览

### 1. 核心方法 (`get`)

所有快捷方法最终都调用此核心方法。

```java
/**
 * @param str             原始内容
 * @param isWithTone      是否带声调
 * @param isOnlyFirst     是否只取多音字的第一个拼音 (true: 单个; false: 所有读音用逗号隔开)
 * @param pinyinSeparator 拼音分隔符 (如空格)
 */
public static String get(String str, boolean isWithTone, boolean isOnlyFirst, String pinyinSeparator)
```

**示例**:

```java
// 基础用法
PinyinUtil.get("重", true, false, ","); 
// -> "zhòng,chóng" (多音字)

// 常用组合
PinyinUtil.get("你好", true, true, " "); 
// -> "nǐ hǎo"
```

### 2. 快捷方法 (Shortcuts) 🛠️

为了方便使用，提供了多种常见场景的快捷方法。

#### 获取拼音 (带/不带声调, 单/多音)

| 方法名 | 描述 | 示例输入 ("重") | 输出示例 |
| :--- | :--- | :--- | :--- |
| `getAll` | 获取**所有**读音 | `getAll("重", true)` | `"zhòng,chóng"` |
| `getFirst` | 仅获取**第一个**读音 | `getFirst("重", true)` | `"zhòng"` |
| `getWithTone` | **带声调** | `getWithTone("好", true)` | `"hǎo"` |
| `getNotWithTone` | **不带声调** | `getNotWithTone("好", true)` | `"hao"` |

#### 组合快捷方法

| 方法名 | 含义 |
| :--- | :--- |
| `getAllWithTone` | 获取**所有**读音 + **带声调** |
| `getAllNotWithTone` | 获取**所有**读音 + **不带声调** |
| `getFirstWithTone` | 获取**首个**读音 + **带声调** |
| `getFirstNotWithTone` | 获取**首个**读音 + **不带声调** |

**示例代码**:

```java
// 获取首个拼音，不带声调，无分隔符
PinyinUtil.getFirstNotWithTone("你好"); 
// -> "nihao"

// 获取首个拼音，带声调，用空格分隔
PinyinUtil.getFirstWithTone("你好世界", " "); 
// -> "nǐ hǎo shì jiè"
```

### 3. 多音字判断 (`isPolyphonicWord`)

判断单个字符是否为多音字。

```java
boolean b1 = PinyinUtil.isPolyphonicWord('好'); // -> true (hǎo, hào)
boolean b2 = PinyinUtil.isPolyphonicWord('你'); // -> false
```

## ⚙️ 配合 PinyinFeat 使用

`PinyinUtil` 会读取 `PinyinFeat` 中的配置来改变输出格式。

**场景：首字母大写 (Name Case)**

```java
// 设置首个单词首字母大写
PinyinFeat.setFirstWordInitialCap(true);
// 设置第二个单词开始首字母大写
PinyinFeat.setSecondWordInitialCap(true);

PinyinUtil.getFirstNotWithTone("诸葛亮", " ");
// -> "Zhu Ge Liang"
```

**场景：处理非汉字间隔**

```java
// 默认情况下，非汉字（如逗号）周围不会添加分隔符
PinyinUtil.getFirstWithTone("你好，世界", " ");
// -> "nǐ hǎo，shì jiè"

// 开启特性：非拼音前后添加分隔符
PinyinFeat.setHasSeparatorByNotPinyinAround(true);
PinyinUtil.getFirstWithTone("你好，世界", " ");
// -> "nǐ hǎo ， shì jiè"
```
