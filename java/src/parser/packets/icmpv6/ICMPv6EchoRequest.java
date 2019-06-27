package parser.packets.icmpv6;

import parser.util.Util;

public class ICMPv6EchoRequest extends ICMPv6 {

  //rfc 4443
  byte[] identifier; //16
  byte[] sequenceNumber;  //16
  byte[] data;//32 * n

  public ICMPv6EchoRequest () {
    super.type = Util.dumpString("80");
    super.code = Util.dumpString("00");
    super.checksum = Util.dumpString("00 00");
  }

  @Override
  public byte[] dump () {
    if (data == null || data.length == 0) {
      return Util.mergeBytes(super.dump(), identifier, sequenceNumber);
    } else {
      return Util.mergeBytes(super.dump(), identifier, sequenceNumber, data);
    }
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

  @Override
  public String toString () {
    String ret = super.toString();
    ret += Util.byteArraytoString(identifier, 2);
    ret += Util.byteArraytoString(sequenceNumber, 2);
    if (data != null) {
      ret += Util.byteArraytoString(data, 2);
    }
    return ret;
  }

  @Override
  public boolean hasIPv6 () {
    return false;
  }

  public static ICMPv6EchoRequest parse (byte[] data) throws IndexOutOfBoundsException, NullPointerException {
    ICMPv6EchoRequest icmPv6 = new ICMPv6EchoRequest();
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
