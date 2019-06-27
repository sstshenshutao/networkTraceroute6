package parser.packets.icmpv6;

import parser.util.Util;
import parser.packets.ipv6.IPv6Packet;

public class ICMPv6TimeExceeded extends ICMPv6 {

  byte[] unused; //32
  IPv6Packet iPv6Packet;//for our cases only this packet

  @Override
  public byte[] dump () {
    return Util.mergeBytes(super.dump(), unused, iPv6Packet.dump());
  }

  @Override
  public String toString () {
    String ret = super.toString();
    ret += Util.byteArraytoString(unused, 2);
    ret += iPv6Packet.toString();
    return ret;
  }

  @Override
  public boolean hasIPv6 () {
    return true;
  }

  public byte[] getUnused () {
    return unused;
  }

  public void setUnused (byte[] unused) {
    this.unused = unused;
  }

  public IPv6Packet getiPv6Packet () {
    return iPv6Packet;
  }

  public void setiPv6Packet (IPv6Packet iPv6Packet) {
    this.iPv6Packet = iPv6Packet;
  }

  public static ICMPv6TimeExceeded parse (byte[] data)throws IndexOutOfBoundsException, NullPointerException {
    ICMPv6TimeExceeded icmPv6 = new ICMPv6TimeExceeded();
    icmPv6.type = new byte[] { data[0] };
    icmPv6.code = new byte[] { data[1] };
    icmPv6.checksum = new byte[] { data[2], data[3] };
    icmPv6.unused = new byte[] { data[4], data[5], data[6], data[7] };
    byte[] ipv6 = new byte[data.length - 8];
    System.arraycopy(data, 8, ipv6, 0, data.length - 8);
    IPv6Packet iPv6Packet = IPv6Packet.parse(ipv6);
    icmPv6.setiPv6Packet(iPv6Packet);
    return icmPv6;
  }

}
