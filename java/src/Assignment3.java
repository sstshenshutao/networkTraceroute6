import packets.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

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


    /*====================================TODO===================================*/

    /* TODO:
     * 1) Initialize the addresses required to build the packet
     * 2) Loop over hoplimit and attempts
     * 3) Build and send the packet for each iteration
     * 4) Print the hops found in the specified format
     */
    IPv6Packet iPv6Packet = new IPv6Packet();
    iPv6Packet.setSourceAddress(srcIp);
    iPv6Packet.setDestinationAddress(dstIp);
    byte[] lastSource = new byte[0];
    int hopLimitLoop = 1;
    int sequence = 1;
    while (hopLimitLoop <= hopLimit && Util.compareBytes(lastSource, dstIp) != 0) {
      System.out.print(hopLimitLoop);
      System.out.print("  ");
      for (int i = 0; i < attempts; i++) {
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
//  debug      Util.printOutBytes(buffer, length, "02x", " ");
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
          lastSource = new byte[0];
          do {
            runTime = System.currentTimeMillis();
            if (runTime - startTime > timeoutMillis) {
              whileTimeout = true;
              break;
            }
            retSize = sock.read(retBuffer, new Timeout(timeout));
            byte[] tmp = new byte[retSize];
            System.arraycopy(retBuffer, 0, tmp, 0, retSize);
            IPv6Packet retIPv6Packet = null;
            try {
              retIPv6Packet = IPv6Packet.parse(tmp);
            } catch (Exception e) {
              e.printStackTrace();
            }
            if (retIPv6Packet != null && retIPv6Packet.getICMPv6() != null) {
              ICMPv6 icmPv6 = retIPv6Packet.getICMPv6();
              if (icmPv6 != null && icmPv6 instanceof ICMPv6DestinationUnreachable) {
                whileTimeout = true;
                break;
              } else if (icmPv6 != null && icmPv6 instanceof ICMPv6TimeExceeded) {
                ICMPv6 icmPv6Inside = ((ICMPv6TimeExceeded) icmPv6).getiPv6Packet().getICMPv6();
                if (icmPv6Inside != null && icmPv6Inside instanceof ICMPv6EchoRequest) {
                  if (
                    Util.compareBytes(((ICMPv6EchoRequest) icmPv6Inside).getIdentifier(), Util.dumpString("1e 66")) == 0
                      && (Util.compareBytes(((ICMPv6EchoRequest) icmPv6Inside).getSequenceNumber(),
                      Util.dumpString(Util.intTo2BString(sequence))) == 0)) {
                    //passed
                    lastSource = retIPv6Packet.getSourceAddress();
                  }
                }
              } else if (icmPv6 != null && icmPv6 instanceof ICMPv6EchoReply) {
                //finished
                if (Util.compareBytes(((ICMPv6EchoReply) icmPv6).getIdentifier(), Util.dumpString("1e 66")) == 0 && Util
                  .compareBytes(((ICMPv6EchoReply) icmPv6).getSequenceNumber(),
                    Util.dumpString(Util.intTo2BString(sequence))) == 0) {
                  lastSource = retIPv6Packet.getSourceAddress();
                }
              }
            }
          }
          while (lastSource.length == 0);
          String outIP;
          if (whileTimeout) {
            outIP = "*";
          } else {
            //            retBuffer[retBuffer.length-]
            outIP = Util.getSourceIPFromReply(lastSource);
          }
          System.out.print(outIP);
        }
        if (i != 2) {
          System.out.print("  ");
        }
      }
      hopLimitLoop++;
      sequence++;
      System.out.println();
    }
    /*===========================================================================*/
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
