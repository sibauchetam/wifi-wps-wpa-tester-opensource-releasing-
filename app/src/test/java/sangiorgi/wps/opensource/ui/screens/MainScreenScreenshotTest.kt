package sangiorgi.wps.opensource.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sangiorgi.wps.opensource.domain.models.WifiNetwork
import sangiorgi.wps.opensource.domain.models.WpsInfo
import sangiorgi.wps.opensource.ui.components.NetworkListSkeleton
import sangiorgi.wps.opensource.ui.theme.GroupedListDefaults
import sangiorgi.wps.opensource.ui.theme.WIFIWPSWPATESTEROPENSOURCETheme

/**
 * Renders the production main-screen building blocks (grouped network rows,
 * skeleton, empty state) and saves PNGs under build/roborazzi so the spacing
 * and rhythm can be reviewed pixel by pixel without an emulator.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h915dp-420dpi", application = Application::class)
class MainScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun groupedNetworkRowsLookRight() {
        render {
            Column(modifier = Modifier.fillMaxWidth()) {
                fakeNetworks.forEachIndexed { index, network ->
                    NetworkCard(
                        network = network,
                        onClick = { },
                        shape = when {
                            fakeNetworks.size == 1 -> GroupedListDefaults.single
                            index == 0 -> GroupedListDefaults.top
                            index == fakeNetworks.lastIndex -> GroupedListDefaults.bottom
                            else -> GroupedListDefaults.middle
                        },
                        showDivider = index != fakeNetworks.lastIndex,
                    )
                }
            }
        }
        capture("main_grouped_rows.png")
    }

    @Test
    fun skeletonLooksRight() {
        render {
            NetworkListSkeleton()
        }
        capture("main_skeleton.png")
    }

    @Test
    fun emptyStateLooksRight() {
        render {
            EmptyState(onScan = { })
        }
        capture("main_empty_state.png")
    }

    private fun render(content: @androidx.compose.runtime.Composable () -> Unit) {
        composeRule.setContent {
            WIFIWPSWPATESTEROPENSOURCETheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    content()
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun capture(name: String) {
        composeRule.onRoot().captureRoboImage("build/roborazzi/$name")
    }
}

private val fakeNetworks = listOf(
    WifiNetwork(
        bssid = "AA:BB:CC:DD:EE:FF",
        ssid = "TP-Link_5GA8",
        signalLevel = -48,
        frequency = 2437,
        capabilities = "[WPA2-PSK-CCMP][WPS]",
        vendor = "TP-Link",
        wpsInfo = WpsInfo(isEnabled = true, isPinSupported = true),
    ),
    WifiNetwork(
        bssid = "11:22:33:44:55:66",
        ssid = "HomeNet",
        signalLevel = -62,
        frequency = 5180,
        capabilities = "[WPA2-PSK-CCMP][WPS]",
        vendor = "ASUS",
        wpsInfo = WpsInfo(isEnabled = true, isLocked = true),
    ),
    WifiNetwork(
        bssid = "77:88:99:AA:BB:CC",
        ssid = "CafeGuest",
        signalLevel = -71,
        frequency = 5240,
        capabilities = "[ESS]",
        vendor = "Unknown",
    ),
)
