package sangiorgi.wps.opensource.algorithm

/**
 * Maps a router to the WPS PIN algorithms most likely to work for it, in priority order.
 *
 * Two complementary sources are consulted:
 * 1. [OuiPrefixRules] — a curated BSSID-prefix table that works with no database access.
 * 2. Keyword rules over the vendor name resolved from the bundled OUI database.
 *
 * This is a heuristic guide, not a guarantee: it encodes which published PIN-generation weaknesses
 * are historically associated with which vendor, so the PIN dialog can surface the most promising
 * candidates first instead of a flat list ordered by algorithm id.
 */
object VendorAlgorithmMatcher {

    /**
     * A vendor matching rule: when any [keywords] appears in the vendor string
     * (case-insensitive), the listed [types] are considered the top candidates for that router.
     */
    private data class Rule(val keywords: List<String>, val types: List<AlgorithmType>)

    // Rules are evaluated in order; the first matching rule wins. Keep vendor-specific
    // formulas before broad keyword rules so "TP-Link" resolves to TP-LINK first.
    private val rules =
        listOf(
            Rule(listOf("xiaomi"), listOf(AlgorithmType.XIAOMI)),
            Rule(listOf("tp-link", "tplink"), listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            Rule(listOf("d-link", "dlink"), listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            Rule(listOf("asus", "asustek"), listOf(AlgorithmType.ASUS)),
            Rule(listOf("trendnet"), listOf(AlgorithmType.TRENDNET)),
            Rule(listOf("arris", "motorola"), listOf(AlgorithmType.ARRIS)),
            Rule(listOf("arcadyan", "easybox", "vodafone"), listOf(AlgorithmType.ARCADYAN)),
            Rule(listOf("belkin"), listOf(AlgorithmType.BELKIN)),
            // FTE is named after Fulltek; FiberHome ONTs (e.g. Jazztel deployments) share it.
            Rule(
                listOf("fte", "fulltek", "fiberhome", "jazztel"),
                listOf(AlgorithmType.FTE),
            ),
            // Sagemcom/Orange Livebox hardware is the documented target of the Orange formula.
            Rule(
                listOf("sagemcom", "orange", "livebox"),
                listOf(AlgorithmType.ORANGE),
            ),
            Rule(listOf("zyxel"), listOf(AlgorithmType.ZYXEL_DEFAULT)),
            // Realtek-based firmwares are the classic source of both the Airocon formula
            // and the all-zeros empty-PIN weakness.
            Rule(listOf("realtek", "airocon"), listOf(AlgorithmType.AIROCON_REALTEK, AlgorithmType.NULL_PIN)),
        )

    /**
     * Returns the algorithms associated with a router, most relevant first, combining the
     * [OuiPrefixRules] table (matched directly on [bssid]) with the vendor-string keyword rules.
     * Returns an empty list when neither source has a documented algorithm association.
     */
    fun matchedTypes(vendor: String?, bssid: String? = null): List<AlgorithmType> =
        (OuiPrefixRules.match(bssid) + matchedVendorTypes(vendor)).distinct()

    /**
     * Returns the algorithms associated with [vendor], most relevant first. Returns an empty list
     * when the vendor is unknown, blank, or has no documented algorithm association.
     */
    private fun matchedVendorTypes(vendor: String?): List<AlgorithmType> {
        if (vendor.isNullOrBlank()) return emptyList()

        val normalized = vendor.lowercase()
        val rule = rules.firstOrNull { rule -> rule.keywords.any { normalized.contains(it) } }
        return rule?.types ?: emptyList()
    }
}
