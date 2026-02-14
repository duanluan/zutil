![](https://socialify.git.ci/duanluan/zutil/image?description=1&font=Bitter&forks=1&issues=1&language=1&logo=https%3A%2F%2Fduanluan.github.io%2Fzutil%2Fimg%2Flogo.png&name=1&owner=1&pattern=Floating%20Cogs&pulls=1&stargazers=1&theme=Light)

# ZUtil

[![](https://img.shields.io/hexpm/l/plug?style=for-the-badge&logo=apache)](./LICENSE)
[![](https://img.shields.io/maven-central/v/top.csaf/zutil-all?style=for-the-badge&logo=apachemaven)](https://central.sonatype.com/artifact/top.csaf/zutil-all)
[![](https://img.shields.io/badge/JDK-8%2B-orange?style=for-the-badge&logo=openjdk)]()
[![](https://img.shields.io/github/stars/duanluan/zutil?style=for-the-badge&logo=github)](https://github.com/duanluan/zutil)
[![GitHub commits](https://img.shields.io/github/commit-activity/m/duanluan/zutil?style=for-the-badge&label=Commits&logo=github)](https://github.com/duanluan/zutil/commits)
[![](https://img.shields.io/badge/Discord-N39y9EvYC9-e76970?style=for-the-badge&logo=discord&logoColor=f5f5f5)](https://discord.gg/N39y9EvYC9)
[![](https://img.shields.io/badge/QQ%20group-273743748-e76970?style=for-the-badge&logo=tencentqq)](https://jq.qq.com/?_wv=1027&k=pYzF0R18)

[English](./README.md) | [简体中文](./README_CN.md)

A faster and more comprehensive Java utility library.

For usage, please refer to the [documentation](https://duanluan.github.io/zutil) and [javadoc](https://apidoc.gitee.com/duanluan/zutil).

For performance comparison with Hutool, see [jmh.comparison](zutil-all/src/test/java/top/csaf/jmh/comparison).

## Features

- Faster: Use [JMH](https://openjdk.org/projects/code-tools/jmh/) for [performance testing](https://github.com/duanluan/zutil/tree/main/zutil-all/src/test/java/top/csaf/jmh).
- More complete:
  - [DateUtil](https://github.com/duanluan/zutil/blob/main/zutil-date/src/main/java/top/csaf/date/DateUtil.java): 170+ methods, 3300+ lines.
  - [RegExUtil](https://github.com/duanluan/zutil/blob/main/zutil-regex/src/main/java/top/csaf/regex/RegExUtil.java): 140+ methods, 2000+ lines.
- Safer: Use [JUnit](https://junit.org/junit5) for suite testing and [JaCoCo](https://www.jacoco.org/jacoco/index.html) for [code coverage testing](https://github.com/duanluan/zutil/tree/main/zutil-all/src/test/java/top/csaf/junit), ensuring every line of code behaves as expected and reducing bugs.

## Stargazers over time

[![Stargazers over time](https://starchart.cc/duanluan/zutil.svg)](https://starchart.cc/duanluan/zutil)

## Instructions

### Installation

#### Maven

```xml
<dependency>
  <groupId>top.csaf</groupId>
  <artifactId>zutil-all</artifactId>
  <version>2.0.0-beta4</version>
</dependency>
```

#### Gradle

```groovy
// groovy
implementation 'top.csaf:zutil-all:2.0.0-beta4'
// kotlin
implementation("top.csaf:zutil-all:2.0.0-beta4")
```

### Notes on Installation

This library includes `slf4j-api` and `slf4j-simple`, which conflict with `spring-boot-starter-web`. You need to exclude them manually.

#### Maven

```xml
<!-- Option 1: Exclude slf4j from ZUtil -->
<dependency>
  <groupId>top.csaf</groupId>
  <artifactId>zutil-all</artifactId>
   <version>2.0.0-beta4</version>
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

<!-- Option 2: Exclude Logback from spring-boot-starter-web -->
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

Reference: [Excluding transitive dependencies - Gradle User Manual](https://docs.gradle.org/current/userguide/dependency_downgrade_and_exclude.html#sec:excluding-transitive-deps)

```groovy
// groovy
dependencies {
  // Option 1: Exclude slf4j from ZUtil
  implementation('top.csaf:zutil-all:2.0.0-beta4') {
    exclude group: 'org.slf4j', module: 'slf4j-api'
    exclude group: 'org.slf4j', module: 'slf4j-simple'
  }
  // Option 2: Exclude Logback from spring-boot-starter-web
  implementation('org.springframework.boot:spring-boot-starter-web') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
  }
}

// kotlin
dependencies {
  // Option 1: Exclude slf4j from ZUtil
  implementation("top.csaf:zutil-all:2.0.0-beta4") {
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "org.slf4j", module = "slf4j-simple")
  }
  // Option 2: Exclude Logback from spring-boot-starter-web
  implementation("org.springframework.boot:spring-boot-starter-web") {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
  }
}
```

### JMH Benchmark Explanation

```java
// Benchmark                                                 Mode     Cnt    Score    Error   Units
// ToPinyinTest.toPinyinByHutool                            thrpt       5    2.880 ±  0.160  ops/us
// ToPinyinTest.toPinyinByZUtil                             thrpt       5    4.577 ±  0.133  ops/us
// ToPinyinTest.toPinyinByHutool                             avgt       5    0.356 ±  0.012   us/op
// ToPinyinTest.toPinyinByZUtil                              avgt       5    0.216 ±  0.006   us/op
// ToPinyinTest.toPinyinByHutool                           sample  175058    0.435 ±  0.008   us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.00    sample            0.300            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.50    sample            0.400            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.90    sample            0.500            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.95    sample            0.500            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.99    sample            0.900            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.999   sample            1.600            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p0.9999  sample           40.900            us/op
// ToPinyinTest.toPinyinByHutool:toPinyinByHutool·p1.00    sample          277.504            us/op
// ToPinyinTest.toPinyinByZUtil                            sample  162384    0.393 ±  0.008   us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.00      sample            0.200            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.50      sample            0.300            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.90      sample            0.500            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.95      sample            0.600            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.99      sample            1.000            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.999     sample            2.500            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p0.9999    sample           45.425            us/op
// ToPinyinTest.toPinyinByZUtil:toPinyinByZUtil·p1.00      sample          170.496            us/op
// ToPinyinTest.toPinyinByHutool                               ss       5   30.880 ± 37.754   us/op
// ToPinyinTest.toPinyinByZUtil                                ss       5   23.060 ± 16.885   us/op
```

Mode (`org.openjdk.jmh.annotations.Mode`):
- thrpt: **Throughput (ops/time)**. Higher is better.
- avgt: **Average time (time/op)**. Lower is better.
- sample: **Sampling time**. Lower is better.
- ss: **Single shot invocation time**. Lower is better.

### Contributing

1. **[Fork](https://github.com/duanluan/zutil/fork)** and **Clone** the repo.
2. Contribution types:
    - **New classes or methods**: please discuss first in the [Discord](https://discord.gg/N39y9EvYC9) or [QQ group](https://jq.qq.com/?_wv=1027&k=pYzF0R18).
    - **Bug fixes** (fix), **performance improvements** (perf), or **tests** (test).
3. Testing:
    - Use `org.junit.jupiter.api.Assertions` for **code coverage testing**:

      ```java
      ……
      import top.csaf.id.NanoIdUtil;
      import static org.junit.jupiter.api.Assertions.*;
      
      @Slf4j
      @DisplayName("NanoId Utility Class Test")
      class NanoIdUtilTest {
      
        @DisplayName("Generate NanoID")
        @Test
        void randomNanoId() {
          /** {@link NanoIdUtil#randomNanoId(int, char[], java.util.Random) } */
          assertThrows(NullPointerException.class, () -> NanoIdUtils.randomNanoId(0, (char[]) null, NanoIdUtils.DEFAULT_ID_GENERATOR));
          assertThrows(NullPointerException.class, () -> NanoIdUtils.randomNanoId(0, new char[0], null));
          assertThrows(IllegalArgumentException.class, () -> NanoIdUtils.randomNanoId(0, new char[0], NanoIdUtils.DEFAULT_ID_GENERATOR));
          assertThrows(IllegalArgumentException.class, () -> NanoIdUtils.randomNanoId(1, new char[0], NanoIdUtils.DEFAULT_ID_GENERATOR));
          assertThrows(IllegalArgumentException.class, () -> NanoIdUtils.randomNanoId(1, new char[256], NanoIdUtils.DEFAULT_ID_GENERATOR));
          assertDoesNotThrow(() -> NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_SIZE, NanoIdUtils.DEFAULT_ALPHABET, NanoIdUtils.DEFAULT_ID_GENERATOR));
        }
      }
      ```
    - Navigate to the **project root directory**, then run the following command to test (using `Reactor` to build dependencies automatically):`mvn test -pl <module-name> -am -Dtest=<TestClassName> "-Dsurefire.failIfNoSpecifiedTests=false"`(e.g., `mvn test -pl zutil-all -am -Dtest=NanoIdUtilTest "-Dsurefire.failIfNoSpecifiedTests=false"`).
    - Run `mvn jacoco:report` to generate the code coverage report in the `target/site` directory.
    - Ensure coverage of updated classes or methods is above **90%** before submitting.
    - Parameter validation using `lombok.NonNull` can be ignored.
4. Please read and follow the **[Commit Convention](./COMMIT_CONVENTION.md)** (based on Angular guidelines) to ensure the bilingual format is correct, then create a **pull request**.
