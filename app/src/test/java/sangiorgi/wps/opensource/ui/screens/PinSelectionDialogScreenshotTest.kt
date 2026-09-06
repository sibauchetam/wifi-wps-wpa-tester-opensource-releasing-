package sangiorgi.wps.opensource.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sangiorgi.wps.opensource.ui.theme.WIFIWPSWPATESTEROPENSOURCETheme

/**
 * Renders the exact production PIN picker body with fake data and saves PNGs
 * under build/roborazzi so the dialog design can be reviewed pixel by pixel
 * without an emulator.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h915dp-420dpi", application = Application::class)
class PinSelectionDialogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun algorithmModeLooksRight() {
        render(
            state = PinSelectionUiState(
                ssid = "TP-Link_5GA8",
                pins = fakePins,
                selectedPins = fakePins.map { it.pin },
            ),
        )
        capture("pin_selection_algorithm_mode.png")
    }

    @Test
    fun shortListLooksRight() {
        val shortList = fakePins.take(3)
        render(
            state = PinSelectionUiState(
                ssid = "Belkin_2G",
                pins = shortList,
                selectedPins = shortList.map { it.pin },
            ),
        )
        capture("pin_selection_short_list.png")
    }

    @Test
    fun customModeWithChecksumLooksRight() {
        render(
            state = PinSelectionUiState(
                ssid = "TP-Link_5GA8",
                pins = fakePins,
                selectedPins = fakePins.map { it.pin },
                showCustomInput = true,
                customPin = "2084589",
            ),
        )
        capture("pin_selection_custom_mode.png")
    }

    private fun render(state: PinSelectionUiState) {
        composeRule.setContent {
            WIFIWPSWPATESTEROPENSOURCETheme(darkTheme = true) {
                // Faithful host for the production content: the dialog body lives on the
                // surfaceContainerHigh container of the official Material 3 AlertDialog,
                // which supplies the extraLarge shape, headline, paddings and buttons.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    PinSelectionContent(
                        state = state,
                        onModeChange = { },
                        onCustomPinChange = { },
                        onSelectionChange = { },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun capture(name: String) {
        composeRule.onRoot().captureRoboImage("build/roborazzi/$name")
    }
}

private val fakePins = listOf(
    PinOption(pin = "20845890", description = "TP-Link algorithm", isRecommended = true),
    PinOption(pin = "70242964", description = "Belkin algorithm"),
    PinOption(pin = "12345670", description = "Database (vendor default)", isFromDatabase = true),
    PinOption(pin = "91607714", description = "D-Link algorithm"),
    PinOption(pin = "48356217", description = "ASUS algorithm"),
    PinOption(pin = "03462871", description = "Zyxel algorithm"),
    PinOption(pin = "69066428", description = "Realtek algorithm"),
    PinOption(pin = "15495723", description = "Arcadyan algorithm"),
    PinOption(pin = "87422659", description = "TRENDnet algorithm"),
    PinOption(pin = "30552684", description = "Sagemcom algorithm"),
    PinOption(pin = "24960713", description = "Arris/Motorola algorithm"),
    PinOption(pin = "56703892", description = "FTE algorithm"),
)
