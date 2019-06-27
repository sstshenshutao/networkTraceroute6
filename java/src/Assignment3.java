import filter.Filter;
import filter.IPv6PacketWrapper;
import parser.exceptions.PacketFormatWrongException;
import parser.packets.icmpv6.*;
import parser.packets.ipv6.IPv6Packet;
import parser.util.Util;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Assignment3 {

  /**
   * This is the entry point for student code.
   * We do highly recommend splitting it up into multiple functions.
   * <p>
   * A good rule of thumb is to make loop bodies functions and group operations
   * that work on the same layer into functions.
   * <p>
   * For reading from the network have a look at assignment2. Also read the
   * comments in GRNVS_RAW.java
   * <p>
   * To get your own IP address use the getIPv6 function.
   * This one is also documented in GRNVS_RAW.java
   */
  public static void run (GRNVS_RAW sock, String dst, int timeout, int attempts, int hopLimit) {
    byte[] buffer = new byte[1514];
    int length = 0;
    byte[] dstIp = Util.parseIpv6Address(dst);
    byte[] srcIp = srcIpv6;
    byte[] ipHeader;
    byte[] payload;
    System.err.println(dst);
    //    Util.printOutBytes(dstIp, -1, "02x", " ");


    /*====================================TODO===================================*/

    /* TODO:
     * 1) Initialize the addresses required to build the packet
     * 2) Loop over hoplimit and attempts
     * 3) Build and send the packet for each iteration
     * 4) Print the hops found in the specified format
     */
    IPv6Packet iPv6Packet = new IPv6Packet();
    iPv6Packet.setVersion(new byte[] { 6 });
    iPv6Packet.setSourceAddress(srcIp);
    iPv6Packet.setDestinationAddress(dstIp);
    byte[] lastSource = new byte[0];
    int hopLimitLoop = 1;
    int sequence = 0;
    while (hopLimitLoop <= hopLimit && Util.compareBytes(lastSource, dstIp) != 0) {
      //      if (hopLimitLoop != 1) {
      //        System.out.println();
      //      }
      boolean breakWhile = false;
      System.out.print(hopLimitLoop);
      for (int i = 0; i < attempts; i++) {
        if (Util.compareBytes(lastSource, dstIp) == 0) {
          breakWhile = true;
        }
        String outIP = "";
        //add hop limit
        iPv6Packet.setHopLimit(new byte[] { (byte) hopLimitLoop });
        //icmpv6
        ICMPv6EchoRequest icmPv6EchoRequest = new ICMPv6EchoRequest();
        icmPv6EchoRequest.setIdentifier(Util.dumpString("1e 66"));
        icmPv6EchoRequest.setSequenceNumber(Util.dumpString(Util.intTo2BString(sequence)));
        icmPv6EchoRequest.setData(Util.dumpString(""));
        //set nextHeader field
        iPv6Packet.setNextHeader(Util.dumpString("3a"));
        //set extension headers to ipv6 Packet
        //set Icmpv6 to ipv6 Packet
        iPv6Packet.addOptionalParts(icmPv6EchoRequest);
        //calculate length
        length = iPv6Packet.dump().length;
        //        System.err.println("length:" + length);
        iPv6Packet.setPayloadLength(Util.dumpString(Util.intTo2BString(length - 40)));
        //set checksum to icmpv6
        buffer = iPv6Packet.dump();
        byte[] cksum = GRNVS_RAW.checksum(buffer, 0, buffer, 40, length - 40);
        icmPv6EchoRequest.setChecksum(cksum);
        buffer = iPv6Packet.dump();
        //        System.err.println("writeOutBuffer");
        //        Util.printOutBytes(buffer, length, "02x", " ");
        //write out packets
        int ret = sock.write(buffer, length);
        if (0 >= ret) {
          System.err.printf("failed to write to socket: %d\n", ret);
          sock.hexdump(buffer, length);
          System.exit(1);
        } else {
          //get something back
          byte[] retBuffer = new byte[1524];
          long startTime = System.currentTimeMillis();
          long runTime = startTime;
          int retSize = -1;
          long timeoutMillis = timeout * 1000;
          boolean whileTimeout = false;
          boolean unreachable = false;
          lastSource = new byte[0];
          do {
            runTime = System.currentTimeMillis();
            if (runTime - startTime > timeoutMillis) {
              whileTimeout = true;
              break;
            }
            retSize = sock.read(retBuffer, new Timeout(timeout));
            if (retSize <= 0) {
              continue;
            }
            byte[] tmp = new byte[retSize];
            System.arraycopy(retBuffer, 0, tmp, 0, retSize);
            IPv6PacketWrapper retIPv6Packet = null;
            try {
              retIPv6Packet = Filter.fromBinary(tmp, iPv6Packet,checksumFunction());
            } catch (PacketFormatWrongException e) {
//              if (e.getDescription().equals("checksum wrong")) {
//                System.err.println(e.toString());
//                GRNVS_RAW.hexdump(retBuffer,retSize);
//              }
              retIPv6Packet = null;
            }
            if (retIPv6Packet != null) {
              lastSource = retIPv6Packet.getiPv6Packet().getSourceAddress();
              if (retIPv6Packet.getIcmpType() == Util.DESTINATION_UNREACHABLE) {
                unreachable = true;
              }
              //debug:
              byte[] retdebug = retIPv6Packet.getiPv6Packet().dump();
              GRNVS_RAW.hexdump(retdebug, retdebug.length);
              //end debug
              break;
            }
          }
          while (lastSource.length == 0);
          if (whileTimeout) {
            outIP = "*";
          } else if (unreachable) {
            outIP = Util.getSourceIPFromReply(lastSource) + "!X";
          } else {
            //            retBuffer[retBuffer.length-]
            outIP = Util.getSourceIPFromReply(lastSource);
          }
          //          System.err.println("retBuffer");
          //          Util.printOutBytes(retBuffer, length, "02x", " ");
          System.out.print("  ");
          System.out.print(outIP);
        }
        sequence++;
        if (outIP.length() >= 2 && outIP.substring(outIP.length() - 2).equals("!X")) {
          breakWhile = true;
        }
      }
      hopLimitLoop++;
      System.out.println();
      if (breakWhile) {
        break;
      }
    }
    /*===========================================================================*/
  }

  private static BiFunction<byte[],Integer, byte[] > checksumFunction () {
    return (buffer,offset) -> GRNVS_RAW.checksum(buffer, 0, buffer, offset, buffer.length - offset);
  }

  private static byte[] srcIpv6;

  public static void main (String[] argv) {
    Arguments args = new Arguments(argv);
    GRNVS_RAW sock = null;
    try {
      sock = new GRNVS_RAW(args.iface, 2);
      srcIpv6 = sock.getIPv6();
      run(sock, args.dst, args.timeout, args.attempts, args.hoplimit);
      sock.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

}
