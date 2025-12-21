# 📜 Commit Message Convention / 提交信息规范

为了保持 `zutil` 项目提交历史的整洁与国际化支持，请遵循以下提交规范。

## 📐 Format / 格式

我们采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范。
**规则：**
1. 标题首行必须使用**英文**。
2. 详细描述中，**英文在前，中文在后**。
3. 如果有多个修改项，请使用列表（List）分语言块展示。

```text
<type>(<scope>): <subject (English only, max 50 chars)>

<BLANK LINE>

[EN]
- <English description item 1>
- <English description item 2>

[CN]
- <中文描述条目 1>
- <中文描述条目 2>

<BLANK LINE>

<footer (Breaking changes, Issue references)>
```

## 🏷️ Type / 类型说明

| Type | Description (EN) | Description (CN) |
| :--- | :--- | :--- |
| **feat** | A new feature | 新增功能 |
| **fix** | A bug fix | 修复 Bug |
| **docs** | Documentation only changes | 文档修改 |
| **style** | Formatting, missing semi colons, etc | 代码格式修改（不影响逻辑） |
| **refactor** | A code change that neither fixes a bug nor adds a feature | 代码重构 |
| **perf** | A code change that improves performance | 性能优化 |
| **test** | Adding missing tests or correcting existing tests | 测试用例修改 |
| **build** | Changes that affect the build system or external dependencies | 构建系统或依赖修改 |
| **ci** | Changes to our CI configuration files and scripts | CI 配置修改 |
| **chore** | Other changes that don't modify src or test files | 其他杂项修改 |

## 💡 Examples / 示例

### 🛠️ Single Change / 单点修改
```text
fix(core): fix NPE in StringUtil.isEmpty

[EN] Fixed NullPointerException in StringUtil.isEmpty when input is a specific non-standard null object.

[CN] 修复了当输入字符串为特定非常规 null 对象时，StringUtil.isEmpty 抛出空指针异常的问题。

Closes #12
```

### 📦 Multiple Changes / 多项修改 (列表模式)
```text
feat(http): enhance HttpUtil request methods

[EN]
- Added support for PATCH requests.
- Implemented automatic retry mechanism for timeout exceptions.
- Updated default User-Agent header.

[CN]
- 新增对 PATCH 请求方法的支持。
- 实现了针对超时异常的自动重试机制。
- 更新了默认的 User-Agent 请求头。

Closes #45, #46
```

### 💥 Breaking Change / 破坏性更新
```text
refactor(date): optimize formatting logic

BREAKING CHANGE:

[EN]
- Removed the deprecated `formatOld` method.
- Changed the return type of `parse` from `Date` to `LocalDateTime`.

[CN]
- 移除了已废弃的 `formatOld` 方法。
- 将 `parse` 方法的返回类型从 `Date` 修改为 `LocalDateTime`。
```
