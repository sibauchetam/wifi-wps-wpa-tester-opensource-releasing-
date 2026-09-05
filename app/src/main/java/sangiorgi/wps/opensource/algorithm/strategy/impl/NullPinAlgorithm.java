package sangiorgi.wps.opensource.algorithm.strategy.impl;

import sangiorgi.wps.opensource.algorithm.strategy.AlgorithmException;
import sangiorgi.wps.opensource.algorithm.strategy.BaseWpsAlgorithm;

/**
 * Empty-PIN vulnerability (all-zeros WPS PIN).
 *
 * <p>A number of Realtek and Broadcom based firmwares ship with, or can be reset into, a state
 * where the registrar accepts the all-zeros PIN "00000000". This is one of the cheapest WPS
 * weaknesses to test: a single attempt that instantly succeeds on vulnerable devices.
 */
public class NullPinAlgorithm extends BaseWpsAlgorithm {

  public NullPinAlgorithm() {
    super("Empty PIN");
  }

  @Override
  public String generatePin(String bssid, String ssid) throws AlgorithmException {
    if (!validateInput(bssid, ssid)) {
      throw new AlgorithmException("Invalid BSSID");
    }

    // 7 zero digits + the checksum of zero (also 0) yields the all-zeros PIN.
    return formatPinWithChecksum(0);
  }
}
