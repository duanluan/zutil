# PinyinFeat 拼音特性

> 📦 **包路径**: `top.csaf.pinyin.PinyinFeat`

可以通过 **临时 (ThreadLocal)** 或 **全局 (Always)** 修改静态成员变量，来决定 `PinyinUtil` 的处理方式。

为了方便配置，推荐使用 **`PinyinFeatConfig`** 进行链式调用。

## ⚙️ 配置方式

### 链式配置 (推荐)

使用 `PinyinFeatConfig` 可以优雅地设置特性。

```java
// 1. 临时生效 (ThreadLocal) - 仅当前线程，使用一次后自动失效（在 PinyinUtil 读取后清除）
PinyinFeatConfig.setFirstWordInitialCap(true)
    .setHasSeparatorByNotPinyinAround(true)
    .apply();

// 2. 全局生效 (Always) - 对所有线程永久生效，优先级高于临时设置
PinyinFeatConfig.setFirstWordInitialCapAlways(true)
    .apply();
```

### 静态方法配置

也可以直接调用 `PinyinFeat` 的静态方法。

```java
// 临时设置
PinyinFeat.setFirstWordInitialCap(true);

// 全局设置
PinyinFeat.setFirstWordInitialCapAlways(true);
```

---

## ✨ 特性详解

### 1. FIRST_WORD_INITIAL_CAP

**第一个单词首字母是否大写**。

* **默认值**: `false`
* **描述**: 控制转换结果中，第一个汉字拼音的首字母是否大写。

```java
// 默认: false
PinyinUtil.get("你好", false, true, " "); // -> "ni hao"

// 设置为 true
PinyinFeat.setFirstWordInitialCap(true);
PinyinUtil.get("你好", false, true, " "); // -> "Ni hao"
```

### 2. SECOND_WORD_INITIAL_CAP

**第二个单词首字母是否大写**。

* **默认值**: `false`
* **描述**: 控制转换结果中，除第一个汉字外，后续汉字拼音的首字母是否大写。常用于生成驼峰式拼音。

```java
// 默认: false
PinyinUtil.get("你好", false, true, " "); // -> "ni hao"

// 设置为 true
PinyinFeat.setSecondWordInitialCap(true);
PinyinUtil.get("你好", false, true, " "); // -> "ni Hao"

// 配合 FIRST_WORD_INITIAL_CAP = true，可实现 PascalCase (Ni Hao)
```

### 3. HAS_SEPARATOR_BY_NOT_PINYIN_AROUND

**非拼音前后是否需要分隔符**。

* **默认值**: `false`
* **描述**: 当指定了 `pinyinSeparator` (拼音分隔符) 时，汉字与非汉字（如标点、数字、英文）之间是否需要添加该分隔符。

```java
String str = "好好学习，天天向上";

// 默认: false (逗号两边没有空格)
PinyinUtil.get(str, true, true, " "); 
// -> "hǎo hǎo xué xí，tiān tiān xiàng shàng"

// 设置为 true (逗号两边添加空格)
PinyinFeat.setHasSeparatorByNotPinyinAround(true);
PinyinUtil.get(str, true, true, " "); 
// -> "hǎo hǎo xué xí ， tiān tiān xiàng shàng"
```
