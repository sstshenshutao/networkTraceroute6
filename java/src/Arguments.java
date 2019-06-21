import packets.IPv6Packet;
import packets.Util;

import java.lang.NumberFormatException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Arguments {

  public String dst;
  public String iface = "eth0";
  public int timeout = 5;
  public int hoplimit = 15;
  public int attempts = 3;

  private void printHelp () {
    System.out.println("Traceroute for GRNVS");
    System.out.println("Usage:");
    System.out.println("-i/--interface: The interface on which the frame should be sent");
    System.out.println("-t             The timeout that should be used for timed actions (default: 5)");
    System.out.println("-q             The attempts that should be done per hop (default: 3)");
    System.out.println("-m             The maximum hops to trace over (default: 15)");
    System.out.println("DST:            The destination IPv6 address ");
    System.out.println("-?/--help	    Print this help message");
  }

  Arguments (String[] argv) {
    if (argv.length == 0) {
      printHelp();
      System.exit(0);
    }
    //For_each would be nice, but we may have to skip/access next
    int i, j = 0;
    String[] fargs = new String[1];
    for (i = 0; i < argv.length; ++i) {
      String arg = argv[i];
      switch (arg) {
        case "-?":
        case "--help":
          printHelp();
          System.exit(0);
          break;
        case "-i":
        case "--interface":
          iface = argv[++i];
          break;
        case "-t":
        case "--timeout":
          try {
            timeout = Integer.parseInt(argv[++i]);
          } catch (NumberFormatException e) {
            System.err.println(argv[i - 1] + "is not a valid number");
            System.exit(-1);
          }
          break;
        case "-q":
        case "--attempts":
          try {
            attempts = Integer.parseInt(argv[++i]);
          } catch (NumberFormatException e) {
            System.err.println(argv[i - 1] + "is not a valid number");
            System.exit(-1);
          }
          break;
        case "-m":
        case "--hoplimit":
          try {
            hoplimit = Integer.parseInt(argv[++i]);
          } catch (NumberFormatException e) {
            System.err.println(argv[i - 1] + "is not a valid number");
            System.exit(-1);
          }
          break;
        default:
          if (j == fargs.length) {
            System.out.println("Encountered an unexpected number of positional arguments");
            System.exit(1);
          }
          fargs[j++] = arg;
          break;
      }
    }
    if (fargs[0] == null) {
      System.out.println("Did not find positional argument: destination");
      System.exit(1);
    }
    dst = fargs[0];
  }

  public static void main (String[] args) {
    //    String len = "2a00:1450:4001:81d::200e";
    //    byte[] aa = Assignment3.string2Bytes(len);
    //    for (byte a : aa) {
    //      System.out.printf("%x ",a);
    //    }
    //    System.out.println(Assignment3.getSourceIPFromReply(new byte[]{96,0,0,0,0,88,58,64,42,0,71,0,0,0,0,9,0,0,0,0,0,0,
    //      0,1,42,0,71,0,0,0,0,9,0,15,0,0,0,0,3,79,3,0,16,26,0,0,0,0,96,0,0,0,0,40,0,1,42,0,71,0,0,0,0,9,0,15,0,0,0,0,3,79
    //      ,42,1,4,-8,13,22,25,67,0,0,0,0,0,0,0,2,43,0,1,0,5,2,0,0,60,0,0,0,0,0,0,0,58,0,0,0,0,0,0,0,-128,0,47,76,10,-127,
    //      0,1,93,10,90,83,0,13,-60,-64,}));
    //    byte a = (byte)0xf8;
    //    System.out.println(Integer.toHexString((int)a & 0xff));
    //    System.out.println(Assignment3.int2OctetString(288));
    //test my code
    byte[] ip  = new byte[]{
      0x2a,0,0x47,0,0,0x09,0,0,0,0,0,0,0,0,0,1
    };
    System.out.println(Util.getSourceIPFromReply(ip));

//    IPv6Packet iPv6Packet = new IPv6Packet();
//    Util.printOutBytes(iPv6Packet.dump(),-1,"x"," ");
//    System.err.println("--------");
//    System.err.println(iPv6Packet);

//    System.out.println(String.format("%01x",0));
    //end test
  }

}
