package parser.packets.ipv6;

import parser.util.Util;

public enum NextHeader {
  UDP(0x11),
  ICMPv6(0x3a),
  HOP_BY_HOP(0x00),
  ROUTING(0x2b),
  DESTINATION_OPTIONS(0x3c)
  ;
  private int number;
  NextHeader (int number) {
    this.number= number;
  }

  public int getNumber () {
    return number;
  }

  public byte[] dump(){
    byte[] ret= new byte[1];
    ret[0] = (byte)this.getNumber();
    return ret;
  }


  @Override
  public String toString () {
    return Util.intToHexString2B(getNumber());
  }
}
