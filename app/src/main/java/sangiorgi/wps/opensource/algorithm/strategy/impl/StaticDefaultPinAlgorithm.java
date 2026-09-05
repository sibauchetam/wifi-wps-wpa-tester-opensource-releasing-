package sangiorgi.wps.opensource.algorithm.strategy.impl;

import sangiorgi.wps.opensource.algorithm.strategy.AlgorithmException;
import sangiorgi.wps.opensource.algorithm.strategy.BaseWpsAlgorithm;

/**
 * Documented static default WPS PIN.
 *
 * <p>Some router families ship with a hard-coded factory WPS PIN that is the same across many units
 * and firmwares. These PINs are widely documented in the security community (Reaver and Wifite
 * default PIN lists, vendor advisories) and are returned verbatim: famous defaults are usually
 * quoted without the checksum digit, and most access points do not validate the checksum anyway, so
 * recomputing it would break the PIN.
 *
 * <p>Instances are shared via the {@link
 * sangiorgi.wps.opensource.algorithm.strategy.AlgorithmFactory}.
 */
public class StaticDefaultPinAlgorithm extends BaseWpsAlgorithm {

  private final String staticPin;

  public StaticDefaultPinAlgorithm(String algorithmName, String staticPin) {
    super(algorithmName);
    this.staticPin = staticPin;
  }

  @Override
  public String generatePin(String bssid, String ssid) throws AlgorithmException {
    if (!validateInput(bssid, ssid)) {
      throw new AlgorithmException("Invalid BSSID");
    }

    // Return the documented default verbatim - no checksum recomputation.
    return staticPin;
  }
}
