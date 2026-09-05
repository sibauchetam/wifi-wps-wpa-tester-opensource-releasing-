package sangiorgi.wps.opensource.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sangiorgi.wps.opensource.algorithm.strategy.AlgorithmFactory

/**
 * Invariant tests for the WPS PIN algorithms. Rather than pin down vendor-specific reference
 * vectors, these assert the properties every generated PIN must hold: it is an 8-digit string,
 * its last digit is a valid WPS checksum of the first seven, and generation is deterministic.
 */
class AlgorithmInvariantsTest {

    private val factory = AlgorithmFactory("build/tmp/algo-test/")
    private val algorithm = Algorithm.from(factory)

    private val sampleBssid = "00:11:22:33:44:55"
    private val sampleSsid = "TestNetwork"

    private fun isValidWpsPin(pin: String): Boolean {
        if (!Regex("\\d{8}").matches(pin)) return false
        val firstSeven = pin.substring(0, 7).toInt()
        val checksum = pin.substring(7).toInt()
        return ChecksumCalculator.calculatePreMultiplied(firstSeven) == checksum
    }

    @Test
    fun suggestedPinsAreWellFormedAndChecksumValid() {
        val results = algorithm.generateUniqueSuggestedPins(sampleBssid, sampleSsid)

        assertTrue("Expected at least one suggested PIN for a normal MAC", results.isNotEmpty())
        results.forEach { result ->
            assertTrue(
                "PIN '${result.pin}' from ${result.algorithmName} must be a valid 8-digit WPS PIN",
                isValidWpsPin(result.pin),
            )
        }
    }

    @Test
    fun suggestedPinsAreUnique() {
        val pins = algorithm.generateUniqueSuggestedPins(sampleBssid, sampleSsid).map { it.pin }
        assertEquals("generateUniqueSuggestedPins must not return duplicates", pins.size, pins.toSet().size)
    }

    @Test
    fun generationIsDeterministic() {
        val first = algorithm.generateUniqueSuggestedPins(sampleBssid, sampleSsid).map { it.pin }
        val second = algorithm.generateUniqueSuggestedPins(sampleBssid, sampleSsid).map { it.pin }
        assertEquals(first, second)
    }

    @Test
    fun fileFreeAlgorithmsProduceValidChecksums() {
        // Algorithms that derive the PIN purely from the MAC (no serial/session file needed).
        val fileFreeTypes = listOf(
            AlgorithmType.PIN,
            AlgorithmType.TWENTY_EIGHT_BIT,
            AlgorithmType.THIRTY_TWO_BIT,
            AlgorithmType.THIRTY_SIX_BIT,
            AlgorithmType.FORTY_BIT,
            AlgorithmType.FORTY_FOUR_BIT,
            AlgorithmType.FORTY_EIGHT_BIT,
            AlgorithmType.DLINK,
            AlgorithmType.DLINK_PLUS_ONE,
            AlgorithmType.TRENDNET,
            AlgorithmType.ARRIS,
            AlgorithmType.ASUS,
            AlgorithmType.AIROCON_REALTEK,
            AlgorithmType.ARCADYAN,
            AlgorithmType.XIAOMI,
            AlgorithmType.TPLINK,
            AlgorithmType.NULL_PIN,
        )

        fileFreeTypes.forEach { type ->
            val result = algorithm.generatePin(type, sampleBssid, sampleSsid)
            assertTrue(
                "$type should generate a PIN for a normal MAC, got: $result",
                result is AlgorithmResult.Success,
            )
            val pin = (result as AlgorithmResult.Success).pin
            assertTrue("$type produced an invalid WPS PIN: $pin", isValidWpsPin(pin))
        }
    }

    @Test
    fun nullPinIsAllZeros() {
        val result = algorithm.generatePin(AlgorithmType.NULL_PIN, sampleBssid, sampleSsid)
        assertEquals("00000000", (result as AlgorithmResult.Success).pin)
    }

    @Test
    fun xiaomiPinMatchesFormula() {
        // Xiaomi: last four bytes of the MAC as hex, reduced mod 10^7, plus checksum.
        val tail = sampleBssid.replace(":", "").substring(4, 12).toLong(16)
        val sevenDigits = (tail % 10_000_000L).toInt()
        val expected = String.format("%07d%d", sevenDigits, ChecksumCalculator.calculatePreMultiplied(sevenDigits))
        val result = algorithm.generatePin(AlgorithmType.XIAOMI, sampleBssid, sampleSsid)
        assertEquals(expected, (result as AlgorithmResult.Success).pin)
    }

    @Test
    fun tplinkPinMatchesFormula() {
        // TP-LINK uses the same published formula as Xiaomi: last four MAC bytes as hex,
        // mod 10^7, plus checksum.
        val tail = sampleBssid.replace(":", "").substring(4, 12).toLong(16)
        val sevenDigits = (tail % 10_000_000L).toInt()
        val expected = String.format("%07d%d", sevenDigits, ChecksumCalculator.calculatePreMultiplied(sevenDigits))
        val result = algorithm.generatePin(AlgorithmType.TPLINK, sampleBssid, sampleSsid)
        assertEquals(expected, (result as AlgorithmResult.Success).pin)
    }

    @Test
    fun tplinkPinEqualsXiaomiPin() {
        // Same underlying formula - the two entries must agree so deduplication works.
        val xiaomi = (
            algorithm.generatePin(
                AlgorithmType.XIAOMI,
                sampleBssid,
                sampleSsid,
            ) as AlgorithmResult.Success
            ).pin
        val tplink = (
            algorithm.generatePin(
                AlgorithmType.TPLINK,
                sampleBssid,
                sampleSsid,
            ) as AlgorithmResult.Success
            ).pin
        assertEquals(xiaomi, tplink)
    }

    @Test
    fun staticDefaultsAreReturnedVerbatim() {
        // Famous factory defaults are documented WITHOUT a recomputed checksum digit -
        // most APs do not validate it, so the PIN must be returned exactly as published.
        val zyxel = algorithm.generatePin(AlgorithmType.ZYXEL_DEFAULT, sampleBssid, sampleSsid)
        assertEquals("22222480", (zyxel as AlgorithmResult.Success).pin)

        val common = algorithm.generatePin(AlgorithmType.COMMON_DEFAULT, sampleBssid, sampleSsid)
        assertEquals("11111110", (common as AlgorithmResult.Success).pin)
    }

    @Test
    fun staticDefaultsAreNotAutoSuggested() {
        // Static defaults do not satisfy the checksum invariant, so they must stay out of the
        // auto-suggested set (which is covered by suggestedPinsAreWellFormedAndChecksumValid).
        val autoSuggested = Algorithm.AUTO_SUGGESTED_ALGORITHMS.toList()
        assertTrue(!autoSuggested.contains(AlgorithmType.ZYXEL_DEFAULT))
        assertTrue(!autoSuggested.contains(AlgorithmType.COMMON_DEFAULT))
    }
}
