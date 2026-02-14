# CompressUtil 压缩解压工具类

> 📦 **包路径**: `top.csaf.io.CompressUtil`
>
> 🔗 **所属模块**: `zutil-io`
>
> ⚙️ **配置对象**: `top.csaf.io.CompressOptions`

`CompressUtil` 提供统一的压缩/解压入口，按目标或源文件后缀自动识别格式，覆盖 ZIP、TAR 系列、7z、RAR 以及 GZ/BZ2/XZ 单文件压缩格式。

它同时提供安全解压能力（路径穿越防护 + 解压限额）、时间戳保留控制、软链接遍历策略，以及 RAR 外部命令集成能力。

## ✨ 核心特性

* **统一 API**: `compress(...)` 与 `decompress(...)` 覆盖单源和多源场景。
* **自动格式识别**: 通过后缀自动识别格式，不需要显式传 `Format`。
* **安全解压**: 默认拦截越界路径（Zip Slip）并支持条目数/大小限额。
* **可控覆盖策略**: `overwrite` 同时影响压缩输出与解压落盘行为。
* **时间戳保留**: 支持保留条目最后修改时间；GZ 头时间戳自动做秒/毫秒转换。
* **RAR 集成**: 支持 RAR 压缩（调用外部 `rar`/`winrar`）与 RAR 解压（junrar）。

## 🚀 常用方法

### 1. 压缩

| 方法                                                                   | 说明           |
|:---------------------------------------------------------------------|:-------------|
| `compress(Path source, Path target)`                                 | 压缩单个源，使用默认选项 |
| `compress(Path source, Path target, CompressOptions options)`        | 压缩单个源，自定义选项  |
| `compress(List<Path> sources, Path target)`                          | 压缩多个源，使用默认选项 |
| `compress(List<Path> sources, Path target, CompressOptions options)` | 压缩多个源，自定义选项  |

```java
Path sourceDir = Paths.get("D:/data/report");
Path zip = Paths.get("D:/backup/report.zip");

CompressUtil.compress(sourceDir, zip);
```

### 2. 解压

| 方法                                                                 | 说明             |
|:-------------------------------------------------------------------|:---------------|
| `decompress(Path source, Path targetDir)`                          | 解压到目标目录，使用默认选项 |
| `decompress(Path source, Path targetDir, CompressOptions options)` | 解压到目标目录，自定义选项  |

```java
Path archive = Paths.get("D:/backup/report.zip");
Path outDir = Paths.get("D:/restore");

CompressUtil.decompress(archive, outDir);
```

### 3. 格式识别

| 方法 | 说明 |
|:---|:---|
| `detectFormat(Path path)` | 根据文件名后缀识别格式，无法识别返回 `null` |

```java
CompressUtil.Format format = CompressUtil.detectFormat(Paths.get("a.tar.gz"));
// -> TAR_GZ
```

## 🧾 支持格式

### 归档格式

| 后缀                         | 压缩 | 解压 | 备注                            |
|:---------------------------|:--:|:--:|:------------------------------|
| `zip`                      | ✅  | ✅  | ZIP 条目名编码受 `charset` 影响       |
| `tar`                      | ✅  | ✅  | 仅归档不压缩                        |
| `tar.gz` / `tgz`           | ✅  | ✅  | TAR + GZ                      |
| `tar.bz2` / `tbz` / `tbz2` | ✅  | ✅  | TAR + BZ2                     |
| `tar.xz` / `txz`           | ✅  | ✅  | TAR + XZ                      |
| `7z`                       | ✅  | ✅  | Apache Commons Compress 7z 实现 |
| `rar`                      | ✅  | ✅  | 压缩依赖外部命令；解压由 junrar 执行        |

### 单文件压缩格式

| 后缀    | 压缩 | 解压 | 备注       |
|:------|:--:|:--:|:---------|
| `gz`  | ✅  | ✅  | 仅支持单文件输入 |
| `bz2` | ✅  | ✅  | 仅支持单文件输入 |
| `xz`  | ✅  | ✅  | 仅支持单文件输入 |

> 后缀识别顺序会优先匹配复合后缀（如 `tar.gz`、`tar.bz2`、`tar.xz`），再匹配 `gz`/`bz2`/`xz`。

## ⚙️ CompressOptions 关键配置

`CompressOptions` 是 `CompressUtil` 的统一配置入口。

| 配置项                    | 默认值                   | 作用                               |
|:-----------------------|:----------------------|:---------------------------------|
| `bufferSize`           | `64 * 1024`           | 流复制缓冲区大小                         |
| `charset`              | `UTF-8`               | ZIP 条目名编码；为 `null` 时自动回退 `UTF-8` |
| `compressionLevel`     | `Deflater.BEST_SPEED` | 压缩等级；内部会归一化到合法区间                 |
| `overwrite`            | `true`                | 目标已存在时是否覆盖（压缩与解压都生效）             |
| `includeRootDir`       | `true`                | 压缩目录时是否包含根目录本身                   |
| `preserveLastModified` | `true`                | 是否保留最后修改时间                       |
| `followLinks`          | `false`               | 压缩遍历时是否跟随软链接                     |
| `allowUnsafePath`      | `false`               | 解压时是否允许越界路径                      |
| `maxEntries`           | `0`                   | 最大解压条目数，`0` 表示不限                 |
| `maxTotalSize`         | `0`                   | 最大解压总字节数，`0` 表示不限                |
| `maxEntrySize`         | `0`                   | 单条目最大解压字节数，`0` 表示不限              |

```java
CompressOptions options = CompressOptions.builder()
    .overwrite(false)
    .includeRootDir(false)
    .preserveLastModified(true)
    .maxEntries(1000)
    .maxTotalSize(500L * 1024 * 1024) // 500 MB
    .maxEntrySize(100L * 1024 * 1024) // 100 MB
    .build();
```

## 📌 关键行为说明

### 1. 输入与目标规则

* `compress(List<Path>, ...)` 传入空列表会抛出 `IllegalArgumentException`。
* `gz`/`bz2`/`xz` 压缩仅允许单个源文件；传目录或多个源会抛出异常。
* 压缩前会自动创建目标父目录。
* 解压前会自动创建目标目录。

### 2. 覆盖策略 `overwrite`

* 压缩：目标文件存在且 `overwrite=false` 时直接失败。
* 解压：若条目落盘路径已存在且 `overwrite=false`，会因 `CREATE_NEW` 失败而抛异常。

### 3. 路径安全与限额

* 默认 `allowUnsafePath=false`，会拒绝 `../../x` 等越界条目。
* `maxEntries`、`maxTotalSize`、`maxEntrySize` 任一大于 0 时启用限额跟踪。
* 超限会抛出 `IllegalArgumentException`，用于防御异常膨胀包（如 Zip Bomb）。

### 4. 时间戳行为

* `preserveLastModified=true` 时，尽可能还原条目修改时间。
* GZ 文件头中的时间戳为秒，内部会转换为毫秒写回文件系统。

### 5. ZIP 编码行为

* ZIP 压缩/解压都会使用 `charset`。
* 当 `charset == null` 时，内部自动回退为 `UTF-8`，避免空指针问题。

## 🧩 RAR 专项说明

### 1. RAR 压缩（外部命令）

RAR 压缩通过外部进程执行命令：

```text
rar a -r -idq <target> *
```

实现要点：

* 先将源文件按归档结构复制到临时目录，再在临时目录执行命令。
* 进程超时时间为 `300` 秒。
* 执行失败时会携带退出码与命令输出。
* 临时目录会在 `finally` 中清理。

### 2. RAR 命令发现优先级

1. JVM 系统属性：`zutil.rar.command`
2. 环境变量：`ZUTIL_RAR_COMMAND`
3. 内置候选：`rar`、`winrar` 及常见安装路径（Windows/Linux）

```java
System.setProperty("zutil.rar.command", "C:\\Program Files\\WinRAR\\rar.exe");
CompressUtil.compress(Paths.get("D:/data"), Paths.get("D:/backup/data.rar"));
```

### 3. RAR 解压限制

* RAR 解压由 junrar 执行。
* 若遇到 RAR5（`UnsupportedRarV5Exception`）会抛出明确错误，提示改用外部 `unrar/winrar` 解压。

## ⚠️ 异常说明

常见异常场景：

| 异常类型                       | 常见触发场景                                          |
|:---------------------------|:------------------------------------------------|
| `IllegalArgumentException` | 源路径不存在、格式不支持、单文件格式传入目录/多源、条目越界、解压超限、目标已存在但禁止覆盖  |
| `RuntimeException`         | 底层 IO/压缩库错误、外部 RAR 命令失败或超时、RAR 命令不可用、RAR5 解压不支持 |
