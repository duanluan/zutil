# TreeUtil 树结构工具类

> 📦 **包路径**：`top.csaf.tree.TreeUtil`
>
> 🔗 **所属模块**：`zutil-all`

**TreeUtil** 提供了一套通用的树形结构构建与拆分方案。
它能够将扁平的列表数据（如数据库查询结果）转换为层级分明的树形结构，同时也支持将树形结构还原为扁平列表。配合 **TreeNode** 和 **TreeConfig**，支持高度自定义的构建逻辑（如自定义根节点判断、排序、生成层级信息等）。

## 🧩 核心组件

### 1. TreeNode 树节点

`TreeNode` 是构建树的基础数据单元，继承自 `LinkedHashMap<String, Object>`。
这意味着它既是一个标准的 JavaBean（拥有 `id`, `parentId`, `children` 等固定字段），也是一个动态 Map，可以随意扩展其他业务字段。

**核心字段**：
* `id`：节点 ID
* `parentId`：父级 ID
* `name`：节点名称
* `order`：排序值
* `children`：子节点列表

### 2. TreeConfig 树配置

`TreeConfig` 用于控制树的构建行为，支持链式调用。

| 配置项                      | 默认值                       | 描述                                              |
|:-------------------------|:--------------------------|:------------------------------------------------|
| `rootParentIdValues`     | `[0, 0L, "0", null, ...]` | **根节点判定值**。父级 ID 为这些值之一的节点被视为顶级节点。              |
| `isRootByNullParent`     | `false`                   | 当父级 ID 不满足上述值，但在列表中找不到父节点时，是否将其视为顶级节点（孤儿节点策略）。  |
| `isSort`                 | `false`                   | 是否开启排序。开启后需设置 `comparator`。                     |
| `comparator`             | `null`                    | 排序规则 (`Comparator<TreeNode>`)。                  |
| `isGenLevel`             | `false`                   | 是否生成 **层级** 字段（如 1级、2级）。默认字段名为 `level`。         |
| `levelKey`               | `"level"`                 | 层级字段的 Key 名称。                                   |
| `isGenAncestors`         | `false`                   | 是否生成 **祖级路径** 字段（如 `0,1,5`）。默认字段名为 `ancestors`。 |
| `ancestorsKey`           | `"ancestors"`             | 祖级路径字段的 Key 名称。                                 |
| `isGenHasChildren`       | `false`                   | 是否生成 **含有子项** 标记。默认字段名为 `hasChildren`。          |
| `hasChildrenKey`         | `"hasChildren"`           | 含有子项标记字段的 Key 名称。                               |
| `isIgnoreIdTypeMismatch` | `true`                    | 是否忽略 ID 类型不匹配（自动转 String 比较）。                   |
| `idType`                 | `null`                    | 强制转换 ID 和 ParentID 的类型（如转为 Long 对比）。            |

## 🚀 常用方法概览

### 1. 构建树结构 (Build)

将扁平的 `TreeNode` 列表转换为树形结构。

| 方法名                                 | 描述                        |
|:------------------------------------|:--------------------------|
| `build(List<TreeNode>)`             | 使用默认配置构建树。                |
| `build(List<TreeNode>, TreeConfig)` | 使用自定义配置构建树（支持排序、扩展字段生成等）。 |

**基础示例**：

```java
// 1. 准备扁平数据
List<TreeNode> list = new ArrayList<>();
list.add(new TreeNode("1", "根节点", 1, "0"));
list.add(new TreeNode("2", "子节点A", 1, "1"));
list.add(new TreeNode("3", "子节点B", 2, "1"));

// 2. 构建树
List<TreeNode> tree = TreeUtil.build(list);

// 结果：节点 "1" 的 children 中包含 "2" 和 "3"
```

**高级示例（排序与扩展字段）**：
```java
// 配置：开启排序、生成层级信息、生成祖级路径
TreeConfig config = TreeConfig.builder()
    .isSort(true)
    .comparator(Comparator.comparing(TreeNode::getOrder)) // 根据 order 排序
    .isGenLevel(true)      // 生成 level 字段
    .isGenAncestors(true)  // 生成 ancestors 字段
    .build();

List<TreeNode> tree = TreeUtil.build(list, config);

// 结果中子节点将包含：
// level：2
// ancestors："1,2" (视具体 ID 而定)
```

### 2. 树结构扁平化 (Flatten)

将树形结构（包含嵌套子列表）还原为一维列表或数组。该方法基于反射实现，因此支持任意类型的对象，只要该对象包含存储子节点的字段即可。

| 方法名                                                                  | 描述                      |
|:---------------------------------------------------------------------|:------------------------|
| `flatten(Object treeNodes, String childrenKey, Supplier<R> factory)` | 	将集合/数组类型的树扁平化为指定类型的集合。 |
| `flatten(T[] treeNodes, String childrenKey)`                         | 将对象数组类型的树扁平化为数组。        |

**示例代码**：

```java
// 假设 treeList 是已经构建好的树形结构 List<TreeNode>
// "children" 是子节点列表的字段名
List<TreeNode> flatList = TreeUtil.flatten(
    treeList, 
    "children", 
    ArrayList::new // 指定返回 ArrayList
);
```

## ⚙️ 处理逻辑详解

### 根节点识别

`TreeUtil`默认将`parentId`为`0`, `0L`, `"0"`, `null`, `""`, `0d`, `0f`, `(short)0`等值的节点识别为根节点。 如果你的业务中根节点的父 ID 是`-1`，可以通过`TreeConfig`修改：

```java
TreeConfig config = TreeConfig.builder()
    .rootParentIdValues(new Object[]{-1, "-1"})
    .build();
```

### ID 类型宽容

在处理父子关联时，`id`和`parentId`的类型可能不一致（例如一个是`Long`，一个是`String`）。 `TreeUtil`默认开启`isIgnoreIdTypeMismatch`，会将它们统一转为 String 进行比对，从而避免关联失败。如果需要严格类型比对，可设置 `idType`强制转换。
