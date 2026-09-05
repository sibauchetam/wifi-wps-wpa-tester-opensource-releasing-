package sangiorgi.wps.opensource.algorithm.strategy.impl;

import sangiorgi.wps.opensource.algorithm.MacAddressUtil;
import sangiorgi.wps.opensource.algorithm.strategy.AlgorithmException;
import sangiorgi.wps.opensource.algorithm.strategy.BaseWpsAlgorithm;

/**
 * TP-LINK router WPS PIN algorithm.
 *
 * <p>The classic TP-Link WPS weakness published by Craig Heffner in 2012: the PIN is derived from
 * the last four bytes of the BSSID, read as a single hexadecimal number, reduced modulo 10^7 to
 * seven decimal digits, with the standard WPS checksum digit appended. The same formula is also
 * used by Xiaomi routers, which is why both entries appear in the PIN dialog; deduplication keeps
 * only one copy of the resulting PIN.
 */
public class TplinkAlgorithm extends BaseWpsAlgorithm {

  public TplinkAlgorithm() {
    super("TP-LINK");
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
