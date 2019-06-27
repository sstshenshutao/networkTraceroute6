package parser.packets.ipv6.optionalPart;

import parser.exceptions.PacketFormatWrongException;
import parser.packets.icmpv6.ICMPv6;
import parser.packets.ipv6.NextHeader;

import java.util.ArrayList;
import java.util.List;

public abstract class OptionalPart {

  public abstract byte[] dump ();

  @Override
  public abstract String toString ();

  public static List<OptionalPart> parse (byte[] data, byte[] nextHeaderField) throws PacketFormatWrongException {
    List<OptionalPart> ret = new ArrayList<>();
    //parse nextHeader
    int nextHeader = 0;
    try {
      nextHeader = ((int) nextHeaderField[0] & 0xff);
    } catch (IndexOutOfBoundsException e) {
      throw new PacketFormatWrongException("nextHeader nothing!");
    }
    int length;
    int offset = 0;
    while (nextHeader != NextHeader.ICMPv6.getNumber()) {
      System.err.println("debug o1");
      System.err.println(nextHeader);
      System.err.println(NextHeader.ICMPv6.getNumber());
      if (!optionType(nextHeader)) {
        //        System.err.printf("%02x",nextHeader);
        System.err.println("debug o2");
        throw new PacketFormatWrongException("unknown nextHeader Field!");
      }
      //      System.err.println("nextHeader:" + nextHeader);
      //      System.err.println("NextHeader.ICMPv6.getNumber():" + NextHeader.ICMPv6.getNumber());
      byte[] extHeader = null;
      try {
        nextHeader = ((int) data[offset] & 0xff);
        length = (((int) data[offset + 1] & 0xff) + 1) * 8;
        System.err.println("debug o4 + length" + length);
        extHeader = new byte[length];
        System.arraycopy(data, offset, extHeader, 0, length);
        offset += length;
      } catch (IndexOutOfBoundsException e) {
        throw new PacketFormatWrongException("data wrong!");
      }
      ret.add(ExtensionHeader.parse(extHeader));
    }
    //icmpv6
    ICMPv6 icmPv6 = null;
    try {
      byte[] icmp = new byte[data.length - offset];
      System.arraycopy(data, offset, icmp, 0, data.length - offset);
      icmPv6 = ICMPv6.parse(icmp);
    } catch (IndexOutOfBoundsException e) {
      throw new PacketFormatWrongException("data wrong!");
    }
    System.err.println("debug o3");
    ret.add(icmPv6);
    //      someOther wrong
    return ret;
  }

  private static boolean optionType (int nextHeader) {
    for (NextHeader nh : NextHeader.values()) {
      if (nextHeader == nh.getNumber()) {
        return true;
      }
    }
    return false;
  }

}
