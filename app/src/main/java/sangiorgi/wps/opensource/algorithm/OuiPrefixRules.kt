package sangiorgi.wps.opensource.algorithm

/**
 * Curated table of well-known OUI prefixes (the first three bytes of a BSSID) mapped to the WPS
 * PIN weaknesses documented for that hardware family.
 *
 * The bundled OUI database resolves a vendor *name* for a MAC prefix and [VendorAlgorithmMatcher]
 * maps that name to algorithms. These rules complement that chain: they match straight off the
 * BSSID prefix with no database access, so a router whose prefix is missing from the database (or
 * whose vendor string is generic or translated) still gets its documented algorithm set surfaced.
 *
 * Every prefix below was verified against the bundled vendor.db, so a prefix rule and the
 * vendor-string rule derived from the same lookup can never contradict each other. Prefixes are
 * stored normalized (uppercase hex, no separators); lookups accept any common MAC formatting.
 */
object OuiPrefixRules {

    /**
     * One rule: a normalized 6-hex-character OUI [prefix], a human-readable [vendor] label, and
     * the algorithm [types] most likely to work on hardware carrying that prefix.
     */
    data class OuiRule(val prefix: String, val vendor: String, val types: List<AlgorithmType>)

    // Xiaomi routers (Xiaomi Communications / Xiaomi Electronics registrations).
    private val xiaomi =
        listOf(
            OuiRule("640980", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("7811DC", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("8CBEBE", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("286C07", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("F8A45F", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("4C49E3", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("508F4C", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("0C1DAF", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
            OuiRule("04CF8C", "Xiaomi", listOf(AlgorithmType.XIAOMI)),
        )

    // TP-Link routers; the Xiaomi formula is kept as a secondary candidate, mirroring the
    // vendor-string rule, because some TP-Link firmware builds share its PIN structure.
    private val tplink =
        listOf(
            OuiRule("50C7BF", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("F4F26D", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("C025E9", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("14CC20", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("EC086B", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("A42BB0", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("98DAC4", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("6032B1", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("8CA6DF", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("B04E26", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("30B5C2", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
            OuiRule("AC84C6", "TP-Link", listOf(AlgorithmType.TPLINK, AlgorithmType.XIAOMI)),
        )

    // D-Link routers; both published D-Link formulas are tried in sequence.
    private val dlink =
        listOf(
            OuiRule("00195B", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("14D64D", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("1C7EE5", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("340804", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("C8D3A3", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("B8A386", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("5CD998", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("A0AB1B", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("FC7516", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
            OuiRule("1CAFF7", "D-Link", listOf(AlgorithmType.DLINK, AlgorithmType.DLINK_PLUS_ONE)),
        )

    // ASUS routers (ASUSTek registrations).
    private val asus =
        listOf(
            OuiRule("04D4C4", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("08606E", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("14DAE9", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("1C872C", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("AC9E17", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("50465D", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("BCEE7B", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("D850E6", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("F07959", "ASUS", listOf(AlgorithmType.ASUS)),
            OuiRule("049226", "ASUS", listOf(AlgorithmType.ASUS)),
        )

    // Zyxel devices: the documented factory default is the primary candidate.
    private val zyxel =
        listOf(
            OuiRule("5CF4AB", "Zyxel", listOf(AlgorithmType.ZYXEL_DEFAULT)),
            OuiRule("B0B2DC", "Zyxel", listOf(AlgorithmType.ZYXEL_DEFAULT)),
            OuiRule("301577", "Zyxel", listOf(AlgorithmType.ZYXEL_DEFAULT)),
            OuiRule("404A03", "Zyxel", listOf(AlgorithmType.ZYXEL_DEFAULT)),
            OuiRule("1C740D", "Zyxel", listOf(AlgorithmType.ZYXEL_DEFAULT)),
            OuiRule("78C57D", "Zyxel", listOf(AlgorithmType.ZYXEL_DEFAULT)),
        )

    // Realtek reference designs: the classic source of both the Airocon formula and the
    // all-zeros empty-PIN weakness.
    private val realtek =
        listOf(
            OuiRule(
                "00E04C",
                "Realtek",
                listOf(AlgorithmType.AIROCON_REALTEK, AlgorithmType.NULL_PIN),
            ),
            OuiRule(
                "FC934E",
                "Realtek",
                listOf(AlgorithmType.AIROCON_REALTEK, AlgorithmType.NULL_PIN),
            ),
        )

    // Arcadyan hardware (also sold under EasyBox / Vodafone branding).
    private val arcadyan =
        listOf(
            OuiRule("0012BF", "Arcadyan", listOf(AlgorithmType.ARCADYAN)),
            OuiRule("001A2A", "Arcadyan", listOf(AlgorithmType.ARCADYAN)),
            OuiRule("743170", "Arcadyan", listOf(AlgorithmType.ARCADYAN)),
            OuiRule("543D60", "Arcadyan", listOf(AlgorithmType.ARCADYAN)),
            OuiRule("6045E8", "Arcadyan", listOf(AlgorithmType.ARCADYAN)),
            OuiRule("1883BF", "Arcadyan", listOf(AlgorithmType.ARCADYAN)),
        )

    // Belkin routers.
    private val belkin =
        listOf(
            OuiRule("08863B", "Belkin", listOf(AlgorithmType.BELKIN)),
            OuiRule("94103E", "Belkin", listOf(AlgorithmType.BELKIN)),
            OuiRule("EC1A59", "Belkin", listOf(AlgorithmType.BELKIN)),
            OuiRule("B4750E", "Belkin", listOf(AlgorithmType.BELKIN)),
            OuiRule("302303", "Belkin", listOf(AlgorithmType.BELKIN)),
            OuiRule("80691A", "Belkin", listOf(AlgorithmType.BELKIN)),
        )

    // TRENDnet devices: every OUI registered to the vendor.
    private val trendnet =
        listOf(
            OuiRule("0014D1", "TRENDnet", listOf(AlgorithmType.TRENDNET)),
            OuiRule("3C8CF8", "TRENDnet", listOf(AlgorithmType.TRENDNET)),
            OuiRule("782D7E", "TRENDnet", listOf(AlgorithmType.TRENDNET)),
            OuiRule("D8EB97", "TRENDnet", listOf(AlgorithmType.TRENDNET)),
        )

    // Arris and Motorola cable gateways.
    private val arris =
        listOf(
            OuiRule("342B70", "Arris", listOf(AlgorithmType.ARRIS)),
            OuiRule("24D53B", "Motorola", listOf(AlgorithmType.ARRIS)),
            OuiRule("34BB26", "Motorola", listOf(AlgorithmType.ARRIS)),
        )

    // Sagemcom broadband hardware (Orange Livebox and other ISP rebrands): the documented
    // target of the Orange algorithm. That algorithm also needs a serial number, so it is
    // surfaced whenever serial data is available rather than auto-generated from the MAC.
    private val sagemcom =
        listOf(
            OuiRule("000E59", "Sagemcom", listOf(AlgorithmType.ORANGE)),
            OuiRule("00194B", "Sagemcom", listOf(AlgorithmType.ORANGE)),
            OuiRule("001E74", "Sagemcom", listOf(AlgorithmType.ORANGE)),
            OuiRule("04E31A", "Sagemcom", listOf(AlgorithmType.ORANGE)),
            OuiRule("087B12", "Sagemcom", listOf(AlgorithmType.ORANGE)),
            OuiRule("44E9DD", "Sagemcom", listOf(AlgorithmType.ORANGE)),
        )

    // Fulltek and FiberHome ONTs, widely deployed by European and Latin American ISPs
    // (e.g. Jazztel): the FTE formula is literally named after Fulltek.
    private val fiberhome =
        listOf(
            OuiRule("0001B0", "Fulltek", listOf(AlgorithmType.FTE)),
            OuiRule("000AC2", "FiberHome", listOf(AlgorithmType.FTE)),
            OuiRule("002FD9", "FiberHome", listOf(AlgorithmType.FTE)),
            OuiRule("04A2F3", "FiberHome", listOf(AlgorithmType.FTE)),
            OuiRule("087458", "FiberHome", listOf(AlgorithmType.FTE)),
            OuiRule("0C6ABC", "FiberHome", listOf(AlgorithmType.FTE)),
        )

    /** The full rule group, ordered by vendor family. */
    val rules: List<OuiRule> = xiaomi + tplink + dlink + asus + zyxel + realtek + arcadyan +
        belkin + trendnet + arris + sagemcom + fiberhome

    /**
     * Returns the algorithms implied by the BSSID's OUI prefix, or an empty list when the prefix
     * is malformed or not covered by the table.
     */
    fun match(bssid: String?): List<AlgorithmType> {
        val prefix = normalizePrefix(bssid) ?: return emptyList()
        return rules.firstOrNull { it.prefix == prefix }?.types ?: emptyList()
    }

    /**
     * Normalizes any common MAC formatting ("F8:3D:FF...", "f8-3d-ff...", already-flat hex) to
     * the 6-character uppercase prefix used by the rule table. Returns null for blank input,
     * input shorter than the prefix, or input that is not hexadecimal.
     */
    fun normalizePrefix(bssid: String?): String? {
        if (bssid.isNullOrBlank()) return null
        val hex = bssid.uppercase().replace(":", "").replace("-", "")
        if (hex.length < 6) return null
        val prefix = hex.substring(0, 6)
        return if (prefix.all { it in '0'..'9' || it in 'A'..'F' }) prefix else null
    }
}
