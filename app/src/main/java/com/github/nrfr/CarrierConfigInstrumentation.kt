package com.github.nrfr

// Fork 变更说明：本文件为 Ackites/Nrfr fork 新增，用于 Android 16 下通过 UiAutomation 调用运营商配置覆盖。

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.os.Bundle
import android.os.Build
import android.os.PersistableBundle
import android.telephony.CarrierConfigManager
import androidx.annotation.RequiresApi
import com.github.nrfr.manager.CarrierConfigOverrideApi
import org.lsposed.hiddenapibypass.HiddenApiBypass

open class CarrierConfigInstrumentation : Instrumentation() {
    private lateinit var operationArguments: Bundle

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            finish(Activity.RESULT_CANCELED, Bundle().apply {
                putString(RESULT_KEY, "failed")
                putString(ERROR_KEY, "Instrumentation requires Android 10 or later")
            })
            return
        }
        operationArguments = Bundle(arguments)
        start()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()
        val arguments = operationArguments
        val result = Bundle()
        try {
            HiddenApiBypass.addHiddenApiExemptions("L")

            val subId = arguments.getString(ARG_SUB_ID)?.toIntOrNull()
                ?: throw IllegalArgumentException(context.getString(R.string.invalid_subscription_id))
            val manager = targetContext.getSystemService(Context.CARRIER_CONFIG_SERVICE)
                    as CarrierConfigManager
            val isQuery = arguments.getString(ARG_QUERY)?.toBooleanStrictOrNull() == true

            if (isQuery) {
                queryCurrentConfig(manager, subId, result)
            } else {
                val persistent = arguments.getString(ARG_PERSISTENT)?.toBooleanStrictOrNull() ?: true
                val overrideValues = buildOverrideValues(arguments)

                // Android 16 禁止 shell 直接调用 overrideConfig，这里通过 UiAutomation 临时采用 shell 权限。
                uiAutomation.adoptShellPermissionIdentity(Manifest.permission.MODIFY_PHONE_STATE)
                try {
                    invokeOverrideConfig(manager, subId, overrideValues, persistent)
                } finally {
                    uiAutomation.dropShellPermissionIdentity()
                }
            }

            result.putString(RESULT_KEY, "ok")
            finish(Activity.RESULT_OK, result)
        } catch (e: Throwable) {
            result.putString(RESULT_KEY, "failed")
            result.putString(ERROR_KEY, e.stackTraceToString())
            finish(Activity.RESULT_CANCELED, result)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission") // READ_PHONE_STATE is adopted below and always released.
    private fun queryCurrentConfig(
        manager: CarrierConfigManager,
        subId: Int,
        result: Bundle
    ) {
        uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_PHONE_STATE)
        try {
            val config = manager.getConfigForSubId(subId) ?: return
            config.getString(CarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING)
                ?.takeIf { it.isNotBlank() }
                ?.let { result.putString(RESULT_COUNTRY_CODE, it) }

            if (config.getBoolean(CarrierConfigManager.KEY_CARRIER_NAME_OVERRIDE_BOOL, false)) {
                config.getString(CarrierConfigManager.KEY_CARRIER_NAME_STRING)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { result.putString(RESULT_CARRIER_NAME, it) }
            }
        } finally {
            uiAutomation.dropShellPermissionIdentity()
        }
    }

    private fun buildOverrideValues(arguments: Bundle): PersistableBundle? {
        if (arguments.getString(ARG_RESET)?.toBooleanStrictOrNull() == true) {
            return null
        }

        return PersistableBundle().apply {
            arguments.getString(ARG_COUNTRY_CODE)?.takeIf { it.length == 2 }?.let {
                putString(CarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING, it.lowercase())
            }
            arguments.getString(ARG_CARRIER_NAME)?.takeIf { it.isNotBlank() }?.let {
                putBoolean(CarrierConfigManager.KEY_CARRIER_NAME_OVERRIDE_BOOL, true)
                putString(CarrierConfigManager.KEY_CARRIER_NAME_STRING, it)
            }
        }
    }

    // Android has no public API for carrier-config overrides. The official shell command is
    // restricted to rooted, non-user builds, so this narrowly scoped hidden-API call is required.
    @SuppressLint("BlockedPrivateApi")
    private fun invokeOverrideConfig(
        manager: CarrierConfigManager,
        subId: Int,
        overrideValues: PersistableBundle?,
        persistent: Boolean
    ) {
        CarrierConfigOverrideApi.invoke(manager, subId, overrideValues, persistent)
    }

    companion object {
        private const val ARG_SUB_ID = "subId"
        private const val ARG_COUNTRY_CODE = "countryCode"
        private const val ARG_CARRIER_NAME = "carrierName"
        private const val ARG_RESET = "reset"
        private const val ARG_PERSISTENT = "persistent"
        private const val ARG_QUERY = "query"
        private const val RESULT_COUNTRY_CODE = "countryCode"
        private const val RESULT_CARRIER_NAME = "carrierName"
        private const val RESULT_KEY = "result"
        private const val ERROR_KEY = "error"
    }
}
