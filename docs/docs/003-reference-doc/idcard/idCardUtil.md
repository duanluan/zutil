# IdCardUtil 身份证工具类

> 📦 **包路径**：`top.csaf.idcard.IdCardUtil`
>
> 🔗 **所属模块**：`zutil-all`

**IdCardUtil** 是针对中国居民身份证（15 位/18 位）的专用处理工具。
它提供了从**格式校验**到**信息提取**（生日、年龄、性别、省市区编码）的全套解决方案，支持将身份证信息直接解析为 Java 对象，并兼容了旧版 15 位身份证的解析逻辑。

## ✨ 核心特性

* **全格式支持**：同时支持 18 位（现代）和 15 位（旧版）身份证号码的解析与校验。
* **深度校验**：不仅校验长度和数字格式，还对**出生日期**合法性（闰年/大小月）及 18 位身份证的**校验码**（ISO 7064:1983）进行严格验证。
* **灵活提取**：支持从身份证中提取生日（可转为 `Date`, `LocalDate`, `LocalDateTime` 等多种类型）、年龄、性别等信息。
* **对象化封装**：提供 `get()` 方法将解析后的所有信息直接封装为 `IdCard` 对象，方便业务传输。

## 🚀 常用方法概览

### 1. 合法性校验 (Validation)

提供两种校验模式：一种返回布尔值，一种在校验失败时抛出具体异常（便于前端提示具体错误原因）。

| 方法名 | 描述 |
| :--- | :--- |
| `validate(String number)` | 校验身份证号码是否有效。返回 `true`/`false`，内部会吞掉异常。 |
| `exceptionValidate(String number)` | **异常校验**。若无效则抛出 `IllegalArgumentException`，异常信息包含具体错误原因（如“长度应为15或18”、“校验码无效”等）。 |
| `validateCheckCode(String number)` | 仅校验 18 位身份证的最后一位**校验码**是否符合 ISO 7064:1983 标准。 |

**示例代码**:

```java
String idCard = "110101199003074518";

// 1. 普通校验
boolean isValid = IdCardUtil.validate(idCard);

// 2. 异常校验 (用于捕获具体错误)
try {
    IdCardUtil.exceptionValidate("11010119900307451X"); // 假设校验码错误
} catch (IllegalArgumentException e) {
    System.err.println("校验失败：" + e.getMessage());
    // -> 校验失败：Number：the check code of index 17 is invalid
}
```

### 2. 信息提取 (Extraction)

所有提取方法均支持 `isValidate` 参数（默认为 `true`）。若设为 `false`，则会跳过格式校验直接强行截取，适用于已确认身份证合法的场景以提升性能。

#### 2.1 基础信息

| 方法名               | 描述             | 示例 (110105199001011234) |
|:------------------|:---------------|:------------------------|
| `getProvinceCode` | 获取省级编码 (前2位)   | `"11"` (北京)             |
| `getCityCode`     | 获取市级编码 (3-4位)  | `"01"`                  |
| `getDistrictCode` | 获取区级编码 (5-6位)  | `"05"`                  |
| `getCheckCode`    | 获取校验码 (仅18位有效) | `"4"`                   |

#### 2.2 出生日期与年龄

`getBirthday` 支持泛型返回，可根据需求直接获取目标类型的日期对象。

| 方法名 | 描述 |
| :--- | :--- |
| `getBirthday(number, Class<T>)` | 获取出生日期。支持 `LocalDate.class`, `Date.class`, `String.class`, `Long.class` (时间戳) 等。 |
| `getAge(number)` | 根据出生日期计算**当前周岁**。 |

```java
String id = "110101199001011234";

// 获取 LocalDate
LocalDate date = IdCardUtil.getBirthday(id, LocalDate.class);
// -> 1990-01-01

// 获取格式化字符串
String dateStr = IdCardUtil.getBirthday(id, String.class);
// -> "1990-01-01"

// 获取年龄
int age = IdCardUtil.getAge(id);
// -> 34 (假设当前是2024年)
```

#### 2.3 性别判断

| 方法名                 | 描述                             |
|:--------------------|:-------------------------------|
| `getGender(number)` | 获取性别。返回 `1`：男, `2`：女, `-1`：错误。 |
| `isMale(number)`    | 是否为男性。                         |
| `isFemale(number)`  | 是否为女性。                         |

### 3. 对象解析 (Parsing)

将身份证号解析为一个包含所有信息的 JavaBean。

| 方法名                  | 描述                 |
|:---------------------|:-------------------|
| `get(String number)` | 解析并返回 `IdCard` 对象。 |

```java
IdCard info = IdCardUtil.get("110101199001011234");

System.out.println(info.getProvinceCode()); // -> 11
System.out.println(info.getBirthday());     // -> 1990-01-01 (LocalDate)
System.out.println(info.getGender());       // -> 1 (男)
```

## ⚠️ 注意事项

1. **15 位身份证**：工具类支持 15 位身份证的解析（年份自动补全为 "19xx"），但 15 位身份证没有校验码，无法进行 `validateCheckCode` 校验。
2. **地区码校验**：`validate` 方法**不校验**前 6 位地区码的真实性（因为行政区划调整频繁），仅校验其是否为数字。如需校验地区码是否存在，需配合行政区划数据库自行实现。
3. **异常处理**：提取类方法（如 `getAge`）在 `isValidate=true` 且校验失败时，通常会返回 `null` 或 `-1`，而不会抛出异常。
