package com.github.nrfr

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.nrfr.i18n.LanguageManager
import com.github.nrfr.ui.screens.AboutScreen
import com.github.nrfr.ui.screens.MainScreen
import com.github.nrfr.ui.screens.SettingsScreen
import com.github.nrfr.ui.screens.ShizukuNotReadyScreen
import com.github.nrfr.ui.theme.NrfrTheme
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    private var isShizukuReady by mutableStateOf(false)
    private var currentScreen by mutableStateOf(AppScreen.Main)

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            isShizukuReady = grantResult == PackageManager.PERMISSION_GRANTED
            if (!isShizukuReady) {
                Toast.makeText(
                    this,
                    getString(R.string.shizuku_permission_required_to_run),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentScreen = savedInstanceState
            ?.getString(CURRENT_SCREEN_KEY)
            ?.let { savedScreen -> AppScreen.entries.firstOrNull { it.name == savedScreen } }
            ?: AppScreen.Main
        enableEdgeToEdge()

        // 初始化 Hidden API 访问
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
            HiddenApiBypass.addHiddenApiExemptions("I")
        }

        // 检查 Shizuku 状态
        checkShizukuStatus()

        // 添加 Shizuku 权限监听器
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

        // 添加 Shizuku 绑定监听器
        Shizuku.addBinderReceivedListener(binderReceivedListener)

        setContent {
            NrfrTheme {
                when (currentScreen) {
                    AppScreen.About -> {
                        BackHandler { currentScreen = AppScreen.Settings }
                        AboutScreen(onBack = { currentScreen = AppScreen.Settings })
                    }

                    AppScreen.Settings -> {
                        BackHandler { currentScreen = AppScreen.Main }
                        SettingsScreen(
                            selectedLanguageTag = LanguageManager.getLanguageTag(this),
                            onLanguageSelected = { languageTag ->
                                if (languageTag != LanguageManager.getLanguageTag(this)) {
                                    LanguageManager.setLanguageTag(this, languageTag)
                                    recreate()
                                }
                            },
                            onShowAbout = { currentScreen = AppScreen.About },
                            onBack = { currentScreen = AppScreen.Main }
                        )
                    }

                    AppScreen.Main -> if (isShizukuReady) {
                        MainScreen(onShowSettings = { currentScreen = AppScreen.Settings })
                    } else {
                        ShizukuNotReadyScreen()
                    }
                }
            }
        }
    }

    private fun checkShizukuStatus() {
        isShizukuReady = if (Shizuku.getBinder() == null) {
            Toast.makeText(
                this,
                getString(R.string.install_and_enable_shizuku),
                Toast.LENGTH_LONG
            ).show()
            false
        } else {
            val hasPermission = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Shizuku.requestPermission(0)
            }
            hasPermission
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_SCREEN_KEY, currentScreen.name)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        super.onDestroy()
    }

    private enum class AppScreen {
        Main,
        Settings,
        About
    }

    private companion object {
        const val CURRENT_SCREEN_KEY = "current_screen"
    }
}
