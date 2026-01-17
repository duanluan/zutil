# ObjUtil 对象工具类

> 📦 **包路径**：`top.csaf.lang.ObjUtil`
>
> 🔗 **所属模块**：`zutil-core`

**ObjUtil** 是在 `org.apache.commons.lang3.ObjectUtils` 基础上的扩展。
主要扩展了**多对象相等性判断**的功能，支持“按数值内容比较”（忽略数据类型差异，如 `1` 与 `1.0`）以及“忽略 Null 值”的特殊比较策略。

## ✨ 核心特性

* **完全兼容**：继承自 Commons Lang3，无缝兼容现有代码。
* **数值内容比较**：支持跨类型的数值相等判断。例如，开启该模式后，整数 `1`、浮点数 `1.0`、字符串 `"1.00"` 均被视为相等。
* **Null 值策略**：支持在批量比较时跳过 `null` 元素。

## 🚀 常用方法概览

### 1. 批量相等判断 (All Equals)

判断传入的一组对象是否**全部相等**。

| 方法名 | 描述 |
| :--- | :--- |
| `isAllEquals(boolean isByValue, boolean isContinueNull, Object... objects)` | 判断数组内的元素是否全部相等。支持配置是否按数值比较、是否跳过 Null。 |

#### 参数说明

* **`isByValue` (boolean)**：是否根据**值**来判断相等。
  * **`true`**：将对象转换为字符串进行比较。特别地，对于浮点数（Float, Double）和 BigDecimal，会去除小数点后无效的 0（如 `1.00` -> `1`）后再比较。
  * **`false`**：使用默认的 `equals` 方法进行比较。
* **`isContinueNull` (boolean)**：对象为 `null` 时是否跳过判断。
  * **`true`**：如果**当前遍历到的元素**为 `null`，则直接跳过该次比较，继续检查下一个。
  * **`false`**：`null` 也会参与比较（即要求其他元素也必须为 `null` 才能相等）。
* **`objects` (Object...)**：需要判断的对象数组，长度必须大于 1。

#### 示例代码

**场景 1：基础比较 (Strict)**
```java
// 默认行为：使用 equals 比较
ObjUtil.isAllEquals(false, false, "a", "a", "a"); // -> true
ObjUtil.isAllEquals(false, false, "a", "b");      // -> false
```

**场景 2：数值内容比较 (By Value)**
忽略类型差异和小数点后多余的 0。
```java
// 1 (Integer) vs 1.0 (Double)
// isByValue = true：转换为 "1" 进行比较 -> true
ObjUtil.isAllEquals(true, false, 1, 1.0); 

// "1.00" (String) vs 1 (Integer)
// isByValue = true："1.00" 被处理为 "1" -> true
ObjUtil.isAllEquals(true, false, "1.00", 1);

// isByValue = false：类型不同 -> false
ObjUtil.isAllEquals(false, false, 1, 1.0);
```

**场景 3：忽略 Null 值 (Skip Null)**
```java
// 跳过中间的 null
// 比较逻辑：
// i=1 ("b")："b" vs "b" -> eq
// i=2 (null)：skip
// -> true
ObjUtil.isAllEquals(false, true, "b", "b", null); 
```

## ⚠️ 注意事项

1. **参数长度限制**：`objects` 数组长度必须 **大于 1**，否则抛出 `IllegalArgumentException`。
2. **Null 跳过机制**：`isContinueNull` 仅跳过**当前元素为 Null** 的情况。比较是逐个与前一个元素进行的（`objects[i]` vs `objects[i-1]`）。
    * 注意：如果数组是 `["a", null, "a"]` 且 `isContinueNull=true`：
      * i=1 (`null`)：跳过。
      * i=2 (`"a"`)：比较 `"a"` 和 `objects[1]` (`null`) -> **不相等**，返回 `false`。
    * 因此，该功能主要用于“允许尾部出现 Null”或“忽略明确的 Null 占位符”场景，而非自动连接断开的链条。
3. **数值转换逻辑**：当 `isByValue=true` 时，只有 **8种基本类型包装类** (参见 `ClassUtil.isPrimitiveType`) 以及 `BigDecimal`, `BigInteger` 会触发特殊数值格式化（去除尾部 0），其他类型仅调用 `toString()`。
