package sangiorgi.wps.opensource.ui.screens

import org.junit.Assert.assertTrue
import org.junit.Test
import sangiorgi.wps.opensource.domain.models.WifiNetwork
import sangiorgi.wps.opensource.domain.models.WpsInfo

class ScanReportTest {

    private val wpsNetwork = WifiNetwork(
        bssid = "AA:BB:CC:DD:EE:FF",
        ssid = "HomeNet",
        signalLevel = -48,
        frequency = 2437,
        capabilities = "[WPA2-PSK-CCMP][WPS]",
        vendor = "TP-Link",
    )

    private val openNetwork = WifiNetwork(
        bssid = "11:22:33:44:55:66",
        ssid = "CafeGuest",
        signalLevel = -70,
        frequency = 5180,
        capabilities = "[ESS]",
        vendor = "Unknown",
    )

    @Test
    fun `report lists every network with its key fields`() {
        val report = buildScanReport("Wi-Fi scan report", listOf(wpsNetwork, openNetwork))

        assertTrue(report.startsWith("Wi-Fi scan report"))
        assertTrue(report.contains("Networks: 2 \u00b7 WPS: 1"))
        assertTrue(
            report.contains("HomeNet | AA:BB:CC:DD:EE:FF | WPA_WPA2 | WPS:yes | -48dBm | ch6 | TP-Link"),
        )
        assertTrue(
            report.contains("CafeGuest | 11:22:33:44:55:66 | OPEN | WPS:no | -70dBm | ch36 | Unknown"),
        )
    }

    @Test
    fun `locked WPS networks are reported as locked`() {
        val locked = wpsNetwork.copy(wpsInfo = WpsInfo(isEnabled = true, isLocked = true))

        val report = buildScanReport("t", listOf(locked))

        assertTrue(report.contains("WPS:locked"))
    }

    @Test
    fun `empty scans still produce a valid header`() {
        val report = buildScanReport("t", emptyList())

        assertTrue(report.contains("Networks: 0"))
    }
}
