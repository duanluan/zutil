# YamlUtil YAML 工具类

> 📦 **包路径**：`top.csaf.yaml.YamlUtil`
>
> 🔗 **所属模块**：`zutil-all`

**YamlUtil** 是基于 `org.yaml.snakeyaml` 的封装工具类。
它简化了 YAML 文件的加载流程，支持**多层级 Key 的点号访问**（如 `user.info.name`），并内置了强大的**变量占位符替换**功能（支持递归引用），可用于处理复杂的配置文件。

## ✨ 核心特性

* **多源加载**：支持从 `File`、文件路径 `String`、`InputStream`、`Reader` 加载 YAML。
* **深层获取**：支持使用点号（`.`）分隔的 Key 路径直接获取深层嵌套的值，无需手动转换 Map。
* **变量替换**：支持配置文件中的 `${key}` 占位符语法，加载时自动引用同一文件中的其他配置项，支持递归替换（如 A 引用 B，B 引用 C）。
* **灵活配置**：通过 `YamlFeat` 可控制变量未找到时的行为（抛异常或使用默认值）。

## 🚀 常用方法概览

### 1. 加载 YAML (Load)

将 YAML 资源加载为 `Map<String, Object>`。默认开启变量替换 (`isEscape = true`)。

| 方法名                                 | 描述       |
|:------------------------------------|:---------|
| `load(File, [isEscape])`            | 从文件加载。   |
| `load(String filePath, [isEscape])` | 从文件路径加载。 |
| `load(InputStream, [isEscape])`     | 从输入流加载。  |
| `load(Reader, [isEscape])`          | 从字符流加载。  |

*注：`isEscape` 参数默认为 `true`，表示开启 `${xxx}` 变量替换。若不需要替换或文件中包含特殊的 `${}` 字符，可手动传入 `false`。*

### 2. 获取值 (Get)

支持直接从资源加载并获取值，或者从已加载的 Map 中获取值。

| 方法名                               | 描述                            |
|:----------------------------------|:------------------------------|
| `get(Map, key)`                   | 从 Map 中获取值，支持 `a.b.c` 嵌套 Key。 |
| `get(File/String/Stream..., key)` | **一步到位**：加载资源并获取指定 Key 的值。    |

**示例代码**：

假设 `config.yml` 内容如下：
```yaml
app:
  name：MySystem
  version：1.0.0
server:
  port：8080
  # 引用上面的 app.name
  description："Welcome to ${app.name}"
```

```java
// 1. 加载为 Map
Map<String, Object> config = YamlUtil.load("config.yml");

// 2. 获取简单值
Integer port = (Integer) YamlUtil.get(config, "server.port"); 
// -> 8080

// 3. 获取引用替换后的值
String desc = (String) YamlUtil.get(config, "server.description"); 
// -> "Welcome to MySystem"

// 4. 一步获取
String version = (String) YamlUtil.get("config.yml", "app.version");
// -> "1.0.0"
```

## ⚙️ 变量替换与特性配置

`YamlUtil` 默认支持 `${key}` 语法的变量替换。该行为可以通过 **`YamlFeat`** 或 **`YamlFeatConfig`** 进行细粒度控制。

### 占位符处理逻辑

当遇到 `${xxx}` 占位符但找不到对应的 Key 时：
1.  **默认行为**：保持原样（即返回字符串 `"${xxx}"`）。
2.  **配置抛出异常**：抛出 `IllegalArgumentException`。
3.  **配置默认替换值**：替换为指定的全局默认值（如 `null` 或空字符串）。

### 配置示例

使用 `YamlFeatConfig` 进行链式配置（支持 **ThreadLocal** 临时生效或 **Always** 全局生效）：

```java
// 场景：强制要求所有变量必须存在，否则报错
YamlFeatConfig.setEscapeNotFoundThrowException(true).apply();

try {
    // 如果 yaml 中有 ${unknown.key}，将抛出异常
    YamlUtil.load("config.yml");
} finally {
    // 配置是 ThreadLocal 的，使用一次后会自动清除，但在 finally 中清理是个好习惯
    // 或者使用 set...Always 全局设置
}
```

```java
// 场景：找不到变量时替换为空字符串，而不是保留 ${xxx}
YamlFeatConfig.setEscapeNotFoundReplacement("").apply();

// "msg"："Hello ${name}" -> "Hello " (如果 name 不存在)
Map<String, Object> map = YamlUtil.load("config.yml");
```

## ⚠️ 注意事项

1. **文件校验**：`load` 方法内部会校验文件是否存在及是否为普通文件，若校验失败（如路径指向目录）会返回空 Map 或 `null`，并打印 Error 日志。
2. **类型转换**：`get` 方法返回的是 `Object`，实际类型取决于 SnakeYAML 的解析结果（通常是 String, Integer, Map, List 等），需自行强转。
3. **递归引用**：变量替换支持递归，例如 A 引用 B，B 引用 C。但请注意避免**循环引用**（A->B->A），否则可能导致栈溢出或无限循环（当前实现主要是循环替换，需留意性能）。
