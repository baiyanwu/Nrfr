# 更新日志 / Changelog

本文件记录 Nrfr 各版本的重要变更。

This file records notable changes for each Nrfr release.

## v1.0.5 — 2026-10-21

### 中文

- Android 12 及以上改为主 APK＋Shizuku，使用自身 Instrumentation 和 `--no-restart`，配置操作期间保留主应用进程，不再要求安装 helper。
- Android 10–11 保留独立 helper 路径；发布包继续包含同版本 helper。Android 8–9 保留旧 Shizuku Binder 回退，但原生系统缺少覆盖接口，修改能力取决于 ROM。
- 配置操作移至 Instrumentation 工作线程并串行执行，严格校验退出码、结果码和成功标记。
- 还原配置时，仅在系统明确确认没有持久化覆盖时跳过持久化清理，避免 Android 16 上不必要的权限错误；未知状态或已有覆盖仍尝试清理并保留失败警告。
- 兼容 Android 10 的双参数覆盖接口与新系统的三参数接口，补充版本分流、覆盖接口和失败结果测试，更新六种语言的应用说明。
- Seeker / Android 16 已完成无 helper 的修改、还原和原配置恢复验证；Android 8–11 尚未做本轮实机复测，真实持久化覆盖的清理能力尚未验证。
- 主应用与 helper 同步为 `1.0.5`（versionCode `5`）；更新安装说明，发布流程增加 Lint 和标签版本一致性检查。

### English

- Android 12+ now uses the main APK and Shizuku, with self-targeted Instrumentation and `--no-restart`. Configuration operations retain the main process and no longer require the helper.
- Android 10–11 retains the separate helper path, and releases still include a matching helper APK. Android 8–9 retains the legacy Shizuku Binder fallback, but stock systems lack the override API; support depends on the ROM.
- Run instrumentation work on its worker thread, serialize invocations, and require successful shell, instrumentation, and operation results.
- Skip persistent reset only when the system explicitly reports no persistent override, avoiding unnecessary permission failures on Android 16. Existing or unknown persistent state still triggers a reset attempt and reports failures.
- Support Android 10's two-argument override API and newer three-argument APIs, add routing and failure-result tests, and update app descriptions in all six languages.
- On a Seeker running Android 16, verified changes, reset, and restoration of the original configuration without the helper. Android 8–11 devices and clearing actual persistent overrides have not been verified in this run.
- Synchronize the app and helper at `1.0.5` (versionCode `5`), update installation instructions, and add Lint and tag/version checks to publishing.

## v1.0.4 — 2026-08-24

### 中文

#### 新增

- 新增设置页面，提供语言和关于入口。
- 新增应用内语言切换，默认跟随系统，并支持简体中文、繁體中文、English、日本語、한국어和 Español；语言选择器中的名称始终以各自原文显示。
- 新增本地 Release 签名配置，主应用与 helper 共用 `local.properties` 中的签名信息，签名文件不会提交到仓库。
- 补齐 Gradle Wrapper，并增加运营商配置解析、Instrumentation 结果解析、Shell 参数转义和预设数据校验等基础单元测试。

#### 改进

- 主应用与 helper 共用版本号，当前同步为 `1.0.4`（versionCode `4`）。
- 将 AGP 从 `8.7.0-rc01` 升级到稳定版 `8.7.0`，使用 Gradle `8.9`，并将 Compose BOM 升级到稳定版 `2024.09.03`。
- 改进 Shizuku 监听器的注册和移除逻辑，完善运营商配置操作的错误提示与多语言显示。
- 删除桌面客户端及相关内容，项目仅保留 Android 主应用和 helper。
- 发布流程改为推送 `v*` 标签后自动测试、打包两个 APK、生成更新说明与 SHA-256 校验文件，并创建 GitHub Release。

### English

#### Added

- Added a Settings page with Language and About entries.
- Added in-app language switching. The app follows the system language by default and supports 简体中文, 繁體中文, English, 日本語, 한국어, and Español. Language names are always shown in their native form.
- Added local Release signing configuration shared by the main app and helper through `local.properties`; signing files remain outside version control.
- Added the complete Gradle Wrapper and basic unit tests for carrier configuration parsing, instrumentation result parsing, shell argument quoting, and preset-data validation.

#### Improved

- Synchronized the main app and helper at version `1.0.4` (versionCode `4`).
- Upgraded AGP from `8.7.0-rc01` to stable `8.7.0`, retained Gradle `8.9`, and upgraded the Compose BOM to stable `2024.09.03`.
- Improved Shizuku listener registration and removal, with clearer and fully localized carrier-configuration errors.
- Removed the desktop client and related content; the project now contains only the Android app and helper.
- Updated publishing so a pushed `v*` tag runs tests, builds both APKs, generates release notes and SHA-256 checksums, and creates a GitHub Release.
