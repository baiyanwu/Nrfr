# Nrfr

[简体中文](README.md) | [English](README_EN.md)

基于 Shizuku 的免 Root SIM 卡运营商配置修改工具。

## v1.0.5：Android 12+ 单 APK

Android 12 及以上只需安装主 APK，并保持 [Shizuku](https://github.com/RikkaApps/Shizuku) 已安装、运行和授权。配置操作通过主应用自身的 Instrumentation 执行，使用 `--no-restart` 保留主应用进程。

| Android 版本 | 执行路径 | 安装要求 |
| --- | --- | --- |
| 12 及以上 | 自身 Instrumentation＋`--no-restart` | 主 APK＋Shizuku |
| 10–11 | 独立 helper 目标包的 Instrumentation | 同版本主 APK、helper＋Shizuku |
| 8–9 | 旧 Shizuku Binder 回退 | 主 APK＋Shizuku；原生系统无覆盖接口，修改能力取决于 ROM |

最低安装版本仍为 Android 8。**安装兼容不代表系统一定支持配置覆盖。** Android 16 的 Seeker 已验证无 helper 的修改、还原和原配置恢复，操作期间主进程保持不变。Android 8–11 本轮未做实机复测，真实持久化覆盖的清理能力尚未验证。详见[验证记录](docs/SINGLE_APK_VALIDATION.md)和[更新日志](CHANGELOG.md)。

## 功能

- 使用 Shizuku 获取系统能力，修改 SIM 国家码和运营商名称。
- 查看 SIM1、SIM2 的当前配置及覆盖状态，分别保存或还原覆盖配置。
- 默认跟随系统语言；支持简体中文、繁體中文、English、日本語、한국어和 Español。
- 在“设置 → 语言”中切换语言，语言名称始终以各自原文显示。

## 安装与升级

从 [v1.0.5 发布页](https://github.com/baiyanwu/Nrfr/releases/tag/v1.0.5) 下载 APK，并先启动 Shizuku。

**Android 12 及以上：** 只安装 `nrfr-v1.0.5.apk`。

```bash
adb install -r nrfr-v1.0.5.apk
```

旧版已安装的 helper 可以暂时保留；主应用不会在这些系统上依赖它。确认主应用配置修改和还原正常后，可以移除 helper。

**Android 10–11：** 安装同一发布版本的两个 APK，先装 helper。

```bash
adb install -r nrfr-instrumentation-target-v1.0.5.apk
adb install -r nrfr-v1.0.5.apk
```

helper 包名为 `com.github.nrfr.instrumentationtarget`，没有桌面图标，无需单独打开。旧系统的 helper 模块和执行分支继续保留。

**Android 8–9：** 保留主 APK 的旧接口回退，但原生系统没有配置覆盖接口；安装 helper 不能补上系统缺失的接口。

自动发布沿用现有的 Debug 签名构建。覆盖升级要求新旧 APK 的签名证书一致；自行构建、历史发布包和本地实测包的证书可能不同。签名不匹配时不会覆盖安装，请先核对版本来源和证书。

## 提交 Issue

请提供手机“关于手机”或“设备信息”页的截图或完整文字信息，并明确提供 **Android 系统版本**（例如 Android 15、Android 16）。HyperOS、One UI、ColorOS、OriginOS 等厂商系统版本不能替代 Android 版本。请同时说明 Nrfr 版本、Shizuku 是否运行，以及旧系统是否安装同版本 helper。

## 构建

需要 JDK 17 或兼容版本，以及 Android SDK 35；仓库自带 Gradle 8.9 Wrapper。

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :instrumentation-target:assembleDebug
```

产物位于 `app/build/outputs/apk/debug/app-debug.apk` 和 `instrumentation-target/build/outputs/apk/debug/instrumentation-target-debug.apk`。

本地 Release 使用 `:app:assembleRelease :instrumentation-target:assembleRelease`。两个模块共用 `gradle.properties` 的版本号，并支持 `local.properties` 中的 `signing.storeFile`、`signing.storePassword`、`signing.keyAlias`、`signing.keyPassword`；签名文件和密码不提交到仓库。

## 发布

[Build Release](.github/workflows/build.yml) 在推送 `v*` 标签时检查标签与应用版本一致，运行单元测试和 Lint，打包 APK，并根据 `CHANGELOG.md` 创建 GitHub Release。每个版本仍提供：

- `nrfr-<version>.apk`：主应用，Android 12+ 只需此 APK。
- `nrfr-instrumentation-target-<version>.apk`：Android 10–11 使用的 helper。
- `SHA256SUMS.txt`：两个 APK 的 SHA-256 校验文件。

也可以在 GitHub Actions 中手动选择版本和是否预发布。发布前需同步两个模块共用的版本号并补齐更新日志。

## 开源与免责

本仓库 fork 自 [Ackites/Nrfr](https://github.com/Ackites/Nrfr)，重点适配 Android 16，移除桌面客户端。

本项目遵循 [Apache-2.0 License](LICENSE)，保留原项目归属说明，但不是上游官方版本。

本工具仅供学习和研究。修改运营商配置可能影响网络、漫游及运营商功能，使用者需自行承担风险。
