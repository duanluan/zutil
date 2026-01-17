# CollUtil 集合工具类

> 📦 **包路径**：`top.csaf.coll.CollUtil`
>
> 🔗 **所属模块**：`zutil-core`

**CollUtil** 是对 `org.apache.commons.collections4.CollectionUtils` 的扩展与增强。
它不仅代理了 Commons Collections 的强大功能，还补充了针对**多集合空值判断**、**元素内容深度校验**（支持所有元素全等、同一位置元素相等）以及**JSON 对象解包比较**等高级特性。

## ✨ 核心特性

* **Commons Collections 增强**：完整代理了 `CollectionUtils` 的常用方法，提供一站式集合操作入口。
* **多维度空判断**:
    * **容器级**：判断多个集合是否均为 Empty。
    * **内容级**：判断集合内是否“全为 Null”或“包含 Null”。
* **深度相等性校验**:
    * **全等校验 (`isAllEquals`)**：判断多个集合中的**所有元素**是否都相等（例如 `[A, A]` 与 `[A]` 视为元素全等）。
    * **索引对齐校验 (`isAllEqualsSameIndex`)**：判断多个集合是否在**同一位置**上拥有相同的元素。
    * **数值宽容**：支持忽略数值类型差异（`1` vs `1.0`）及自动去除末尾 0。
    * **JSON 支持**：比较时自动解包 `JsonReflectUtil` 支持的 JSON 对象（如 Gson/Jackson 包装类）。

## 🚀 常用方法概览

### 1. 元素获取 (Retrieval)

提供更安全的元素获取方式，支持 `Collection`、`Map`、`List`、`Object[]` 等多种类型。

| 方法名                             | 描述                     |
|:--------------------------------|:-----------------------|
| `get(Object object, int index)` | 获取指定索引的元素（支持集合、数组等）。   |
| `get(Map map, int index)`       | 获取 Map 中指定索引位置的 Entry。 |
| `getFirst(Object object)`       | 获取第一个元素。               |
| `getLast(Object object)`        | 获取最后一个元素。              |
| `extractSingleton(Collection)`  | 提取单例集合的唯一元素。           |

### 2. 空值与内容校验 (Empty/Null Checks)

CollUtil 将“空”的概念细分为 **容器空 (Size Empty)** 和 **内容空 (All Null)**。

#### 2.1 容器/对象判空 (Size)
基于 `size() == 0` 或 `null` 的判断。

| 方法名                          | 描述                               |
|:-----------------------------|:---------------------------------|
| `isEmpty(Collection)`        | 判断集合是否为 null 或无元素。               |
| `isNotEmpty(Collection)`     | 判断集合是否不为 null 且有元素。              |
| `sizeIsEmpty(Object)`        | 判断对象（集合/Map/数组/迭代器）是否为空。         |
| `sizeIsEmptys(Object...)`    | 判断**所有**传入的对象是否都为空。              |
| `sizeIsNotEmpty(Object)`     | 判断对象是否不为空。                       |
| `sizeIsNotEmptys(Object...)` | 判断是否**不满足**“所有对象都为空”（即至少有一个不为空）。 |

#### 2.2 内容判空 (Content Null)
遍历元素，判断元素是否为 `null`。

| 方法名                      | 描述                               | 示例                                          |
|:-------------------------|:---------------------------------|:--------------------------------------------|
| `isAllEmpty(Object)`     | 判断对象是否**为空**，或者所有元素**均为 null**。  | `[]` -> `true`<br/>`[null, null]` -> `true` |
| `isAnyEmpty(Object)`     | 判断对象是否**为空**，或者**包含任意 null** 元素。 | `[]` -> `true`<br/>`[1, null]` -> `true`    |
| `isAllEmptys(Object...)` | 判断是否**所有**对象的元素都全为 null。         |                                             |
| `isAnyEmptys(Object...)` | 判断是否**任意**对象的元素包含 null。          |                                             |

### 3. 相等性判断 (Equality Checks)

支持复杂的相等性校验逻辑，常用于数据比对、配置校验等场景。

**核心参数说明**:
* `isToString` (boolean)：是否将元素转换为 String 后比较。
    * 开启后，`Number` 类型会去除小数点后无效的 0（如 `1.00` 视为 `1`）。
    * 自动通过 `JsonReflectUtil` 解包 JSON 包装对象。
* `continueFunction` (Function)：判定对象是否跳过检查的逻辑（返回 `true` 则跳过该对象）。

#### 3.1 所有元素全等 (`isAllEquals`)
判断传入的所有对象（容器）内部的所有元素，是否**全部相等**。即：所有容器的所有元素都必须是同一个值。

```java
// 场景：检查多个列表是否都只包含 "A"
// [A, A] vs [A] -> true (元素都是 A)
CollUtil.isAllEquals(null, Arrays.asList("A", "A"), Collections.singletonList("A"));

// [A, B] -> false (包含不同元素)
CollUtil.isAllEquals(null, Arrays.asList("A", "B"));
```

#### 3.2 索引对齐相等 (`isAllEqualsSameIndex`)
判断多个集合/数组是否**相等**（长度相同，且同一索引位置的元素相等）。

```java
List<Object> list1 = Arrays.asList(1, 2.0);
List<Object> list2 = Arrays.asList("1", "2.00");

// isToString=true：忽略类型和精度差异
// 1 == "1", 2.0 == "2.00" -> true
CollUtil.isAllEqualsSameIndex(true, null, list1, list2);
```

### 4. 集合操作 (Operations)

继承自 Commons Collections 的强大操作能力。

| 分类 | 方法示例 | 描述 |
| :--- | :--- | :--- |
| **集合运算** | `union`, `intersection`, `disjunction`, `subtract` | 并集、交集、交集的补集、差集。 |
| **添加/移除** | `addAll`, `addIgnoreNull`, `removeAll`, `retainAll` | 批量添加（支持迭代器）、忽略 Null 添加、移除、保留。 |
| **筛选/转换** | `filter`, `select`, `selectRejected`, `transform`, `collect` | 过滤、选择、反选、原地转换、映射转换。 |
| **比较/排序** | `isEqualCollection`, `isSubCollection`, `collate` | 集合相等判断、子集判断、合并排序。 |
| **其他** | `permutations`, `getCardinalityMap`, `reverseArray` | 排列组合、频次统计、数组反转。 |

### 5. 其他工具 (Misc)

| 方法名 | 描述 |
| :--- | :--- |
| `contains(Collection, Object)` | 空安全的包含判断（集合为 null 返回 false）。 |
| `emptyIfNull(Collection)` | 如果集合为 null 返回空集合（避免 NPE）。 |

## ⚠️ 注意事项

1. **JSON 自动解包**：`isAllEquals` 系列方法在比较时，会自动检测元素是否为 Gson/Jackson 的包装类（如 `JsonPrimitive`, `ValueNode`），并提取真实值进行比较。
2. **数值比较**：当启用 `isToString=true` 时，`BigDecimal("1.0")`、`Double(1.0)` 和 `String("1")` 会被视为相等。这在处理跨系统数据（如数据库 vs JSON）时非常有用。
3. **迭代器消耗**：部分方法（如 `isAllEquals`）在处理 `Iterator` 或 `Enumeration` 时，为了复用数据，可能会将其转换为 `List`。若传入不可重复消费的流，请留意内存开销。
