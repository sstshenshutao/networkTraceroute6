package parser;

import parser.packets.ipv6.IPv6Packet;

public class Parser {

  public static IPv6Packet fromBinary (byte[] data) {
    return IPv6Packet.parse(data);
  }

}
