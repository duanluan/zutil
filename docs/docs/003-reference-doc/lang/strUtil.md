# StrUtil 字符串工具类

> 📦 **包路径**：`top.csaf.lang.StrUtil`
>
> 🔗 **所属模块**：`zutil-core`

**StrUtil** 是在 `org.apache.commons.lang3.StringUtils` 基础上的扩展。
它继承了 Commons Lang3 强大的字符串处理能力，并重点增强了对 `Object` 类型输入的友好支持（无需手动转 String），同时补充了格式化、命名转换等高频实用方法。

## ✨ 核心特性

* **完全兼容**：继承自 Commons Lang3 `StringUtils`，无缝兼容现有代码。
* **对象支持**：`isEmpty`, `isBlank` 等核心校验方法直接支持 `Object` 入参，内部自动处理 `toString()` 和 Null 安全。
* **格式化**：提供类似 SLF4J 的 `{}` 占位符格式化方法。
* **命名转换**：支持驼峰转下划线、首字母大写等常见命名风格转换。

## 🚀 常用方法概览

### 1. 空值校验 (Empty/Blank Check)

增强了 Commons Lang3 的校验能力，直接接受 `Object` 类型，省去了调用 `toString()` 的步骤。

| 方法名                     | 描述                                    |
|:------------------------|:--------------------------------------|
| `isEmpty(Object)`       | 判断对象是否为 `null` 或 `toString()` 后长度为 0。 |
| `isNotEmpty(Object)`    | 判断对象是否**不**为 `null` 且长度大于 0。          |
| `isAnyEmpty(Object...)` | 判断数组中是否**任意一个**对象为空。                  |
| `isBlank(Object)`       | 判断对象是否为 `null`、空字符串或仅包含空白字符。          |
| `isNotBlank(Object)`    | 判断对象是否**不**为 `null`、空字符串且不全为空白字符。     |
| `isAnyBlank(Object...)` | 判断数组中是否**任意一个**对象为空白。                 |

**示例代码**:

```java
StrUtil.isEmpty(null);       // -> true
StrUtil.isEmpty("");         // -> true
StrUtil.isEmpty(new Object()); // -> false (取决于 toString)

StrUtil.isBlank("   ");      // -> true
```

### 2. 字符串操作 (Manipulation)

提供了忽略大小写的前后缀移除功能。

| 方法名                                  | 描述                 |
|:-------------------------------------|:-------------------|
| `removeStartIgnoreCase(str, remove)` | 移除字符串开头的子串（忽略大小写）。 |
| `removeEndIgnoreCase(str, remove)`   | 移除字符串结尾的子串（忽略大小写）。 |
| `nCopies(source, n)`                 | 将字符串重复 `n` 次。      |

**注意**：`remove...` 方法在输入字符串或移除内容为 Blank 时会抛出 `IllegalArgumentException`。

```java
StrUtil.removeStartIgnoreCase("HelloWorld", "hello"); 
// -> "World"

StrUtil.nCopies("Abc", 3);
// -> "AbcAbcAbc"
```

### 3. 字符串格式化 (Formatting)

提供简便的占位符替换功能，类似于 SLF4J 的日志格式化。

| 方法名                    | 描述                    |
|:-----------------------|:----------------------|
| `format(str, vals...)` | 将字符串中的 `{}` 依次替换为参数值。 |

```java
StrUtil.format("Hello, {}!", "World"); 
// -> "Hello, World!"

StrUtil.format("User：{}, Age：{}", "Tom", "18"); 
// -> "User：Tom, Age：18"
```

### 4. 命名与大小写转换 (Case Conversion)

| 方法名                          | 描述                  | 示例                      |
|:-----------------------------|:--------------------|:------------------------|
| `toUnderscore(String)`       | 驼峰转下划线（Snake Case）。 | `UserAge` -> `user_age` |
| `toInitialUpperCase(String)` | 首字母大写。              | `apple` -> `Apple`      |

```java
StrUtil.toUnderscore("HelloWorld"); // -> "hello_world"
```

### 5. 查找 (Search)

| 方法名                       | 描述                                           |
|:--------------------------|:---------------------------------------------|
| `indexOf(seq, searchSeq)` | 查找子串位置。针对 2 字符长度的查找进行了性能优化（代理给 `ArrayUtil`）。 |

## ⚠️ 注意事项

1. **异常处理**：`removeStartIgnoreCase`、`removeEndIgnoreCase` 和 `format` 方法在参数不符合预期（如为 Blank）时会主动抛出 `IllegalArgumentException`，而非返回 `null` 或原字符串。
2. **Object toString**：校验方法依赖对象的 `toString()` 实现，请确保对象实现了有意义的 `toString()`。
