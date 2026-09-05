package sangiorgi.wps.opensource.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the curated OUI-prefix rule table that maps well-known BSSID prefixes directly to
 * their documented WPS PIN algorithms, with no vendor-database access.
 */
class OuiPrefixRulesTest {

    @Test
    fun tpLinkPrefixMapsToTpLinkAlgorithms() {
        assertEquals(
            listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI),
            OuiPrefixRules.match("50:C7:BF:12:34:56"),
        )
    }

    @Test
    fun xiaomiPrefixMapsToXiaomiAlgorithm() {
        assertEquals(
            listOf(AlgorithmType.XIAOMI),
            OuiPrefixRules.match("64:09:80:AA:BB:CC"),
        )
    }

    @Test
    fun realtekPrefixMapsToAiroconAndNullPin() {
        assertEquals(
            listOf(AlgorithmType.AIROCON_REALTEK, AlgorithmType.NULL_PIN),
            OuiPrefixRules.match("00:E0:4C:11:22:33"),
        )
    }

    @Test
    fun fiberhomePrefixMapsToFte() {
        assertEquals(listOf(AlgorithmType.FTE), OuiPrefixRules.match("00:0A:C2:00:00:01"))
    }

    @Test
    fun dlinkPrefixMapsToBothDlinkVariants() {
        assertEquals(
            listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE),
            OuiPrefixRules.match("14:D6:4D:99:88:77"),
        )
    }

    @Test
    fun sagemcomPrefixMapsToOrange() {
        assertEquals(listOf(AlgorithmType.ORANGE), OuiPrefixRules.match("00:0E:59:44:33:22"))
    }

    @Test
    fun separatorsAndCaseAreNormalized() {
        assertEquals(
            listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI),
            OuiPrefixRules.match("f4-f2-6d-aa-bb-cc"),
        )
        assertEquals(
            listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI),
            OuiPrefixRules.match("f4f26daabbcc"),
        )
    }

    @Test
    fun flatVendorDatabaseFormatIsAccepted() {
        assertEquals(
            listOf(AlgorithmType.XIAOMI),
            OuiPrefixRules.match("7811DC001122"),
        )
    }

    @Test
    fun unknownPrefixReturnsEmptyList() {
        assertTrue(OuiPrefixRules.match("00:11:22:33:44:55").isEmpty())
        assertTrue(OuiPrefixRules.match("FFFFFF:11:22:33").isEmpty())
    }

    @Test
    fun malformedInputReturnsEmptyList() {
        assertTrue(OuiPrefixRules.match(null).isEmpty())
        assertTrue(OuiPrefixRules.match("").isEmpty())
        assertTrue(OuiPrefixRules.match("   ").isEmpty())
        assertTrue(OuiPrefixRules.match("abc").isEmpty())
        assertTrue(OuiPrefixRules.match("12345").isEmpty())
    }

    @Test
    fun nonHexadecimalPrefixReturnsEmptyList() {
        assertTrue(OuiPrefixRules.match("ZZ:11:22:33:44:55").isEmpty())
    }

    @Test
    fun everyRulePrefixIsWellFormedAndUnique() {
        val prefixes = OuiPrefixRules.rules.map { it.prefix }
        assertEquals(prefixes.size, prefixes.distinct().size)
        for (rule in OuiPrefixRules.rules) {
            assertEquals("prefix must be 6 chars: ${rule.prefix}", 6, rule.prefix.length)
            assertTrue(
                "prefix must be uppercase hex: ${rule.prefix}",
                rule.prefix.all { it in '0'..'9' || it in 'A'..'F' },
            )
            assertTrue("rule must imply algorithms: ${rule.prefix}", rule.types.isNotEmpty())
        }
    }

    @Test
    fun normalizePrefixMatchesDatabaseLookupFormat() {
        assertEquals("50C7BF", OuiPrefixRules.normalizePrefix("50:C7:BF:12:34:56"))
        assertEquals("50C7BF", OuiPrefixRules.normalizePrefix("50c7bf"))
        assertEquals("50C7BF", OuiPrefixRules.normalizePrefix("50-C7-BF-12-34-56"))
    }

    @Test
    fun normalizePrefixRejectsMalformedInput() {
        assertNull(OuiPrefixRules.normalizePrefix(null))
        assertNull(OuiPrefixRules.normalizePrefix("12345"))
        assertNull(OuiPrefixRules.normalizePrefix("nothex!"))
    }

    @Test
    fun matchedRuleExposesVendorLabelForKnownPrefix() {
        val rule = OuiPrefixRules.matchedRule("50:C7:BF:12:34:56")
        assertEquals("TP-Link", rule?.vendor)
        assertEquals(
            listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI),
            rule?.types,
        )
    }

    @Test
    fun matchedRuleReturnsNullForUnknownOrMalformedPrefix() {
        assertNull(OuiPrefixRules.matchedRule("00:11:22:33:44:55"))
        assertNull(OuiPrefixRules.matchedRule(null))
        assertNull(OuiPrefixRules.matchedRule("12345"))
    }

    @Test
    fun formatPrefixSeparatesBytePairs() {
        assertEquals("50:C7:BF", OuiPrefixRules.formatPrefix("50C7BF"))
        assertEquals("50C7B", OuiPrefixRules.formatPrefix("50C7B"))
    }
}
