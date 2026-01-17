# ClassUtil 类工具类

> 📦 **包路径**：`top.csaf.lang.ClassUtil`
>
> 🔗 **所属模块**：`zutil-core`

**ClassUtil** 是在 `org.apache.commons.lang3.ClassUtils` 基础上的扩展。
这意味着除了可以使用 Apache Commons 提供的所有标准类操作（如 `getAllInterfaces`, `isAssignable` 等）外，还额外提供了针对**基本数据类型包装类**的批量校验功能。

## ✨ 核心特性

* **完全兼容**：继承自 Commons Lang3，无缝兼容现有代码。
* **包装类校验**：专门用于判断类或对象是否属于 Java 的 8 种基本数据类型的包装类（`Integer`, `Long`, `Double`, `Float`, `Character`, `Byte`, `Boolean`, `Short`）。
* **批量处理**：支持可变参数，可一次性校验多个类或对象，只要有一个不符合即返回 `false`。

## 🚀 常用方法概览

### 1. 基本类型包装类判断 (Primitive Wrapper Check)

用于判断给定的类 (`Class`) 或对象实例 (`Object`) 是否为基本数据类型的**包装类**。

| 方法名 | 描述 | 示例 |
| :--- | :--- | :--- |
| `isPrimitiveType(Class<?>...)` | 判断传入的**类**是否均为基本类型的包装类。 | `Integer.class` -> `true`<br>`String.class` -> `false` |
| `isPrimitiveType(Object...)` | 判断传入的**对象**是否均为基本类型的包装类实例。 | `1` (Integer) -> `true`<br>`"abc"` -> `false` |

**示例代码**:

```java
// 1. 校验 Class 对象
// 只有 8 种包装类返回 true (Integer, Long, Double, Float, Character, Byte, Boolean, Short)
boolean isPrim1 = ClassUtil.isPrimitiveType(Integer.class, Long.class); 
// -> true

boolean isPrim2 = ClassUtil.isPrimitiveType(Integer.class, String.class); 
// -> false (因为包含 String)

// 注意：int.class (原生类型) 在此处返回 false，仅匹配包装类
boolean isPrim3 = ClassUtil.isPrimitiveType(int.class); 
// -> false


// 2. 校验 Object 实例
// 自动装箱后，1 变为 Integer，属于包装类
boolean isObj1 = ClassUtil.isPrimitiveType(1, 2L, 3.5); 
// -> true

boolean isObj2 = ClassUtil.isPrimitiveType(1, "Text"); 
// -> false
```

## ⚠️ 注意事项

**关于**`int.class`**vs**`Integer.class`：该工具类中的`isPrimitiveType`方法逻辑是严格比对包装类（如`Integer.class.equals(clazz)`）。因此，如果你传入原生类型`int.class`，结果将为`false`。

**异常与边界处理：**
- 传入 null 返回 false。
- 传入空数组（长度为0）会抛出 IllegalArgumentException。
