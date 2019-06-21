package packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IPv6Packet {

  @Override
  public String toString () {
    //header
    String ret = Util.byteArraytoString(version, 1);
    ret += Util.byteArraytoString(trafficClass, 2);
    ret += Util.byteArraytoString(new byte[] { flowLabel[0] }, 1);
    ret += Util.byteArraytoString(new byte[] { flowLabel[1], flowLabel[2] }, 2);
    ret += Util.byteArraytoString(payloadLength, 2);
    ret += Util.byteArraytoString(nextHeader, 2);
    ret += Util.byteArraytoString(hopLimit, 2);
    ret += Util.byteArraytoString(sourceAddress, 2);
    ret += Util.byteArraytoString(destinationAddress, 2);
    //others
    for (OptionalPart op : optionalParts) {
      ret += op.toString();
    }
    ret = ret.replaceAll(" ", "");
    StringBuffer newRet = new StringBuffer();
    for (int i = 0; i < ret.length() / 2; i++) {
      newRet.append(ret.charAt(2 * i));
      newRet.append(ret.charAt(2 * i + 1));
      newRet.append(" ");
    }
    return newRet.deleteCharAt(newRet.length() - 1).toString();
  }

  public byte[] dumpFixHeader () {
    byte[] ret = Util.mergeBytes(Util.dumpString("60 00 00 00"), payloadLength, nextHeader, hopLimit, sourceAddress,
      destinationAddress);
    return ret;
  }

  public byte[] dumpExtensionHeader () {
    byte[] ret = new byte[0];
    for (int i = 0; i < optionalParts.size(); i++) {
      if (optionalParts.get(i) instanceof ExtensionHeader) {
        ret = Util.mergeBytes(ret, optionalParts.get(i).dump());
      }
    }
    return ret;
  }

  public byte[] dumpICMPv6 () {
    byte[] ret = new byte[0];
    if (optionalParts.get(optionalParts.size() - 1) instanceof ICMPv6) {
      ret = Util.mergeBytes(ret, optionalParts.get(optionalParts.size() - 1).dump());
    }
    return ret;
  }

  public byte[] dump () {
    byte[] ret = dumpFixHeader();
    ret = Util.mergeBytes(ret, dumpExtensionHeader());
    ret = Util.mergeBytes(ret, dumpICMPv6());
    return ret;
  }

  /**
   * ipv6 fix header
   * cite: the Cheatsheet of GRNVS
   */
  //fixed part: 40 length
  byte[] version;
  byte[] trafficClass;
  byte[] flowLabel;
  byte[] payloadLength;
  byte[] nextHeader;
  byte[] hopLimit;
  byte[] sourceAddress;
  byte[] destinationAddress;
  //    fixLen.put("version", 4);
  //    fixLen.put("trafficClass", 8);
  //    fixLen.put("flowLabel", 20);
  //    fixLen.put("payloadLength", 16);
  //    fixLen.put("nextHeader", 8);
  //    fixLen.put("hopLimit", 8);
  //    fixLen.put("sourceAddress", 32 * 16);
  //    fixLen.put("destinationAddress", 32 * 16);

  /**
   * length as bits
   */
  //  Map<String, Integer> fixLen = new HashMap<>();
  public IPv6Packet () {
    //init schema
    version = Util.dumpString("6");
    trafficClass = Util.dumpString("00");
    flowLabel = Util.dumpString("0 00 00");
    payloadLength = Util.dumpString("00 00");
    nextHeader = Util.dumpString("11");
    hopLimit = Util.dumpString("00");
    sourceAddress = Util.dumpString("FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF");
    destinationAddress = Util.dumpString("FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF");
  }

  /**
   * Extension headers optional part. also as the payloadLength of ipv6 packet
   * either ext headers or icmpv6
   */
  List<OptionalPart> optionalParts = new ArrayList<>();

  public void addOptionalParts (OptionalPart optionalPart) {
    if (optionalPart==null){
      return;
    }
    if (optionalParts.size() == 0 || !(optionalParts.get(optionalParts.size() - 1) instanceof ICMPv6)) {
      this.optionalParts.add(optionalPart);
    } else {
      if (!(optionalPart instanceof ICMPv6)) {
        this.optionalParts.add(optionalParts.size() - 2, optionalPart);
      } else {
        this.optionalParts.set(optionalParts.size() - 1, optionalPart);
      }
    }
  }

  public byte[] getVersion () {
    return version;
  }

  public void setVersion (byte[] version) {
    this.version = version;
  }

  public byte[] getTrafficClass () {
    return trafficClass;
  }

  public void setTrafficClass (byte[] trafficClass) {
    this.trafficClass = trafficClass;
  }

  public byte[] getFlowLabel () {
    return flowLabel;
  }

  public void setFlowLabel (byte[] flowLabel) {
    this.flowLabel = flowLabel;
  }

  public byte[] getPayloadLength () {
    return payloadLength;
  }

  public void setPayloadLength (byte[] payloadLength) {
    this.payloadLength = payloadLength;
  }

  public byte[] getNextHeader () {
    return nextHeader;
  }

  public void setNextHeader (byte[] nextHeader) {
    this.nextHeader = nextHeader;
  }

  public byte[] getHopLimit () {
    return hopLimit;
  }

  public void setHopLimit (byte[] hopLimit) {
    this.hopLimit = hopLimit;
  }

  public byte[] getSourceAddress () {
    return sourceAddress;
  }

  public void setSourceAddress (byte[] sourceAddress) {
    this.sourceAddress = sourceAddress;
  }

  public byte[] getDestinationAddress () {
    return destinationAddress;
  }

  public void setDestinationAddress (byte[] destinationAddress) {
    this.destinationAddress = destinationAddress;
  }

  public List<OptionalPart> getOptionalParts () {
    return optionalParts;
  }

  public ICMPv6 getICMPv6 () {
    if (optionalParts.size() == 0) {
      return null;
    }
    OptionalPart optionalPart = optionalParts.get(optionalParts.size() - 1);
    if (optionalPart instanceof ICMPv6) {
      return (ICMPv6)optionalPart;
    } else {
      return null;
    }
  }

  public static IPv6Packet parse (byte[] data) {
    if (data.length < 40) {
      return null;
    }
    IPv6Packet iPv6Packet = new IPv6Packet();
    String tmpStr = Integer.toHexString((int) data[0] & 0xff);
    if (tmpStr.length() < 2 || tmpStr.charAt(0) != '6') {
//      System.err.println("not ipv6 packet");
      return null;
    }
    iPv6Packet.payloadLength = new byte[] {
      data[4], data[5]
    };
    iPv6Packet.nextHeader = new byte[] {
      data[6]
    };
    iPv6Packet.hopLimit = new byte[] {
      data[7]
    };
    System.arraycopy(data, 8, iPv6Packet.sourceAddress, 0, 16);
    System.arraycopy(data, 24, iPv6Packet.destinationAddress, 0, 16);
    //parse nextHeader
    int nextHeader = ((int) iPv6Packet.nextHeader[0] & 0xff);
    int offset = 40;
    int length = 0;

    while (optionType(nextHeader) && nextHeader != NextHeader.ICMPv6.getNumber()) {
      System.err.println("nextHeader:"+nextHeader);
      System.err.println("NextHeader.ICMPv6.getNumber():"+NextHeader.ICMPv6.getNumber());
      nextHeader = ((int) data[offset] & 0xff);
      length = (((int) data[offset + 1] & 0xff) + 1) * 8;
      offset += length;
      byte[] extHeader = new byte[length];
      System.arraycopy(data, offset, extHeader, 0, length);
      iPv6Packet.addOptionalParts(ExtensionHeader.parse(extHeader));
    }
    if (nextHeader == NextHeader.ICMPv6.getNumber()) {
      //icmpv6
      byte[] icmp = new byte[data.length - offset];
      System.arraycopy(data, offset, icmp, 0, data.length - offset);
      iPv6Packet.addOptionalParts(ICMPv6.parse(icmp, iPv6Packet.sourceAddress, iPv6Packet.destinationAddress));
    } else {
      //      someOther wrong
      return null;
    }
    return iPv6Packet;
  }
  private static boolean optionType(int nextHeader){
    for (NextHeader nh : NextHeader.values()) {
      if (nextHeader == nh.getNumber()) {
        return true;
      }
    }
    return false;
  }
}



