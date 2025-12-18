# constant 常量

> 📦 **包路径**: `top.csaf.date.constant`

## DateConst - 时间常量

时间工具类的默认配置值。

如需修改默认行为，请参考 [DateFeat 时间特性](./003-dateFeat.md)。

| 常量名 | 默认值 | 描述 |
| :--- | :--- | :--- |
| `DEFAULT_LOCAL_DATE_PATTERN` | `uuuu-MM-dd` | LocalDate 默认格式 |
| `DEFAULT_LOCAL_DATE_TIME_PATTERN` | `yyyy-MM-dd HH:mm:ss` | LocalDateTime 默认格式 |
| `DEFAULT_LOCAL_TIME_PATTERN` | `HH:mm:ss` | LocalTime 默认格式 |
| `DEFAULT_RESOLVER_STYLE` | `ResolverStyle.STRICT` | 默认解析模式 (严格) |
| `DEFAULT_LOCALE` | `Locale.ENGLISH` | 默认区域 (英文) |
| `DEFAULT_MIN_DATE_YEAR` | `1970` | Date 类型最小年份 |
| `SYSTEM_ZONE_ID` | 系统默认时区 | 当前系统时区 |

## DateDuration - 持续时间

常用时间单位的毫秒数常量。

| 常量名 | 值 (毫秒) | 描述 |
| :--- | :--- | :--- |
| `MILLIS_1000` | 1000 | 1 秒 |
| `WEEK_MILLIS` | 604800000 | 1 周 |
| `DAY_OF_MONTH_MILLIS` | 86400000 | 1 天 |
| `HOUR_MILLIS` | 3600000 | 1 小时 |
| `MINUTE_MILLIS` | 60000 | 1 分钟 |
| `SECOND_MILLIS` | 1000 | 1 秒 |

## DatePattern - 时间格式字符串

包含常用的日期时间格式字符串（支持 `uuuu` 和 `yyyy` 两种年份格式）。

**命名规则**: `[年份类型]_[分隔符]_[时间部分]`
* `UUUU`: `uuuu` (Year)
* `YYYY`: `yyyy` (Year-of-Era)
* `SLASH`: `/`
* `DOT`: `.`

**常用常量概览**:

* **标准格式**: `UUUU_MM_DD_HH_MM_SS` ("uuuu-MM-dd HH:mm:ss")
* **斜杠分隔**: `UUUU_MM_DD_SLASH_HH_MM_SS` ("uuuu/MM/dd HH:mm:ss")
* **点分隔**: `UUUU_MM_DD_DOT_HH_MM_SS` ("uuuu.MM.dd HH:mm:ss")
* **无分隔**: `UUUUMMDDHHMMSS` ("uuuuMMddHHmmss")
* **日期部分**: `UUUU_MM_DD`, `UUUU_MM`, `MM_DD` 等
* **时间部分**: `HH_MM_SS`, `HH_MM`

*(注：类中同时包含对应的 `YYYY` 开头的版本)*

## DateFormatter - 时间格式化器

预定义的 `java.time.format.DateTimeFormatter` 对象。

| 常量名 | 模式/描述 |
| :--- | :--- |
| `YYYY_MM_DD_HH_MM_SS` | `yyyy-MM-dd HH:mm:ss` |
| `YYYY_MM_DD` | `yyyy-MM-dd` |
| `HH_MM_SS` | `HH:mm:ss` |
| `M_EN` / `M_ZH` | `M` (英文/中文) |
| `MM_EN` / `MM_ZH` | `MM` (英文/中文) |
| `MMM_EN` / `MMM_ZH` | `MMM` (英文/中文，如 Jan/一月) |
| `MMMM_EN` / `MMMM_ZH` | `MMMM` (英文/中文，如 January/一月) |

## DateFormat - FastDateFormat

预定义的 `org.apache.commons.lang3.time.FastDateFormat` 对象 (线程安全)。

* `YYYY_MM_DD_HH_MM_SS`
* `YYYY_MM_DD`

## DateRegExPattern - 时间正则

用于解析和提取时间的正则表达式模式。

* `WEEK_OF_MONTH`: 匹配 `W`
* `DAY_OF_MONTH`: 匹配 `d`
* `HOUR_OF_DAY`: 匹配 `H`
* ... (包含所有标准日期时间符号的正则匹配)
