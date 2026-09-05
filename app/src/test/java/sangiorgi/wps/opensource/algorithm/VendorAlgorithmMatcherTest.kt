package sangiorgi.wps.opensource.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the vendor-to-algorithm priority mapping used to surface the most promising
 * PIN candidates first for a detected router vendor.
 */
class VendorAlgorithmMatcherTest {

    @Test
    fun xiaomiVendorMapsToXiaomiAlgorithm() {
        val matched = VendorAlgorithmMatcher.matchedTypes("Xiaomi Communications Co Ltd")
        assertEquals(listOf(AlgorithmType.XIAOMI), matched)
    }

    @Test
    fun tpLinkVendorMapsToTpLinkFirst() {
        val matched = VendorAlgorithmMatcher.matchedTypes("TP-LINK TECHNOLOGIES CO.,LTD.")
        assertEquals(AlgorithmType.TPLINK, matched.first())
        assertTrue(matched.contains(AlgorithmType.XIAOMI))
    }

    @Test
    fun dLinkVendorMapsToBothDlinkVariants() {
        val matched = VendorAlgorithmMatcher.matchedTypes("D-Link Systems Inc.")
        assertEquals(listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE), matched)
    }

    @Test
    fun asusVendorMapsToAsusAlgorithm() {
        val matched = VendorAlgorithmMatcher.matchedTypes("ASUSTek COMPUTER INC.")
        assertEquals(listOf(AlgorithmType.ASUS), matched)
    }

    @Test
    fun realtekVendorMapsToAiroconAndNullPin() {
        val matched = VendorAlgorithmMatcher.matchedTypes("Realtek Semiconductor Corp.")
        assertEquals(listOf(AlgorithmType.AIROCON_REALTEK, AlgorithmType.NULL_PIN), matched)
    }

    @Test
    fun zyxelVendorMapsToFactoryDefault() {
        val matched = VendorAlgorithmMatcher.matchedTypes("Zyxel Communications Corporation")
        assertEquals(listOf(AlgorithmType.ZYXEL_DEFAULT), matched)
    }

    @Test
    fun arcadyanVariantsMapToEasyBox() {
        assertEquals(
            listOf(AlgorithmType.ARCADYAN),
            VendorAlgorithmMatcher.matchedTypes("Arcadyan Technology Corporation"),
        )
        assertEquals(
            listOf(AlgorithmType.ARCADYAN),
            VendorAlgorithmMatcher.matchedTypes("Vodafone Germany"),
        )
    }

    @Test
    fun matchingIsCaseInsensitive() {
        val matched = VendorAlgorithmMatcher.matchedTypes("BELKIN INTERNATIONAL, INC.")
        assertEquals(listOf(AlgorithmType.BELKIN), matched)
    }

    @Test
    fun unknownVendorsReturnEmptyList() {
        assertTrue(VendorAlgorithmMatcher.matchedTypes("Samsung Electronics").isEmpty())
        assertTrue(VendorAlgorithmMatcher.matchedTypes("Unknown").isEmpty())
        assertTrue(VendorAlgorithmMatcher.matchedTypes(null).isEmpty())
        assertTrue(VendorAlgorithmMatcher.matchedTypes("").isEmpty())
        assertTrue(VendorAlgorithmMatcher.matchedTypes("   ").isEmpty())
    }
}
