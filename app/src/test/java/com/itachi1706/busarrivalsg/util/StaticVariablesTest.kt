package com.itachi1706.busarrivalsg.util

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StaticVariablesTest {

    @Test
    fun testCheckIfYouGotJsonString() {
        assertTrue(StaticVariables.checkIfYouGotJsonString("{\"key\":\"value\"}"))
        assertFalse(StaticVariables.checkIfYouGotJsonString("not a json string"))
    }

    @Test
    fun testUseServerTime() {
        val sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        `when`(sharedPreferences.getBoolean("useServerTime", false)).thenReturn(true)
        assertTrue(StaticVariables.useServerTime(sharedPreferences))
    }

    @Test
    fun testParseLTAEstimateArrival() {
        assertEquals(-9999, StaticVariables.parseLTAEstimateArrival("", false, null))

        // Add more assertions for different inputs...
        val serverTime = "2024-06-26T17:24:08+08:00"
        assertEquals(-1, StaticVariables.parseLTAEstimateArrival("2024-06-26T17:23:01+08:00", true, serverTime))
        assertEquals(4, StaticVariables.parseLTAEstimateArrival("2024-06-26T17:29:01+08:00", true, serverTime))
        assertEquals(0, StaticVariables.parseLTAEstimateArrival("2024-06-26T17:24:01+08:00", true, serverTime))
    }

    @Test
    fun testCheckBusLocationValid() {
        assertTrue(StaticVariables.checkBusLocationValid(1.0, 1.0))
        assertFalse(StaticVariables.checkBusLocationValid(-1000.0, -1000.0))
    }

    @Test
    fun testCheckIfCoraseLocationGranted() {
        val result = mapOf("android.permission.ACCESS_COARSE_LOCATION" to true)
        assertTrue(StaticVariables.checkIfCoraseLocationGranted(result))

        val result2 = mapOf("android.permission.ACCESS_COARSE_LOCATION" to false)
        assertFalse(StaticVariables.checkIfCoraseLocationGranted(result2))
    }
}