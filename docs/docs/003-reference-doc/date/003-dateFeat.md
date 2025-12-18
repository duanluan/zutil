# DateFeat 时间特性

> 📦 **包路径**: `top.csaf.date.DateFeat`

可以通过 **临时 (ThreadLocal)** 或 **全局 (Always)** 修改其静态成员变量，来决定 `DateUtil` 中方法对时间的处理方式。

推荐使用 **`DateFeatConfig`** 进行链式配置。

## ⚙️ 配置方式

### 链式配置 (推荐)

使用 `DateFeatConfig` 可以优雅地设置特性，支持一次性应用多个配置。

```java
// 1. 临时生效 (ThreadLocal) - 仅当前线程，使用一次后自动失效（在 DateUtil 读取后清除）
DateFeatConfig.set(ResolverStyle.LENIENT) // 设置解析模式为宽容
    .set(Locale.CHINESE)                  // 设置语言为中文
    .set(ZoneId.of("Asia/Shanghai"))      // 设置时区
    .apply();                             // 应用配置

// 2. 全局生效 (Always) - 对所有线程永久生效，优先级高于临时设置
DateFeatConfig.setAlways(ResolverStyle.STRICT)
    .apply();
```

### 静态方法配置

也可以直接调用 `DateFeat` 的静态方法进行设置。

```java
// 临时设置
DateFeat.set(ResolverStyle.LENIENT);

// 全局设置
DateFeat.setAlways(ResolverStyle.STRICT);
```

---

## ✨ 特性详解

### 1. RESOLVER_STYLE - 解析器模式

控制日期解析的严格程度。

* **默认值**: `ResolverStyle.STRICT` (严格模式)
* **STRICT**: 严格模式。例如 2 月 30 日会抛出异常。
* **LENIENT**: 宽容模式。例如 2 月 30 日会自动顺延到 3 月 2 日。
* **SMART**: 智能模式。在合理范围内调整，例如 2 月 30 日可能会变成 2 月 28 日（视具体情况而定）。

```java
// 临时设置解析器模式为宽容
DateFeat.set(ResolverStyle.LENIENT);

// 默认为严格模式，此方法超出时间范围会报错（例如某月没有第 7 周）
// 但如果在调用方法前将模式修改为宽容，则不会报错，正常返回顺延后的日期
DateUtil.getStartDayOfWeekOfMonth(LocalDate.now(), 7);
```

### 2. STRICT_YY_TO_UU - 严格模式下 yy 转 uu

* **默认值**: `true`
* **描述**: 在 `ResolverStyle.STRICT` 模式下，如果格式字符串中包含 `yyyy` 或 `yy` 但不包含 `uuuu` 或 `uu`，是否自动将其替换为 `uuuu` 或 `uu`。
* **背景**: Java 8 的 `DateTimeFormatter` 在 `STRICT` 模式下，`yyyy` (Year-of-Era) 解析行为比较怪异（通常需要 Era 信息），推荐使用 `uuuu` (Year)。开启此特性可自动修正格式字符串，避免解析报错。

```java
// 默认开启，DateUtil 会自动将 yyyy 视为 uuuu 处理
// 若需关闭：
DateFeat.setStrictYyToUu(false);
```

### 3. LOCALE - 区域

控制月份、星期等文本的语言环境，以及某些格式化行为。

* **默认值**: `Locale.ENGLISH`

```java
// 总是设置解析器模式为中文
DateFeat.setAlways(Locale.SIMPLIFIED_CHINESE);

// 输出周时结果为中文 (例如 "星期一")
DateUtil.format(LocalDate.now(), "E");
// 输出月时结果为中文 (例如 "一月")
DateUtil.format(LocalDate.now(), "MMM");
```

### 4. ZONE_ID - 时区

控制时间计算、格式化以及时间戳转换时使用的时区。

* **默认值**: `ZoneId.systemDefault()` (系统默认时区)

```java
// 临时设置时区为 UTC
DateFeat.set(ZoneId.from(ZoneOffset.UTC));

// 假设系统时区为 UTC+8
// 因为上一行设置了时区，所以 formatting 结果会比系统时间少 8 小时
DateUtil.format(LocalDateTime.now());
```

### 5. MIN_DATE_YEAR - 最小 Date 年份

* **默认值**: `1970`
* **描述**: 在将 `LocalTime` (只有时间) 转换为 `Date` (日期+时间) 时，需要补充日期部分。此常量定义了填充的默认年份（默认填充为 `1970-01-01`）。

```java
// 设置转换时的默认年份为 2000
DateFeat.setMinDateYear(2000L);

// LocalTime 转 Date 时，年份将变为 2000
Date date = DateUtil.toDate(LocalTime.now());
```
