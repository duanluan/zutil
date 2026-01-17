# NumberUtil 数字工具类

> 📦 **包路径**：`top.csaf.lang.NumberUtil`
>
> 🔗 **所属模块**：`zutil-core`

**NumberUtil** 是在 `org.apache.commons.lang3.math.NumberUtils` 基础上的扩展。
它继承了 Commons Lang3 的强大数字处理能力，并重点增强了对 `Object` 类型输入的友好支持，提供了**类型转换**、**格式校验**以及便捷的**数值比对**（如与 0 比较）功能。

## ✨ 核心特性

* **完全兼容**：继承自 Commons Lang3 `NumberUtils`，无缝兼容现有代码。
* **对象支持**：所有 `create` 和 `is` 系列方法均支持 `Object` 入参（内部自动调用 `toString()`），并进行了 Null 安全处理。
* **零值比对**：提供了语义化的方法（如 `gtZero`, `leZero`）用于判断数值与 0 的关系，简化代码逻辑。

## 🚀 常用方法概览

### 1. 数字对象创建 (Creation)

将任意对象（`Object`）转换为指定的数字类型。内部通过 `obj.toString()` 获取字符串后解析。

**特性**:
* 入参为 `null` 时，直接返回 `null`，不会抛出异常。
* 解析失败时会抛出 `NumberFormatException`（继承自父类行为）。

| 方法名 | 描述 | 示例 |
| :--- | :--- | :--- |
| `createBigDecimal(Object)` | 转 `BigDecimal` | `"1.2"` -> `1.2` |
| `createBigInteger(Object)` | 转 `BigInteger` | `"10"` -> `10` |
| `createInteger(Object)` | 转 `Integer` | `"123"` -> `123` |
| `createLong(Object)` | 转 `Long` | `"123"` -> `123L` |
| `createDouble(Object)` | 转 `Double` | `"1.23"` -> `1.23d` |
| `createFloat(Object)` | 转 `Float` | `"1.23"` -> `1.23f` |
| `createNumber(Object)` | 智能转 `Number` (自动识别类型) | `"1"` -> `Integer`, `"1.0"` -> `Double` |

```java
Object val = "100.5";
BigDecimal num = NumberUtil.createBigDecimal(val); 
// -> 100.5
```

### 2. 格式校验 (Validation)

判断传入的对象是否可以被解析为指定的数字类型。

**特性**:
* 基于 `try-catch` 机制实现。如果转换成功返回 `true`，失败（抛出异常）或入参为 `null` 则返回 `false`。

| 方法名 | 描述 |
| :--- | :--- |
| `isNumber(Object)` | 是否为有效数字（支持 Hex、Octal 等格式） |
| `isInteger(Object)` | 是否可转为 `Integer` |
| `isLong(Object)` | 是否可转为 `Long` |
| `isDouble(Object)` | 是否可转为 `Double` |
| `isBigDecimal(Object)` | 是否可转为 `BigDecimal` |

```java
NumberUtil.isInteger("123");   // -> true
NumberUtil.isInteger("12.3");  // -> false (抛出异常被捕获)
NumberUtil.isNumber(null);     // -> false
```

### 3. 零值比对 (Zero Comparison)

提供简便的方法来判断数字与 `0` 的大小关系。内部先将对象转换为 `Number`，再通过 `doubleValue()` 进行比较。

**特性**:
* 入参为 `null` 时，统一返回 `false`。

| 方法名 | 简写含义 | 描述 | 数学符号 |
| :--- | :--- | :--- | :--- |
| `gtZero(Object)` | **G**reater **T**han | 大于 0 | `> 0` |
| `geZero(Object)` | **G**reater or **E**qual | 大于等于 0 | `>= 0` |
| `ltZero(Object)` | **L**ess **T**han | 小于 0 | `< 0` |
| `leZero(Object)` | **L**ess or **E**qual | 小于等于 0 | `<= 0` |
| `eqZero(Object)` | **EQ**ual | 等于 0 | `== 0` |

```java
NumberUtil.gtZero(10);    // -> true
NumberUtil.leZero(-5.5);  // -> true
NumberUtil.eqZero("0");   // -> true
NumberUtil.gtZero(null);  // -> false
```

## ⚠️ 注意事项

1.  **精度说明**：零值比对方法（`gtZero` 等）底层使用 `number.doubleValue()` 进行比较。对于超出 `Double` 精度范围的极大/极小 `BigDecimal`，可能存在精度丢失风险，但在判断“正负性”场景下通常是安全的。
2.  **异常处理**：`create` 系列方法在格式非法时会抛出异常，而 `is` 系列方法会吞掉异常并返回 `false`。请根据业务需求选择使用。
