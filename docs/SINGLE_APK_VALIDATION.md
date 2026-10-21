# 单 APK＋Shizuku 验证记录

日期：2026-10-21（Asia/Shanghai）

设备：Solana Mobile Seeker，Android 16 / API 36

实测版本：Nrfr 1.0.4（versionCode 4），本地 Release 构建；单 APK 改造随后整理为 v1.0.5 发布

## 结论

Seeker 上已经移除当前用户的 `com.github.nrfr.instrumentationtarget`，主 APK＋Shizuku 可以完成国家码、运营商名称的修改和还原。最终安装包复测期间，主进程 PID 始终为 `8612`；修改和还原均为零警告。

本次复测从 `JP / NTT docomo` 改为 `CN / China Mobile`，点击“还原设置”后两个覆盖字段均被清除，随后恢复用户原来的 `JP / NTT docomo`。第二路 SIM 的配置保持原样。

**置信度：高，限本次 Seeker / Android 16 验证范围。Android 8–11 未做实体机复测。**

## 实机证据

| 检查 | 结果 |
| --- | --- |
| 旧版覆盖安装 | 包名、版本兼容、证书及不可调试标记一致，应用 UID 和数据目录保持原样 |
| 连续 `--no-restart` 查询 | 原型阶段三次成功，主进程不重启，配置不变 |
| App 内“保存生效” | 实际通过 Shizuku 启动自身 Instrumentation，系统读回 `cn / China Mobile` |
| App 内“还原设置” | 系统覆盖包为空；独立 CarrierConfig API 查询无国家码、运营商名覆盖；零警告 |
| helper 缺失 | 移除前后都完成完整界面操作周期；最终包在 helper 不存在时通过复测 |
| 原配置恢复 | 最后读回 `jp / NTT docomo`，界面显示一致 |
| 进程及导航 | 最终周期 PID `8612` 不变；设置页进入和返回正常 |

系统还原后可能以空配置包 `{}` 而非 `null` 表示没有覆盖，因此按实际字段和独立 API 查询共同判断，不能只比较是否为 `null`。

## 兼容分流

| 系统 | 当前实现 | 验证边界 |
| --- | --- | --- |
| Android 12+ | 主应用自身 Instrumentation＋`--no-restart`，不依赖 helper | Android 16 Seeker 实测通过 |
| Android 10–11 | 独立的 legacy Instrumentation 入口，继续使用 helper | 保留分支；旧入口在 Android 16 上完成只读冒烟验证，不能替代旧系统实测 |
| Android 8–9 | 保留 Shizuku Binder 回退，跳过这些版本没有的权限接管 API | 原生系统无 `overrideConfig` 接口；不承诺完整修改能力，接口缺失时明确报错 |

Android 10 的配置覆盖接口只有两个参数，新版本另有持久化参数；共用适配器会优先选择三参数接口，缺失时选择两参数接口。单元测试覆盖两种签名、重载优先级以及接口不存在的情况。

依据：[Android 12 命令解析](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/android-12.0.0_r1/cmds/am/src/com/android/commands/am/Am.java)、[Android 8 AIDL](https://android.googlesource.com/platform/frameworks/base/+/android-8.0.0_r1/telephony/java/com/android/internal/telephony/ICarrierConfigLoader.aidl)、[Android 9 AIDL](https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r1/telephony/java/com/android/internal/telephony/ICarrierConfigLoader.aidl)、[Android 10 AIDL](https://android.googlesource.com/platform/frameworks/base/+/android-10.0.0_r1/telephony/java/com/android/internal/telephony/ICarrierConfigLoader.aidl)。

## 还原与权限处理

- 操作放入 Instrumentation 的 `onStart()` 工作线程，避免在保留主界面进程时阻塞其主线程。
- Instrumentation 调用串行执行；只有 shell 退出码、最终结果码和成功标记均正确时才判为成功，缺失结果和错误结果均拒绝。
- 还原前检查对应 SIM 的持久化覆盖：仅当系统明确显示为 `null` 时跳过清理；存在或未知时仍尝试，失败继续显示原有警告。
- Android 16 曾拒绝无条件的持久化清理调用。本次持久化覆盖始终为空，最终还原无需触发这一步。**未证明可以清理真实存在的持久化覆盖。**
- 普通读取回退明确检查电话状态权限；主应用未新增权限弹窗。测试读取所需权限由 UiAutomation 临时采用并释放。

## 构建与静态检查

Gradle 8.9、AGP 8.7.0、Kotlin 2.0.0、JBR 21.0.11。最终执行：

```bash
./gradlew --offline --no-daemon --console=plain \
  :app:testReleaseUnitTest :app:assembleRelease \
  :instrumentation-target:assembleRelease :app:lintRelease
```

结果：**21 项单元测试通过；两个模块 Release 构建通过；Lint 0 错误、35 条非阻断警告。** 现有 Compose 弃用提示、旧系统内联常量和必要隐藏接口相关警告保留。

## v1.0.5 发布源码检查

发布时单独整理源码，保留配置执行逻辑，排除工作区原有的诊断页面和日志功能。对这份独立源码执行 Debug 和 Release 检查：

- 两种构建变体各 **14 项单元测试通过**，无失败、错误或跳过；上述 21 项历史测试包含尚未提交的诊断功能测试。
- 主应用和 helper 的 Debug、Release 均构建成功，版本统一为 `1.0.5` / `5`。
- Debug、Release 的 Lint 均为 **0 错误、34 条非阻断警告**。
- 发布流程检查已验证：接受 `v1.0.5`，拒绝与应用版本不一致的 `v1.0.4`；更新说明仅提取本次版本。

## 交付与本地证据

实测安装包文件名：`nrfr-1.0.4-single-apk-release-device.apk`。这是本地验证包，未作为 GitHub Release 资产上传；正式版本请从 [v1.0.5](https://github.com/baiyanwu/Nrfr/releases/tag/v1.0.5) 下载。

该包沿用 Seeker 已有安装的证书，供覆盖更新；`debuggable=false`。

- APK SHA-256：`83a26ebb78786de59c52c339350ed6091ab61d7900c7b442b0abb506d8e99fde`
- 证书 SHA-256：`215a3e58056f6506a641572bf377ece6eec0b49aba2bf63e47263393c31a072e`
- 本地证据目录：`build/verification/single-apk-20261021/`
- 其中包含签名安装记录、移除前后结果、运行日志、完整构建日志、Lint 报告、源码哈希和最终原始截图。

以上为发布前实机验证记录。v1.0.5 发布整理保留同一配置执行逻辑，排除工作区原有的诊断页面和日志功能，并单独构建检查发布源码。设备验证不代表 GitHub 发布包的签名或构建变体与本地验证包一致。helper 模块继续保留给旧系统，Seeker 当前用户已不再安装 helper。
