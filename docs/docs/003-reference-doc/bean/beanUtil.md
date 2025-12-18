# BeanUtil Bean 工具类

> 📦 **包路径**: `top.csaf.bean.BeanUtil`
>
> 🧬 **继承关系**: 继承自 `org.springframework.beans.BeanUtils`

**BeanUtil** 是一个增强型的 Bean 工具类。它在 Spring 的 `BeanUtils` 基础上，扩展了 **Bean 转 Map**、**List 对象拷贝** 以及 **深克隆** 等实用功能。 🛠️

## ✨ 核心特性

* **Spring 兼容**: 继承自 `org.springframework.beans.BeanUtils`，可以直接使用 Spring 的原有方法（如 `copyProperties`）。
* **深度转换**: `toMap` 方法支持将 Bean 的属性（包括嵌套属性）转换为 Map。
* **批量拷贝**: 提供了方便的 List 转换方法。
* **JSON 深克隆**: 利用 JSON 序列化机制实现对象的深拷贝。

## 🚀 方法概览

| 方法名 | 描述 | 输入示例                                       |
| :--- | :--- |:-------------------------------------------|
| `toMap` | 将 Bean 深层转换为 Map | `new User("admin")` ➡️ `{"name": "admin"}` |
| `toBean` | 将 List 中的对象拷贝为目标类型 List | `[UserBO]` ➡️ `[UserVO]`                   |
| `deepClone` | 对象深克隆 (基于 JSON) | `user` ➡️ `clonedUser`                     |

## 📝 详细用法

### 1. Bean 转 Map (`toMap`)

将 Java Bean 转换为 `Map<String, Object>`。

* 支持任意深度的对象属性转换。
* 处理了 `Map` 类型本身的情况。

```java
MyBean bean = new MyBean();
bean.setName("张三");
bean.setAge(18);

// 转换为 Map
Map<String, Object> map = BeanUtil.toMap(bean);
// -> {name=张三, age=18, ...}
```

### 2. List 转换 (`toBean`)

将源 List 中的每个对象，拷贝属性并转换为目标 Class 类型的对象，生成新的 List。

* **场景**: 可以用于`DTO`、`BO`、`VO`类的相互转换（更推荐 [MapStruct](https://mapstruct.org/)）。
* **注意**: 目标类必须包含**无参构造函数**。

```java
List<UserDO> doList = new ArrayList<>();
doList.add(new UserDO("user1"));
doList.add(new UserDO("user2"));

// 批量转换为 UserVO
List<UserVO> voList = BeanUtil.toBean(doList, UserVO.class);
```

### 3. 深克隆 (`deepClone`)

通过序列化和反序列化（基于 `JsonUtil`）实现对象的深克隆。

* 相较于 `BeanUtils.copyProperties` 的浅拷贝，深克隆会完全复制对象及其引用的子对象。

```java
User user = new User();
user.setRoles(Arrays.asList("admin", "manager"));

// 深克隆
User clonedUser = BeanUtil.deepClone(user);

// 修改原对象不会影响克隆对象
user.getRoles().set(0, "guest");
System.out.println(clonedUser.getRoles().get(0)); // 仍为 "admin"
```
