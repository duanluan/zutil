# 📜 Commit Message Convention / 提交信息规范

为保持 `zutil` 提交历史清晰、可追溯、便于中英文协作，统一使用
[Conventional Commits](https://www.conventionalcommits.org/) 规范，并遵循本文规则。

## 🎯 Core Rules / 核心规则

1. 标题（Header）必须是英文。
2. 标题建议不超过 50 个字符，最大不超过 72 个字符。
3. 如填写正文（Body），采用双语：`[EN]` 在前，`[CN]` 在后。
4. 有多个变更点时，正文必须使用列表。
5. 破坏性变更必须显式标注：`!` 或 `BREAKING CHANGE:`。
6. 一个 commit 只做一件事（single concern）。

## 📐 Message Format / 提交格式

```text
<type>: <subject>
<type>!: <subject>
<type>(<scope>): <subject>
<type>(<scope>)!: <subject>

[optional body]
[EN]
- <English description item 1>
- <English description item 2>

[CN]
- <中文描述条目 1>
- <中文描述条目 2>

[optional footer]
```

说明：
- `<scope>` 可选，但推荐填写。
- 正文可选；如填写正文，需包含 `[EN]` 与 `[CN]` 两段。
- 破坏性变更可在 Header 中追加 `!`（如：`feat(api)!: remove v1 endpoints`）。
- `[optional footer]` 可包含 Issue 关联、破坏性说明等。

## 🧩 Header Rules / 标题规则

Header 格式：

```text
<type>: <subject>
<type>!: <subject>
<type>(<scope>): <subject>
<type>(<scope>)!: <subject>
```

约束：
- `type` 必填，使用小写。
- `scope` 推荐使用小写短词（如 `core`、`http`、`build`）。
- `subject` 使用英文、祈使句或简洁现在时，不加句号。
- 避免模糊描述：禁止使用 `update`, `fix bug`, `misc changes` 等无信息词。

## 🏷️ Type Definitions / 类型说明

| Type | Description (EN) | Description (CN) |
| :--- | :--- | :--- |
| **feat** | A new feature | 新增功能 |
| **fix** | A bug fix | 缺陷修复 |
| **docs** | Documentation only changes | 文档修改 |
| **style** | Code style changes (no logic impact) | 代码格式修改（不影响逻辑） |
| **refactor** | Refactoring without feature or fix | 重构（非新增/非修复） |
| **perf** | Performance improvements | 性能优化 |
| **test** | Add or update tests | 测试新增或调整 |
| **build** | Build system or dependency changes | 构建系统或依赖调整 |
| **ci** | CI config/script changes | CI 配置或脚本调整 |
| **chore** | Misc maintenance changes | 其他维护类修改 |
| **revert** | Revert a previous commit | 回滚历史提交 |

## 🧭 Scope Recommendations / Scope 建议

推荐使用与模块一致的 scope，示例：
- `core`
- `http`
- `io`
- `date`
- `json`
- `test`
- `docs`
- `build`
- `ci`

如果无法明确归属，可省略 scope：

```text
chore: bump dependencies
```

## 📝 Body Rules / 正文规则

正文用于回答两个问题：
- Why: 为什么改。
- What: 改了什么，以及影响范围。

规则：
- 单项变更可用单行；多项变更使用列表。
- 如填写正文，必须按 `[EN]` 再 `[CN]` 顺序。
- 每行建议不超过 72 个字符。
- 描述事实和结果，避免过程流水账。

## 🔖 Footer Rules / 尾注规则

常见 Footer：
- Issue 关闭：`Closes #123`
- Issue 关联：`Refs #123`
- 破坏性说明：`BREAKING CHANGE: <details>`

若存在破坏性变更，推荐同时使用：
- Header 中的 `!`
- Footer 中的 `BREAKING CHANGE:`

## 💡 Examples / 示例

### ✅ Single Change / 单点修改

```text
fix(core): handle null sentinel in StringUtil.isEmpty

[EN]
- Prevent NullPointerException when input is a custom null sentinel.

[CN]
- 避免输入为自定义 null 哨兵对象时抛出空指针异常。

Closes #12
```

### ✅ Multiple Changes / 多项修改

```text
feat(http): enhance HttpUtil request capabilities

[EN]
- Add PATCH request support.
- Add retry strategy for timeout exceptions.
- Update default User-Agent header.

[CN]
- 新增 PATCH 请求支持。
- 增加超时异常重试策略。
- 更新默认 User-Agent 请求头。

Closes #45
Refs #46
```

### ✅ Breaking Change / 破坏性更新

```text
refactor(date)!: simplify parse and format APIs

[EN]
- Remove deprecated `formatOld`.
- Change `parse` return type from `Date` to `LocalDateTime`.

[CN]
- 移除废弃的 `formatOld` 方法。
- 将 `parse` 返回类型从 `Date` 调整为 `LocalDateTime`。

BREAKING CHANGE: parse now returns LocalDateTime instead of Date.
```

### ❌ Anti-Patterns / 反例

```text
fix: fix bug
```

问题：
- 信息不足，无法体现影响范围和具体行为。

```text
update code
```

问题：
- 不符合 Conventional Commits 格式，无法自动生成变更日志。

## ✅ Commit Checklist / 提交前检查

提交前请确认：
- Header 符合以下形式之一：`<type>: <subject>`、`<type>(<scope>): <subject>`、`<type>!: <subject>` 或 `<type>(<scope>)!: <subject>`。
- `subject` 英文且清晰，不超过长度建议。
- 如填写正文，遵循 `[EN]` + `[CN]` 顺序。
- 多项变更使用列表。
- 关联 Issue 已写入 Footer。
- 破坏性变更已添加 `!` 或 `BREAKING CHANGE:`（推荐两者都写）。
