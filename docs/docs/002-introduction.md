# 快速入门

## 🔗 资源

* [Maven 中央库](https://central.sonatype.com/artifact/top.csaf/zutil-all)
* [Maven Repository](https://mvnrepository.com/artifact/top.csaf/zutil-all)

---

## 📦 安装

请在项目的依赖管理文件中添加 **ZUtil**。

> 💡 **提示**: 当前最新版本为 `2.0.0`，请根据实际情况调整。

### Maven

```xml
<dependency>
  <groupId>top.csaf</groupId>
  <artifactId>zutil-all</artifactId>
  <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
// Groovy DSL
implementation 'top.csaf:zutil-all:2.0.0'

// Kotlin DSL
implementation("top.csaf:zutil-all:2.0.0")
```

---

## ⚠️ 安装注意事项 (依赖冲突)

ZUtil 默认引入了 `slf4j-api` 和 `slf4j-simple` 以支持简单的日志输出。

如果你的项目中同时使用了 **Spring Boot Web** (它包含 `spring-boot-starter-logging` 和 `Logback`)，**会发生日志实现冲突**。

请根据你的需求，选择以下**任意一种**方式解决冲突：

### 1. 方式一：排除 ZUtil 的日志依赖 (推荐)

如果你希望使用 Spring Boot 默认的 Logback，请排除 ZUtil 自带的 `slf4j` 依赖。

#### Maven
```xml
<dependency>
  <groupId>top.csaf</groupId>
  <artifactId>zutil-all</artifactId>
  <version>2.0.0</version>
  <exclusions>
    <exclusion>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
    </exclusion>
    <exclusion>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

#### Gradle
```groovy
// Groovy DSL
implementation('top.csaf:zutil-all:2.0.0') {
  exclude group: 'org.slf4j', module: 'slf4j-api'
  exclude group: 'org.slf4j', module: 'slf4j-simple'
}

// Kotlin DSL
implementation("top.csaf:zutil-all:2.0.0") {
  exclude(group = "org.slf4j", module = "slf4j-api")
  exclude(group = "org.slf4j", module = "slf4j-simple")
}
```

### 2. 方式二：排除 Spring Boot 的日志依赖

如果你希望使用 ZUtil 提供的简单日志实现（不推荐在生产环境使用），可以排除 Spring Boot 的日志模块。

#### Maven
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-logging</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

#### Gradle
参考：[Gradle 文档 - 排除传递依赖](https://docs.gradle.org/current/userguide/dependency_downgrade_and_exclude.html#sec:excluding-transitive-deps)

```groovy
// Groovy DSL
implementation('org.springframework.boot:spring-boot-starter-web') {
  exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
}

// Kotlin DSL
implementation("org.springframework.boot:spring-boot-starter-web") {
  exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}
```
