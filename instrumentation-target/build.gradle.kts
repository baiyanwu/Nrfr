// Fork 变更说明：本文件为 Ackites/Nrfr fork 新增，定义 Android 16 instrumentation helper APK。
plugins {
    id("com.android.application")
}

android {
    namespace = "com.github.nrfr.instrumentationtarget"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.nrfr.instrumentationtarget"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}
