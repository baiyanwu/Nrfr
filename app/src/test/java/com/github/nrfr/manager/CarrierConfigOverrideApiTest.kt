package com.github.nrfr.manager

import android.os.PersistableBundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CarrierConfigOverrideApiTest {
    @Test
    fun invokesAndroid10TwoArgumentInterface() {
        val target = TwoArgumentApi()
        CarrierConfigOverrideApi.invoke(target, 7, null, persistent = false)
        assertEquals(7, target.subscription)
    }

    @Test
    fun forwardsPersistenceToNewerInterface() {
        val target = ThreeArgumentApi()
        CarrierConfigOverrideApi.invoke(target, 8, null, persistent = true)
        assertEquals(8, target.subscription)
        assertEquals(true, target.persistent)
    }

    @Test
    fun prefersExplicitPersistenceWhenBothOverloadsExist() {
        val target = BothOverloads()
        CarrierConfigOverrideApi.invoke(target, 9, null, persistent = false)
        assertEquals("three", target.called)
    }

    @Test
    fun rejectsPlatformWithoutOverrideApi() {
        assertThrows(NoSuchMethodException::class.java) {
            CarrierConfigOverrideApi.findMethod(Any::class.java)
        }
    }

    private class TwoArgumentApi {
        var subscription = -1
        fun overrideConfig(subId: Int, @Suppress("UNUSED_PARAMETER") values: PersistableBundle?) {
            subscription = subId
        }
    }

    private class ThreeArgumentApi {
        var subscription = -1
        var persistent = false
        fun overrideConfig(subId: Int, @Suppress("UNUSED_PARAMETER") values: PersistableBundle?, persistent: Boolean) {
            subscription = subId
            this.persistent = persistent
        }
    }

    private class BothOverloads {
        var called = ""
        @Suppress("UNUSED_PARAMETER")
        fun overrideConfig(subId: Int, values: PersistableBundle?) { called = "two" }
        @Suppress("UNUSED_PARAMETER")
        fun overrideConfig(subId: Int, values: PersistableBundle?, persistent: Boolean) { called = "three" }
    }
}
