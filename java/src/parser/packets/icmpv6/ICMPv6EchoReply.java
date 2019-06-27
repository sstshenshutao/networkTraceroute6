package parser.packets.icmpv6;

import parser.util.Util;

public class ICMPv6EchoReply extends ICMPv6 {

  //rfc 4443
  byte[] identifier; //16
  byte[] sequenceNumber;  //16
  byte[] data;//32 * n

  @Override
  public byte[] dump () {
    if (data == null || data.length == 0) {
      return Util.mergeBytes(super.dump(), identifier, sequenceNumber);
    } else {
      return Util.mergeBytes(super.dump(), identifier, sequenceNumber, data);
    }
  }

  @Override
  public boolean hasIPv6 () {
    return false;
  }

  public byte[] getIdentifier () {
    return identifier;
  }

  public void setIdentifier (byte[] identifier) {
    this.identifier = identifier;
  }

  public byte[] getSequenceNumber () {
    return sequenceNumber;
  }

  public void setSequenceNumber (byte[] sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public byte[] getData () {
    return data;
  }

  public void setData (byte[] data) {
    this.data = data;
  }

  public static ICMPv6EchoReply parse (byte[] data) throws IndexOutOfBoundsException, NullPointerException {
    ICMPv6EchoReply icmPv6 = new ICMPv6EchoReply();
    icmPv6.type = new byte[] { data[0] };
    icmPv6.code = new byte[] { data[1] };
    icmPv6.checksum = new byte[] { data[2], data[3] };
    icmPv6.identifier = new byte[] { data[4], data[5] };
    icmPv6.sequenceNumber = new byte[] { data[6], data[7] };
    icmPv6.data = new byte[data.length-8];
    System.arraycopy(data, 8, icmPv6.data, 0, data.length - 8);
    return icmPv6;
  }

}
