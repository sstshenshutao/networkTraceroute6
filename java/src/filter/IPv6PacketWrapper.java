package filter;

import parser.packets.ipv6.IPv6Packet;

public class IPv6PacketWrapper {

  int icmpType;
  IPv6Packet iPv6Packet;

  public IPv6PacketWrapper (int icmpType, IPv6Packet iPv6Packet) {
    this.icmpType = icmpType;
    this.iPv6Packet = iPv6Packet;
  }

  public int getIcmpType () {
    return icmpType;
  }

  public void setIcmpType (int icmpType) {
    this.icmpType = icmpType;
  }

  public IPv6Packet getiPv6Packet () {
    return iPv6Packet;
  }

  public void setiPv6Packet (IPv6Packet iPv6Packet) {
    this.iPv6Packet = iPv6Packet;
  }

}
