package com.itachi1706.busarrivalsg.util

import android.content.Context
import android.content.Intent
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ShortcutHelperTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var intent: Intent

    private lateinit var shortcutHelper: ShortcutHelper

    @Before
    fun setUp() {
        shortcutHelper = ShortcutHelper(context)
    }

    @Test
    fun `updateBusStopShortcuts returns false when SDK version is less than Nougat`() {
        val busStop = BusStopJSON()
        busStop.BusStopCode = "12345"
        busStop.Description = "Test Bus Stop"

        val result = shortcutHelper.updateBusStopShortcuts(busStop, intent)

        assertEquals(false, result)
    }
}