package parser.packets.icmpv6;

import parser.util.Util;
import parser.exceptions.PacketFormatWrongException;
import parser.packets.ipv6.optionalPart.OptionalPart;

public abstract class ICMPv6 extends OptionalPart {
  //general Format by Rfc 4443

  byte[] type; //8
  byte[] code;  //8
  byte[] checksum;  //16

  @Override
  public byte[] dump () {
    return Util.mergeBytes(type, code, checksum);
  }

  @Override
  public String toString () {
    String ret = "";
    ret += Util.byteArraytoString(type, 2);
    ret += Util.byteArraytoString(code, 2);
    ret += Util.byteArraytoString(checksum, 2);
    return ret;
  }
  public abstract boolean hasIPv6();

  public byte[] getType () {
    return type;
  }

  public void setType (byte[] type) {
    this.type = type;
  }

  public byte[] getCode () {
    return code;
  }

  public void setCode (byte[] code) {
    this.code = code;
  }

  public byte[] getChecksum () {
    return checksum;
  }

  public void setChecksum (byte[] checksum) {
    this.checksum = checksum;
  }

  public static ICMPv6 parse (byte[] data) throws PacketFormatWrongException {
    ICMPv6 icmPv6 = null;
    switch (data[0] & 0xff) {
      case 3:
        //time exceeded
        try {
          icmPv6 = ICMPv6TimeExceeded.parse(data);
        } catch (IndexOutOfBoundsException e) {
          throw new PacketFormatWrongException("ICMPv6TimeExceeded parse wrong!");
        }
        //        if (iPv6Packet == null || iPv6Packet.dump().length < 40) {
        //          return null;
        //        }
        break;
      case 1:
        //destination unreachable
        try {
          icmPv6 = ICMPv6DestinationUnreachable.parse(data);
        } catch (IndexOutOfBoundsException e) {
          throw new PacketFormatWrongException("ICMPv6DestinationUnreachable parse wrong");
        }
        //        if (iPv6Packet == null || iPv6Packet.dump().length < 40) {
        //          return null;
        //        }
        break;
      case 129:
        //echo reply
        try {
          icmPv6 = ICMPv6EchoReply.parse(data);
        } catch (IndexOutOfBoundsException e) {
          throw new PacketFormatWrongException("ICMPv6EchoReply parse wrong!");
        }
        //        if ((icmPv6.code[0] & 0xff) != 0) {
        //          return null;
        //        }
        break;
      case 128:
        try {
          icmPv6 = ICMPv6EchoRequest.parse(data);
        } catch (IndexOutOfBoundsException e) {
          throw new PacketFormatWrongException("ICMPv6EchoRequest parse wrong!");
        }
        break;
      default:
        throw new PacketFormatWrongException("no ICMPv6 type!");
    }
    return icmPv6;
  }

}
