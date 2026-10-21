package com.github.nrfr.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class CarrierConfigManagerTest {
    @Test
    fun noRestart_isOnlyUsedOnAndroid12AndLater() {
        listOf(26, 27, 28, 29, 30).forEach { assertFalse(CarrierConfigManager.supportsNoRestart(it)) }
        listOf(31, 32, 33, 34, 35, 36).forEach { assertTrue(CarrierConfigManager.supportsNoRestart(it)) }
    }

    @Test
    fun persistentOverrideState_distinguishesAbsentPresentAndUnknownSlots() {
        val output = """
            Phone Id = 0
                mPersistentOverrideConfigs : null
                mOverrideConfigs :
                    sim_country_iso_override_string = jp
            Phone Id = 1
                mPersistentOverrideConfigs :
                    sim_country_iso_override_string = us
        """.trimIndent()

        assertEquals(false, CarrierConfigManager.parsePersistentOverrideState(output, 0))
        assertEquals(true, CarrierConfigManager.parsePersistentOverrideState(output, 1))
        assertNull(CarrierConfigManager.parsePersistentOverrideState(output, 2))
        assertNull(CarrierConfigManager.parsePersistentOverrideState("Phone Id = 0", 0))
        assertNull(CarrierConfigManager.parsePersistentOverrideState("", 0))
    }

    @Test
    fun parseDumpsysCarrierConfig_readsOverridesForEachPhone() {
        val output = """
            Phone Id = 0
            mOverrideConfigs:
              sim_country_iso_override_string = jp
              carrier_name_override_bool = true
              carrier_name_string = NTT docomo
            mPersistentOverrideConfigs:
            Phone Id = 1
            mOverrideConfigs: null
        """.trimIndent()

        val result = CarrierConfigManager.parseDumpsysCarrierConfig(output)

        assertEquals(
            mapOf(
                CarrierConfigManager.CONFIG_COUNTRY_CODE to "jp",
                CarrierConfigManager.CONFIG_CARRIER_NAME to "NTT docomo"
            ),
            result[0]
        )
        assertEquals(emptyMap<String, String>(), result[1])
    }

    @Test
    fun parseDumpsysCarrierConfig_ignoresCarrierNameWithoutOverrideFlag() {
        val output = """
            Phone Id = 0
            mOverrideConfigs:
              carrier_name_override_bool = false
              carrier_name_string = Ignored carrier
            mPersistentOverrideConfigs:
        """.trimIndent()

        val result = CarrierConfigManager.parseDumpsysCarrierConfig(output)

        assertFalse(result.getValue(0).containsKey(CarrierConfigManager.CONFIG_CARRIER_NAME))
    }

    @Test
    fun parseInstrumentationResult_readsValuesAndFinalCode() {
        val output = """
            INSTRUMENTATION_STATUS: class=com.github.nrfr.CarrierConfigInstrumentation
            INSTRUMENTATION_RESULT: result=ok
            INSTRUMENTATION_RESULT: carrierName=AT&T=Test
            INSTRUMENTATION_CODE: -1
        """.trimIndent()

        val result = CarrierConfigManager.parseInstrumentationResult(output)

        assertEquals("ok", result.values["result"])
        assertEquals("AT&T=Test", result.values["carrierName"])
        assertEquals(-1, result.code)
        assertTrue(result.isSuccessful(exitCode = 0))
        assertFalse(result.isSuccessful(exitCode = 1))
    }

    @Test
    fun instrumentationResult_rejectsMissingOrUnsuccessfulCompletion() {
        listOf(
            "",
            "INSTRUMENTATION_FAILED: Unable to find instrumentation info",
            "INSTRUMENTATION_RESULT: result=ok",
            "INSTRUMENTATION_CODE: -1",
            "INSTRUMENTATION_RESULT: result=ok\nINSTRUMENTATION_CODE: 0",
            "INSTRUMENTATION_RESULT: result=ok\nINSTRUMENTATION_CODE: 1",
            "INSTRUMENTATION_RESULT: result=failed\nINSTRUMENTATION_CODE: -1",
            "INSTRUMENTATION_RESULT: result=ok\nINSTRUMENTATION_RESULT: error=failure\nINSTRUMENTATION_CODE: -1"
        ).forEach { output ->
            assertFalse(output, CarrierConfigManager.parseInstrumentationResult(output).isSuccessful(0))
        }
    }

    @Test
    fun shellQuote_handlesWhitespaceApostrophesAndEmptyValues() {
        assertEquals("'hello world'", CarrierConfigManager.shellQuote("hello world"))
        assertEquals("'O'\\''Brien'", CarrierConfigManager.shellQuote("O'Brien"))
        assertEquals("''", CarrierConfigManager.shellQuote(""))
    }
}
