# DateUtil 时间工具类

> 📦 **包路径**: `top.csaf.date.DateUtil`
>
> 🧬 **继承关系**: 继承自 `org.apache.commons.lang3.time.DateUtils`

**DateUtil** 是一个全能型的时间工具类。它不仅封装了常见的格式化、解析、转换操作，还引入了 **DateFeat** 特性配置（支持全局/线程级设置时区、解析模式等），并提供了复杂的**时间段交集/差集计算**以及**高度定制化的周/月计算**功能。 🕰️

## ✨ 核心特性

* **全面兼容**: 完美支持 `Date` 与 Java 8 `Temporal` (`LocalDate`, `LocalDateTime`, `ZonedDateTime`) 互转。
* **智能解析**: `parse` 方法可根据字符串长度自动识别常用格式，支持多种分隔符（`-`, `/`, `.`）。
* **特性配置**: 深度集成 `DateFeat`，可灵活控制解析严格模式（Strict/Lenient）、默认时区、语言区域等。
* **高级计算**: 支持时间段的**交集**、**差集**计算，以及复杂的倒计时格式化。
* **周月增强**: 提供了精确的“月内周”计算（如：某月第几周的开始/结束天），解决跨月周的痛点。

## 🚀 常用方法概览

### 1. 智能解析 (Parse) 🧠

`DateUtil.parseXxx(String source)` 系列方法能够根据字符串**长度**和**分隔符**自动匹配格式，无需手动指定 Pattern。

**支持的分隔符**: `-` (横杠), `/` (斜杠), `.` (点), `:` (冒号, 仅限时间)

| 长度 | 支持格式示例 (Pattern) | 实际输入示例 |
| :--- | :--- | :--- |
| **19** | `uuuu-MM-dd HH:mm:ss`<br/>`uuuu/MM/dd HH:mm:ss`<br/>`uuuu.MM.dd HH:mm:ss` | `"2023-10-01 12:30:00"`<br/>`"2023/10/01 12:30:00"` |
| **16** | `uuuu-MM-dd HH:mm`<br/>`uuuu/MM/dd HH:mm`<br/>`uuuu.MM.dd HH:mm` | `"2023-10-01 12:30"` |
| **10** | `uuuu-MM-dd`<br/>`uuuu/MM/dd`<br/>`uuuu.MM.dd` | `"2023-10-01"`<br/>`"2023.10.01"` |
| **7** | `uuuu-MM`<br/>`uuuu/MM`<br/>`uuuu.MM` | `"2023-10"` |
| **8** | `HH:mm:ss` | `"12:30:59"` |
| **5** | `HH:mm` | `"12:30"` |

```java
// 自动识别 yyyy-MM-dd HH:mm:ss
LocalDateTime dt1 = DateUtil.parseLocalDateTime("2023-05-20 13:14:00");

// 自动识别 yyyy/MM/dd (斜杠)
LocalDate d1 = DateUtil.parseLocalDate("2023/05/20");

// 自动识别 yyyy.MM (点)
LocalDate d2 = DateUtil.parseLocalDate("2023.05");

// 自动识别 HH:mm
LocalTime t1 = DateUtil.parseLocalTime("12:00");

// 校验字符串是否为有效的时间格式 (内部复用 parse 逻辑)
boolean isValid = DateUtil.validate("2023-05-20", "yyyy-MM-dd");
```

### 2. 格式化 (Format) 🎨

支持 `Date`、`Temporal` 和 时间戳 (`Long`) 的格式化。

| 方法 | 描述 | 示例 |
| :--- | :--- | :--- |
| `format` | 通用格式化 | `DateUtil.format(new Date())` |
| `formatCountdown` | **倒计时格式化** | 见下方示例 |
| `formatBetween` | **时间差格式化** | 计算两个时间差并格式化为 "x天x小时" |

```java
// 基础格式化
DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd"); 
// -> "2023/10/01"

// 倒计时格式化 (智能替换 d, H, m, s 等占位符，支持周 W)
long diffMillis = 100000000L; // 约 1天 3小时 46分
DateUtil.formatCountdown(diffMillis, "d天 HH:mm:ss"); 
// -> "1天 03:46:40"
```

### 3. 当前时间获取 (Now) ⌚

提供了丰富的静态方法获取当前时间，支持指定格式和时区。

```java
// 获取当前时间字符串 (yyyy-MM-dd HH:mm:ss)
String nowStr = DateUtil.now();

// 获取今天日期字符串 (yyyy-MM-dd)
String todayStr = DateUtil.today();

// 获取当前时间戳
long epochSec = DateUtil.nowEpochSecond();
long epochMilli = DateUtil.nowEpochMilli();

// 获取指定时区的当前时间
String zoneNow = DateUtil.now(ZoneId.of("America/New_York"));
```

### 4. 类型转换 (Convert) 🔄

打通 `Date`、`long` (时间戳) 与 Java 8 `Temporal` 之间的壁垒。

```java
Date date = new Date();

// Date -> LocalDateTime / LocalDate / LocalTime
LocalDateTime ldt = DateUtil.toLocalDateTime(date);

// Temporal -> Date (支持指定时区修正)
Date newDate = DateUtil.toDate(ldt);

// 转时间戳
long ts = DateUtil.toEpochMilli(ldt);

// 月份数字转文本 (例如: "1" -> "January" 或 "一月", 取决于 Locale)
String monthText = DateUtil.convertMonthText("1", Locale.ENGLISH); 
// -> "January"
String monthShort = DateUtil.convertMonthShortText("1", Locale.ENGLISH); 
// -> "Jan"
```

### 5. 日期计算与调整 (Calc & Modify) ➕➖

```java
LocalDateTime now = LocalDateTime.now();

// 1. 加减时间
DateUtil.plusOrMinus(now, 3, ChronoUnit.DAYS); // 3天后

// 2. 计算差值 (Between)
long days = DateUtil.between(date1, date2, ChronoUnit.DAYS);

// 3. 调整到最小值/最大值 (Min/Max)
// 将时间调整到当月最后一天 (即修改 DayOfMonth 为最大值)
LocalDateTime lastDay = DateUtil.max(now, ChronoField.DAY_OF_MONTH);
// 将时间调整到当年第一天 (即修改 DayOfYear 为最小值)
LocalDateTime firstDay = DateUtil.min(now, ChronoField.DAY_OF_YEAR);

// 4. 获取一天的开始/结束
LocalDateTime startOfDay = DateUtil.todayMinTime(); // 00:00:00
LocalDateTime endOfDay = DateUtil.todayMaxTime();   // 23:59:59.999...

// 5. 闰年判断
boolean isLeap = DateUtil.isLeapYear(now);
```

## 🧩 高级功能详解

### 1. 时间段集合运算 (Range Set)

处理两个时间段 `[x1, y1]` 和 `[x2, y2]` 的关系。

* **`isIntersection`**: 判断是否相交。
* **`getIntersection`**: 获取交集时间段。
* **`getDifferenceSetsByIntersection`**: 获取**差集**（即从 A 时间段中扣除与 B 时间段重合的部分，可能分裂成两段）。

```java
LocalDateTime x1 = LocalDateTime.parse("2023-01-01T10:00:00");
LocalDateTime y1 = LocalDateTime.parse("2023-01-01T12:00:00");
LocalDateTime x2 = LocalDateTime.parse("2023-01-01T11:00:00"); // 在 x1-y1 中间
LocalDateTime y2 = LocalDateTime.parse("2023-01-01T13:00:00");

// 获取差集: (x1~y1) - (x2~y2)
// 结果应为: (扣除相交部分)
LocalDateTime[][] diff = DateUtil.getDifferenceSetsByIntersection(x1, y1, x2, y2);
```

### 2. 周与月的高级处理 (Week & Month)

针对“月内周”的特殊业务场景（如报表统计）。

* **`getWeekOfMonth`**: 获取某天是本月的第几周（支持配置周起始日规则）。
* **`getStartDayOfWeekOfMonth`**: 获取某月第 N 周的**开始天**。
    * *特点*: 如果该周跨月，会根据 `DateFeat` 的 `ResolverStyle` 决定是返回上月月底、本月1号还是报错。
    * *逻辑示意图*:

      ![getStartDayOfWeekOfMonth 逻辑手稿](DateUtil.getStartDayOfWeekOfMonth.png)

* **`getEndDayOfWeekOfMonth`**: 获取某月第 N 周的**结束天**。
    * *逻辑示意图*:

      ![getEndDayOfWeekOfMonth 逻辑手稿](DateUtil.getEndDayOfWeekOfMonth.png)

```java
LocalDate date = LocalDate.of(2023, 10, 1);

// 获取本月第几周
int weekNum = DateUtil.getWeekOfMonth(date);

// 获取本月第 2 周的开始日期
LocalDate startDay = DateUtil.getStartDayOfWeekOfMonth(date, 2);
```

### 3. 批量生成 (Generation)

* **`getByRange`**: 生成指定范围内的所有日期列表。
* **`getByWeeks` / `getByRangeAndWeeks`**: 根据指定的星期（如 "1,3,5" 代表周一、三、五）筛选或生成日期。

```java
// 获取范围内所有的周一和周五
List<String> weekDays = DateUtil.getByRangeAndWeeks(start, end, "1,5", "yyyy-MM-dd");
```

## ⚙️ DateFeat 特性配置

`DateUtil` 的行为可以通过 `DateFeat` 进行动态调整，支持 **ThreadLocal**（临时生效）和 **Always**（全局生效）两种模式。

为了方便链式调用，推荐使用 `DateFeatConfig`。

### 使用 DateFeatConfig

```java
// 链式配置并应用
DateFeatConfig.set(ResolverStyle.LENIENT) // 设置解析模式为宽容
    .set(Locale.CHINESE)                  // 设置语言为中文
    .set(ZoneId.of("Asia/Shanghai"))      // 设置时区
    .apply();                             // 应用配置 (ThreadLocal)

// 执行操作 (配置生效)
LocalDate d = DateUtil.parseLocalDate("2023-02-30"); // 宽容模式下会自动顺延

// 若需全局永久生效
DateFeatConfig.setAlways(ResolverStyle.STRICT).apply();
```

> 更多详情参考: [DateFeat 时间特性](./003-dateFeat.md)

## ⚠️ 异常处理

* **`IllegalArgumentException`**:
    * Pattern 为空时抛出。
    * 解析字符串长度不符合预期且无法自动匹配时抛出。
    * 在 STRICT 模式下，获取不存在的“第 6 周”等情况时抛出。
* **`DateTimeParseException`**: 解析格式不匹配时由底层抛出（部分方法内部已捕获并返回 null，如 `validate`）。
