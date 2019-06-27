package filter;

import parser.Parser;
import parser.exceptions.PacketFormatWrongException;
import parser.packets.icmpv6.*;
import parser.util.Util;
import parser.packets.ipv6.IPv6Packet;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Filter {

  public static IPv6PacketWrapper fromBinary (byte[] data, IPv6Packet sendPacket,
    BiFunction<byte[], Integer, byte[]> checksumFunction) throws PacketFormatWrongException {
    IPv6Packet ret = null;
    boolean before = beforeAdvice(data, sendPacket);
    try {
      ret = Parser.fromBinary(data);
    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
//      System.err.println("java.lang.ArrayIndexOutOfBoundsException:");
//      Util.printOutBytes(data);
//      System.err.println("==end java.lang.ArrayIndexOutOfBoundsException==");
    }
    int after = afterAdvice(data, ret, sendPacket, checksumFunction);
    return before
           ? new IPv6PacketWrapper(after, ret)
           : null;
  }

  private static boolean beforeAdvice (byte[] data, IPv6Packet sendPacket) throws PacketFormatWrongException {
    return true;
  }

  private static int afterAdvice (byte[] data, IPv6Packet parsed, IPv6Packet sendPacket,
    BiFunction<byte[], Integer, byte[]> checksumFunction) throws PacketFormatWrongException {
    //check the packets is ipv6
    if (Util.compareBytes(parsed.getVersion(), Util.dumpString("06")) != 0) {
      //      System.err.println("getVersion:");
      //      Util.printOutBytes(parsed.getVersion());
      throw new PacketFormatWrongException("check the packets is ipv6");
    }
    //check payload
    if (Util.compareBytes(parsed.getPayloadLength(), Util.dumpString(Util.intTo2BString(data.length - 40))) != 0) {
      throw new PacketFormatWrongException("check payload");
    }
    //check icmp->ipv6 == sent, might be too strict
    // check source and dst ip addrss
    //this is my send packet
    if (Util.compareBytes(parsed.getSourceAddress(), sendPacket.getSourceAddress()) == 0) {
      throw new PacketFormatWrongException("this is my send packet");
    }
    if (Util.compareBytes(parsed.getDestinationAddress(), sendPacket.getSourceAddress()) != 0) {
      throw new PacketFormatWrongException("check source and dst ip addrss");
    }
    // no icmp
    if (parsed.getICMPv6() == null) {
      throw new PacketFormatWrongException("no icmp");
    }
/*
  test icmp \\\
 */
    boolean debug = false;
    ICMPv6 icmPv6 = parsed.getICMPv6();
//    System.err.println("parsed.icmPv6. dump");
//    Util.printOutBytes(icmPv6.dump());
//    System.err.println("parsed.icmPv6. dump end");
//    System.err.println("parsed.getOptionalParts().size():" + parsed.getOptionalParts().size());
//    Util.printOutBytes(parsed.dumpExtensionHeader());
//    System.err.println("parsed.icmPv6. total dump end too");
    //checksum test
    int checksumFunctionOffset = 40;
    if (parsed.getOptionalParts().size() > 1) {
      //      has ext header
      for (int i = 0; i < parsed.getOptionalParts().size() - 1; i++) {
        checksumFunctionOffset += parsed.getOptionalParts().get(i).dump().length;
      }
    }
    byte[] calculatedChecksum = checksumFunction.apply(parsed.dump(), checksumFunctionOffset);
//    System.err.println("icmPv6.getChecksum()");
//    Util.printOutBytes(icmPv6.getChecksum());
//    System.err.println("calculatedChecksum");
//    Util.printOutBytes(calculatedChecksum);
    if (Util.compareBytes(calculatedChecksum, Util.dumpString("00 " + "00")) != 0) {
      throw new PacketFormatWrongException("checksum wrong");
    }
    if (debug) {
      System.err.println("debug1");
    }
    if (icmPv6.hasIPv6()) {
      if (icmPv6 instanceof ICMPv6DestinationUnreachable) {
        //      unreachable
        //send and receive not the same
        if (!checkEqualsIPv6(((ICMPv6DestinationUnreachable) icmPv6).getiPv6Packet(), sendPacket)) {
          throw new PacketFormatWrongException("unreachable send and receive not the same");
        }
        if (debug) {
          System.err.println("debug2");
        }
        return Util.DESTINATION_UNREACHABLE;
      } else if (icmPv6 instanceof ICMPv6TimeExceeded) {
        //      icmp  timeExceed
        IPv6Packet iPv6Inside = ((ICMPv6TimeExceeded) icmPv6).getiPv6Packet();
        if (!(iPv6Inside.getICMPv6() instanceof ICMPv6EchoRequest)) {
          throw new PacketFormatWrongException("ICMPv6TimeExceeded receive no ICMPv6EchoRequest");
        }
        //check message also checked the sent identifier and sequence
        if (!checkEqualsIPv6(iPv6Inside, sendPacket)) {
          //          System.err.println("iPv6Inside.dump()");
          //          Util.printOutBytes(iPv6Inside.dump());
          //          System.err.println("sendPacket.dump()");
          //          Util.printOutBytes(sendPacket.dump());
          throw new PacketFormatWrongException("ICMPv6TimeExceeded send and receive not the same");
        }
        if (Util.compareBytes(icmPv6.getCode(), new byte[] { 0 }) != 0) {
          throw new PacketFormatWrongException("ICMPv6TimeExceeded code not 0");
        }
        if (debug) {
          System.err.println("debug3");
        }
        return Util.TIME_EXCEEDED;
      }
    } else {
      if (icmPv6 instanceof ICMPv6EchoReply) {
        //code==0
        if (Util.compareBytes(icmPv6.getCode(), Util.dumpString("00")) != 0) {
          throw new PacketFormatWrongException("ICMPv6EchoReply check code==0");
        }
        //check sequenceNumber and identifier and ip
        if (Util.compareBytes(((ICMPv6EchoReply) icmPv6).getIdentifier(),
          ((ICMPv6EchoRequest) sendPacket.getICMPv6()).getIdentifier()) != 0 || Util
          .compareBytes(((ICMPv6EchoReply) icmPv6).getSequenceNumber(),
            ((ICMPv6EchoRequest) sendPacket.getICMPv6()).getSequenceNumber()) != 0) {
          throw new PacketFormatWrongException("ICMPv6EchoReply check sequenceNumber and identifier and ip");
        }
        //check return.srcip ??=sent.dst
        if (Util.compareBytes(parsed.getSourceAddress(), sendPacket.getDestinationAddress()) != 0) {
          throw new PacketFormatWrongException("ICMPv6EchoReply check return.srcip ??=sent.dst");
        }
        if (debug) {
          System.err.println("debug4");
        }
        return Util.ECHO_REPLY;
      } else {
        if (debug) {
          System.err.println("debug5");
        }
        throw new PacketFormatWrongException("ICMPv6 unknown type find!");
      }
    }
    if (debug) {
      System.err.println("debug6");
    }
    return -1;
  }
  //            if (retIPv6Packet != null && retIPv6Packet.getICMPv6() != null) {
  //              ICMPv6 icmPv6 = retIPv6Packet.getICMPv6();
  //              if (icmPv6 != null && icmPv6 instanceof ICMPv6DestinationUnreachable) {
  //              unreachable = true;
  //              //                if(!((ICMPv6DestinationUnreachable) icmPv6).getiPv6Packet().equals(iPv6Packet)){
  //              //                  continue;
  //              //                }
  //              //                byte[] fl=((ICMPv6DestinationUnreachable) icmPv6).getiPv6Packet().getFlowLabel();
  //              //                if(Util.compareBytes(fl,iPv6Packet))
  //              System.err.println("ICMPv6DestinationUnreachable");
  //              lastSource = retIPv6Packet.getSourceAddress();
  //              System.err.println("ICMPv6DestinationUnreachable data");
  //              Util.printOutBytes(tmp, -1, "02x", " ");
  //              System.err.println("ICMPv6DestinationUnreachable data end");
  //              break;
  //              } else if (icmPv6 != null && icmPv6 instanceof ICMPv6TimeExceeded) {
  //              ICMPv6 icmPv6Inside = ((ICMPv6TimeExceeded) icmPv6).getiPv6Packet().getICMPv6();
  //              if (icmPv6Inside != null && icmPv6Inside instanceof ICMPv6EchoRequest) {
  //              if (
  //              Util.compareBytes(((ICMPv6EchoRequest) icmPv6Inside).getIdentifier(), Util.dumpString("1e 66")) == 0
  //              && (Util.compareBytes(((ICMPv6EchoRequest) icmPv6Inside).getSequenceNumber(),
  //              Util.dumpString(Util.intTo2BString(sequence))) == 0)) {
  //              //passed
  //              System.err.println(":ICMPv6EchoRequest");
  //              lastSource = retIPv6Packet.getSourceAddress();
  //              System.err.println("get");
  //              Util.printOutBytes(lastSource, -1, "02x", " ");
  //              System.err.println("get end");
  //              }
  //              }
  //              } else if (icmPv6 != null && icmPv6 instanceof ICMPv6EchoReply) {
  //              //finished
  //              if (Util.compareBytes(((ICMPv6EchoReply) icmPv6).getIdentifier(), Util.dumpString("1e 66")) == 0 && Util
  //              .compareBytes(((ICMPv6EchoReply) icmPv6).getSequenceNumber(),
  //              Util.dumpString(Util.intTo2BString(sequence))) == 0) {
  //              System.err.println(":ICMPv6EchoReply");
  //              if (Util.compareBytes(retIPv6Packet.getSourceAddress(), dstIp) != 0) {
  //              System.err.println("echo reply src wrong:compare==0");
  //              continue;
  //              }
  //              System.err.println("tmp");
  //              Util.printOutBytes(tmp, -1, "02x", " ");
  //
  //              byte[] icmpReplyCksum = GRNVS_RAW.checksum(tmp, 0, tmp, iPv6Packet.dumpFixHeader().length+iPv6Packet.dumpExtensionHeader().length, tmp.length - 40);
  //              //                  ===
  //              if (Util.compareBytes(icmpReplyCksum, Util.dumpString("00 00")) == 0){
  //              //tested based code, because the hop after reply (1) is wrong checksum
  //              }else
  //              //                  ===
  //              if ( Util.compareBytes(icmpReplyCksum, ((ICMPv6EchoReply) icmPv6).getChecksum()) != 0) {
  //              System.err.println("echo icmpReplyCksum wrong:compare!=0");
  //              System.err.println("icmpReplyCksum");
  //              Util.printOutBytes(icmpReplyCksum, -1, "02x", " ");
  //              System.err.println();
  //              System.err.println("((ICMPv6EchoReply) icmPv6).getChecksum()");
  //              Util.printOutBytes(((ICMPv6EchoReply) icmPv6).getChecksum(), -1, "02x", " ");
  //              System.err.println();
  //              continue;
  //              }
  //              lastSource = retIPv6Packet.getSourceAddress();
  //              System.err.println("get");
  //              Util.printOutBytes(lastSource, -1, "02x", " ");
  //              System.err.println("get end");
  //              }
  //              }
  //              }

  public static boolean checkEqualsIPv6 (IPv6Packet ob1, IPv6Packet ob2) {
    return Util.compareBytes(ob1.getFlowLabel(), ob2.getFlowLabel()) == 0
      && Util.compareBytes(ob1.getVersion(), ob2.getVersion()) == 0
      && Util.compareBytes(ob1.getDestinationAddress(), ob2.getDestinationAddress()) == 0
      && Util.compareBytes(ob1.getNextHeader(), ob2.getNextHeader()) == 0
      && Util.compareBytes(ob1.getPayloadLength(), ob2.getPayloadLength()) == 0
      && Util.compareBytes(ob1.getSourceAddress(), ob2.getSourceAddress()) == 0
      && Util.compareBytes(ob1.getICMPv6().dump(), ob2.getICMPv6().dump()) == 0;
  }
  //  public boolean checkEqualsICMP (ICMPv6 ob1, ICMPv6 ob2) {
  //  }
}
