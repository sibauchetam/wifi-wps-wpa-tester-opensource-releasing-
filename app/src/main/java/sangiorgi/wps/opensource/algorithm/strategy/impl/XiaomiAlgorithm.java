package sangiorgi.wps.opensource.algorithm.strategy.impl;

import sangiorgi.wps.opensource.algorithm.MacAddressUtil;
import sangiorgi.wps.opensource.algorithm.strategy.AlgorithmException;
import sangiorgi.wps.opensource.algorithm.strategy.BaseWpsAlgorithm;

/**
 * Xiaomi router WPS PIN algorithm.
 *
 * <p>The PIN is derived from the last four bytes of the BSSID: the bytes are read as a single
 * hexadecimal number, reduced modulo 10^7 to seven decimal digits, and the standard WPS checksum
 * digit is appended. This makes the PIN fully computable from the MAC address alone, so affected
 * routers leak their PIN the moment they broadcast their BSSID.
 */
public class XiaomiAlgorithm extends BaseWpsAlgorithm {

  public XiaomiAlgorithm() {
    super("Xiaomi");
  }

  @Override
  public String generatePin(String bssid, String ssid) throws AlgorithmException {
    String normalized = MacAddressUtil.normalize(bssid);

    if (normalized.length() < 12) {
      throw new AlgorithmException("BSSID must contain 12 hex digits");
    }

    // Last four bytes (hex positions 4..11) as an integer, folded into 7 decimal digits.
    long tail = parseLongHexSafe(normalized.substring(4, 12), 0L);
    int pin = (int) (tail % PIN_MODULO);

    return formatPinWithChecksum(pin);
  }
}
