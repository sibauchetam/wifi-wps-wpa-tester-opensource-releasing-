package sangiorgi.wps.opensource.algorithm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sangiorgi.wps.opensource.algorithm.strategy.AlgorithmFactory
import sangiorgi.wps.opensource.data.database.PinDatabaseHelper
import sangiorgi.wps.opensource.data.database.VendorDatabaseHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service that generates WPS PINs by combining:
 * 1. Static/default PINs from pin.db (based on MAC prefix)
 * 2. Documented community default PINs (e.g. the famous Zyxel factory PIN)
 * 3. Algorithm-generated PINs, with the ones matching the router's vendor first
 *
 * This provides a comprehensive list of PINs to try for a given network, ordered so the most
 * promising candidates come first.
 */
@Singleton
class PinGeneratorService @Inject constructor(
    private val algorithmFactory: AlgorithmFactory,
    private val pinDatabaseHelper: PinDatabaseHelper,
    private val vendorDatabaseHelper: VendorDatabaseHelper,
) {
    private val algorithm: Algorithm by lazy { Algorithm.from(algorithmFactory) }

    /**
     * Data class representing a PIN with its source.
     */
    data class PinWithSource(
        val pin: String,
        val source: String,
        val isFromDatabase: Boolean = false,
        val isRecommended: Boolean = false,
    )

    /**
     * A documented static factory PIN, returned verbatim (no checksum recomputation).
     */
    private data class KnownDefault(
        val type: AlgorithmType,
        val pin: String,
        val source: String,
    )

    /**
     * Generate all suggested PINs for a network.
     * Combines database PINs (from MAC prefix lookup) with documented default PINs and
     * algorithm-generated PINs. Algorithms whose vendor matches the router (resolved from the
     * OUI database) are generated first and flagged as recommended, so the strongest candidates
     * sit at the top of the list.
     *
     * @param bssid The router's BSSID (MAC address)
     * @param ssid The router's SSID (network name)
     * @return List of PINs with their sources, deduplicated, best candidates first
     */
    suspend fun generateAllPins(bssid: String, ssid: String?): List<PinWithSource> = withContext(Dispatchers.IO) {
        val pins = mutableListOf<PinWithSource>()
        val seenPins = mutableSetOf<String>()

        // 1. First, get static PINs from database (these are known defaults for this vendor)
        val databasePins = pinDatabaseHelper.getPinsByMac(bssid)
        for (pin in databasePins) {
            if (pin !in seenPins) {
                seenPins.add(pin)
                pins.add(
                    PinWithSource(
                        pin = pin,
                        source = "Database (vendor default)",
                        isFromDatabase = true,
                        isRecommended = true,
                    ),
                )
            }
        }

        // 2. Resolve the vendor from the OUI database and derive the priority algorithm set.
        //    The matcher also consults the built-in OUI prefix rules, so a BSSID whose prefix is
        //    missing from the database still resolves to its documented algorithm family.
        val vendor = vendorDatabaseHelper.getVendorByMac(bssid)
        val priorityTypes = VendorAlgorithmMatcher.matchedTypes(vendor, bssid).toSet()

        // 3. Documented community defaults, right after the vendor-specific hits. The Zyxel
        //    factory PIN is flagged recommended when the router actually is a Zyxel.
        for (knownDefault in KNOWN_DEFAULTS) {
            if (knownDefault.pin !in seenPins) {
                seenPins.add(knownDefault.pin)
                pins.add(
                    PinWithSource(
                        pin = knownDefault.pin,
                        source = knownDefault.source,
                        isRecommended = knownDefault.type in priorityTypes,
                    ),
                )
            }
        }

        // 4. Algorithm-generated PINs: vendor-matched algorithms first (recommended), then the
        //    rest of the auto-suggested set in its default order.
        val orderedTypes = Algorithm.AUTO_SUGGESTED_ALGORITHMS
            .sortedByDescending { it in priorityTypes }

        for (type in orderedTypes) {
            val result = algorithm.generatePin(type, bssid, ssid)
            if (result is AlgorithmResult.Success && result.pin !in seenPins) {
                seenPins.add(result.pin)
                pins.add(
                    PinWithSource(
                        pin = result.pin,
                        source = result.algorithmName,
                        isRecommended = type in priorityTypes,
                    ),
                )
            }
        }

        // 5. Add fallback default PIN if not already present
        if (DEFAULT_PIN !in seenPins) {
            pins.add(PinWithSource(DEFAULT_PIN, "Default", isFromDatabase = false))
        }

        pins
    }

    companion object {
        private const val DEFAULT_PIN = "12345670"

        private val KNOWN_DEFAULTS =
            listOf(
                KnownDefault(AlgorithmType.ZYXEL_DEFAULT, "22222480", "Zyxel default PIN"),
                KnownDefault(AlgorithmType.COMMON_DEFAULT, "11111110", "Common default PIN"),
            )
    }
}
