# FileUtil 文件工具类

> 📦 **包路径**: `top.csaf.io.FileUtil`
>
> 🔗 **所属模块**: `zutil-io`
>
> 🧬 **继承关系**: 继承自 `org.apache.commons.io.FileUtils`

**FileUtil** 是一个强大的文件操作工具类。它继承自 Apache Commons IO 的 `FileUtils`，保留了其所有强大的文件读写、拷贝、移动等功能，并在此基础上扩展了**项目路径获取**、**资源文件加载**、**文件名/扩展名解析**以及**路径替换**等实用功能。

## ✨ 核心特性

* **完全兼容**: 继承自 `commons-io`，无缝使用其所有标准文件操作方法（如 `writeStringToFile`, `copyFile` 等）。
* **路径获取**: 轻松获取项目根路径、工作目录、ClassPath 根路径，自动处理不同操作系统的路径差异（如 Windows 下去除开头的 `/`）。
* **资源加载**: 简化 `resources` 目录下文件的流获取，无需关心路径前缀问题。
* **正则解析**: 利用反向正则高效提取文件名、扩展名、目录路径。
* **路径模板**: 提供支持变量（$1, $2）的文件路径替换功能，方便文件重命名或迁移。

## 🚀 常用方法概览

### 1. 路径与环境获取 (Path & Env) 🌍

获取当前运行环境的相关路径。

| 方法名 | 描述 | 示例 |
| :--- | :--- | :--- |
| `getUserDir` | 获取工作目录 (`user.dir`) | Tomcat Bin 目录或项目根目录 |
| `getProjectPath` | 获取项目规范路径 | `/path/to/project` |
| `getResourceRootPath` | 获取类加载根路径 (target/classes) | `/path/to/classes/` |
| `getClassPath` | 获取指定类的 `.class` 文件所在路径 | `/path/to/classes/top/csaf/io/` |

**示例代码**:

```java
// 获取 resources 根目录 (target/classes)
String rootPath = FileUtil.getResourceRootPath();

// 获取当前类的路径
String classPath = FileUtil.getClassPath(FileUtil.class);

```

### 2. 资源加载 (Resources) 📂

简化从 ClassPath (`src/main/resources`) 读取文件的操作。

```java
// 读取 resources/config/app.yml
InputStream is = FileUtil.getResourceAsStream("config/app.yml");

// 相对于指定类加载资源
InputStream is2 = FileUtil.getResourceAsStream(MyClass.class, "test.txt");

```

### 3. 文件名与扩展名解析 (Parse) 🔍

使用正则精确解析文件路径中的各个部分。

| 方法名 | 描述 | 输入示例 | 输出示例 |
| --- | --- | --- | --- |
| `getFileExtension` | 获取文件后缀名 | `/a/b/test.txt` | `txt` |
| `getNameByPath` | 获取文件名 (含后缀) | `/a/b/test.txt` | `test.txt` |
| `getDirPathByPath` | 获取父目录路径 | `/a/b/test.txt` | `/a/b/` |
| `getDirPathAndNameByPath` | 同时获取目录和文件名 | `/a/b/test.txt` | `["/a/b/", "test.txt"]` |

```java
String path = "/data/logs/error.log";

String ext = FileUtil.getFileExtension(path); // -> "log"
String name = FileUtil.getNameByPath(path);   // -> "error.log"
String dir = FileUtil.getDirPathByPath(path); // -> "/data/logs/"

```

### 4. 路径替换与重命名 (Replace) 🔄

支持使用模板变量生成新路径，常用于文件重命名或格式转换场景。

* **$1**: 原文件名 (不含后缀)
* **$2**: 原后缀名

```java
String src = "/data/images/photo.png";

// 场景 1: 修改后缀名 (photo.png -> photo.jpg)
// 只有文件名部分，保持原目录不变
String newPath1 = FileUtil.replace(src, "$1.jpg");
// -> "/data/images/photo.jpg"

// 场景 2: 移动目录并重命名 (photo.png -> /backup/photo_bak.png)
String newPath2 = FileUtil.replace(src, "/backup/$1_bak.$2");
// -> "/backup/photo_bak.png"

```

## 📚 继承能力 (Commons IO)

由于继承自 `org.apache.commons.io.FileUtils`，你还可以直接使用以下常用静态方法：

* `readFileToString(File, Charset)`: 读取文件内容为字符串。
* `writeStringToFile(File, String, Charset)`: 字符串写入文件。
* `copyFile(File, File)`: 复制文件。
* `forceDelete(File)`: 强制删除文件/目录。
* `sizeOf(File)`: 获取文件/目录大小。

> 更多父类功能请参考 [Commons IO 官方文档](https://commons.apache.org/proper/commons-io/javadocs/api-release/org/apache/commons/io/FileUtils.html)。
