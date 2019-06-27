package parser.packets.ipv6.optionalPart;

import parser.exceptions.PacketFormatWrongException;
import parser.util.Util;

public class ExtensionHeader extends OptionalPart {

  /**
   * format cite: rfc 2460
   */
  byte[] nextHeader;
  byte[] headerExtensionLength;
  byte[] data;

  public byte[] getNextHeader () {
    return nextHeader;
  }

  public void setNextHeader (byte[] nextHeader) {
    this.nextHeader = nextHeader;
  }

  public byte[] getHeaderExtensionLength () {
    return headerExtensionLength;
  }

  public void setHeaderExtensionLength (byte[] headerExtensionLength) {
    this.headerExtensionLength = headerExtensionLength;
  }

  public byte[] getData () {
    return data;
  }

  public void setData (byte[] data) {
    this.data = data;
  }

  @Override
  public byte[] dump () {
    byte[] ret = Util.mergeBytes(nextHeader, headerExtensionLength, data);
    return ret;
  }

  @Override
  public String toString () {
    String ret = Util.byteArraytoString(nextHeader, 2);
    ret += Util.byteArraytoString(headerExtensionLength, 2);
    ret += Util.byteArraytoString(data, 2);
    return ret;
  }

  public static ExtensionHeader parse (byte[] data) throws PacketFormatWrongException {
//    System.err.println("ExtensionHeader parse");
//    Util.printOutBytes(data);
//    System.err.println("ExtensionHeader parse data end");
    ExtensionHeader extensionHeader = new ExtensionHeader();
    try {
      extensionHeader.nextHeader = new byte[] { data[0] };
      extensionHeader.headerExtensionLength = new byte[] { data[1] };
      int length = (((int) data[1] & 0xff) + 1) * 8;
      extensionHeader.data = new byte[length-2];
      System.arraycopy(data, 2, extensionHeader.data, 0, length-2);
    } catch (IndexOutOfBoundsException e) {
      throw new PacketFormatWrongException("parse extension header wrong!");
    }
    return extensionHeader;
  }

}