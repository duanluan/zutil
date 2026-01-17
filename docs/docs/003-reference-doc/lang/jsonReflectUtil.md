# JsonReflectUtil JSON 反射工具类

> 📦 **包路径**：`top.csaf.lang.JsonReflectUtil`
>
> 🔗 **所属模块**：`zutil-core`

**JsonReflectUtil** 是一个专门用于处理多 JSON 框架兼容性的工具类。
它旨在解决不同 JSON 库（如 Gson, Jackson）生成的对象包装类（Wrapper Class）在通用逻辑中无法直接当做普通 Java 对象使用的问题。通过**反射机制**，它可以在不引入这些库作为强依赖的情况下，动态提取出对象内部的真实值。

## ✨ 核心特性

* **零强依赖**：仅通过反射调用方法，无需在 `pom.xml` 中引入 Gson 或 Jackson 的依赖，避免了依赖传递和版本冲突问题。
* **统一入口**：提供单一方法 `getValue`，自动识别对象类型并分发处理逻辑。
* **智能解包**:
    * **Gson**：自动识别 `JsonPrimitive`，将其转换为 `BigDecimal`（数字时）或 `String`。
    * **Jackson**：自动识别 `ValueNode`，根据具体类型提取 `numberValue` 或 `textValue`。
* **容错设计**：如果反射调用失败（如方法名变更或权限问题），会记录警告日志并优雅降级返回原对象。

## 🚀 常用方法概览

### 1. 提取真实值 (Unwrap Value)

尝试解包 JSON 元素对象，提取其底层的 Java 原生对象（如 `String`, `BigDecimal`, `Integer` 等）。

| 方法名 | 描述 | 支持的源类型 |
| :--- | :--- | :--- |
| `getValue(Object obj)` | 检查传入对象是否为已知的 JSON 包装类，如果是则提取值；否则原样返回。 | `com.google.gson.JsonPrimitive`<br>`com.fasterxml.jackson.databind.node.ValueNode` |

**示例代码**:

> 场景假设：你的通用工具方法接收一个 `Object` 参数，上游可能传给你一个 Gson 的 `JsonObject` 解析出的字段值。如果不处理，你拿到的将是 `JsonPrimitive` 对象而不是字符串 "abc"。

```java
// 假设 obj 是通过 Gson 解析得到的 JsonPrimitive("100")
Object obj = new com.google.gson.JsonPrimitive(100);

// 1. 直接使用 (错误示范)
// System.out.println(obj instanceof Integer); // -> false

// 2. 使用工具类解包
Object realValue = JsonReflectUtil.getValue(obj);

// 结果是 BigDecimal (Gson 默认行为)
System.out.println(realValue.getClass()); // -> java.math.BigDecimal
System.out.println(realValue); // -> 100
```

## ⚙️ 处理逻辑详解

### Gson 支持
针对 `com.google.gson.JsonPrimitive`:
1.  调用 `isNumber()` 判断是否为数字。
    * 是：调用 `getAsBigDecimal()` 返回 `BigDecimal`。
    * 否：调用 `getAsString()` 返回 `String`。

### Jackson 支持
针对 `com.fasterxml.jackson.databind.node.ValueNode` (及其子类):
1.  调用 `isValueNode()` 确认节点类型。
2.  如果是数字 (`isNumber()`)，调用 `numberValue()`。
3.  如果是文本 (`isTextual()`)，调用 `textValue()`。
4.  其他情况调用 `asText()`。

### Fastjson / Fastjson2
Fastjson 的 `JSONObject` 和 `JSONArray` 通常直接实现了 `Map` 和 `List` 接口，其内部值通常已经是原生 Java 类型。因此该工具类目前对 Fastjson 对象采取**原样返回**策略，交由下游逻辑（如 `CollUtil`）直接处理。
