package com.github.nrfr.manager

import android.os.PersistableBundle
import java.lang.reflect.Method

/** Android 10 has the two-argument API; newer releases also expose a persistence flag. */
internal object CarrierConfigOverrideApi {
    fun findMethod(type: Class<*>): Method = try {
        type.getMethod(
            "overrideConfig", Int::class.javaPrimitiveType, PersistableBundle::class.java,
            Boolean::class.javaPrimitiveType
        )
    } catch (_: NoSuchMethodException) {
        type.getMethod("overrideConfig", Int::class.javaPrimitiveType, PersistableBundle::class.java)
    }

    fun invoke(target: Any, subId: Int, values: PersistableBundle?, persistent: Boolean) {
        val method = findMethod(target.javaClass)
        method.isAccessible = true
        if (method.parameterCount == 3) {
            method.invoke(target, subId, values, persistent)
        } else {
            method.invoke(target, subId, values)
        }
    }
}
