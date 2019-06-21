import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Assignment3bak {

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
    byte[] dstIp = string2Bytes(dst);
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
    //while (false) {
    /* Make sure you set your length before you get here */
    byte[] ipv6HdrSchema = dumpString("60 0a d3 3f FF FF 11 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF "
      + "FF FF FF FF FF FF FF FF FF FF FF FF FF FF");
    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.SOURCE_IP, srcIp, ipv6HdrSchema);
    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.DESTINATION_IP, dstIp, ipv6HdrSchema);
    //need to check if 123 the right order
    //=====with header-======
    //    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.NEXT_HEADER, dumpString("00"), ipv6HdrSchema);
    //    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.EXTENSION_HEADERS, dumpString("2b"), 0,
    //      dumpString("01 00 05 02 " + "00 00"), ipv6HdrSchema);
    //    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.EXTENSION_HEADERS, dumpString("3c"), 0,
    //      dumpString("00 00 00 00 " + "00 00"), ipv6HdrSchema);
    //    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.EXTENSION_HEADERS, dumpString("3a"), 0,
    //      dumpString("00 00 00 00" + " 00 00"), ipv6HdrSchema);
    //=====with header-======
    //=====without header======
    ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.NEXT_HEADER, dumpString("3a"), ipv6HdrSchema);
    //=====without header======
    //need to change in the while loop;
    String lastSource = "";
    int hopLimitLoop = 1;
    int sequence = 0;
    while (hopLimitLoop <= hopLimit && compareBytes(string2Bytes(lastSource), dstIp) != 0) {
      System.out.print(hopLimitLoop);
      System.out.print("  ");
      for (int i = 0; i < attempts; i++) {
        byte[] tmpByteLH = new byte[1];
        tmpByteLH[0] = (byte) hopLimitLoop;
        ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.HOP_LIMIT, tmpByteLH, ipv6HdrSchema);
        byte[] icmpHdr = dumpString("80 00 00 00 1c 9a "+int2OctetString(sequence));
        byte[] icmpData = dumpString("5d 0b 9e 9b 00 0a 1e 2a");
        length = ipv6HdrSchema.length + icmpHdr.length + icmpData.length;
        ipv6HdrSchema = ipv6HeaderAddAttribute(Ipv6Header.PAYLOAD_LENGTH, dumpString(int2OctetString(length - 40)),
          ipv6HdrSchema);
        //deal with checksum
        buffer = combineBytes(ipv6HdrSchema, icmpHdr);
        buffer = combineBytes(buffer, icmpData);
        byte[] cksum = GRNVS_RAW.checksum(buffer, 0, combineBytes(icmpHdr, icmpData), 0, 16);
        buffer[buffer.length - 16 + 2] = cksum[0];
        buffer[buffer.length - 16 + 3] = cksum[1];

        /* You still have to put the checksum into the buffer! */
        int ret = sock.write(buffer, length);
        //        System.err.println("write ret length:" + ret);
        if (0 >= ret) {
          System.err.printf("failed to write to socket: %d\n", ret);
          sock.hexdump(buffer, length);
          System.exit(1);
        } else {
          //check the length -12 +0+1 identifier
          byte[] retBuffer = new byte[1524];
          long startTime = System.currentTimeMillis();
          long runTime = startTime;
          int retSize = sock.read(retBuffer, new Timeout(timeout));
          long timeoutMillis = timeout * 1000;
          //      identifier
          boolean whileTimeout = false;
          while (!(retSize > length && retBuffer[retSize - 12] == dumpString("1c")[0]
            && retBuffer[retSize - 12 + 1] == dumpString("9a")[0])) {
            //drop
            retSize = sock.read(retBuffer, new Timeout(timeout));
            runTime = System.currentTimeMillis();
            if (runTime - startTime > timeoutMillis) {
              whileTimeout = true;
              break;
            }
          }
          //want:
          //          if (hopLimitLoop == 4) {
          //            printOutBuffer(retBuffer, retSize);
          //          }
          if (whileTimeout) {
            lastSource = "*";
          } else {
//            retBuffer[retBuffer.length-]
            lastSource = getSourceIPFromReply(retBuffer);
          }
          System.out.print(lastSource);
        }
        if (i != 2) {
          System.out.print("  ");
        }
      }
      hopLimitLoop++;
      sequence++;
      System.out.println();
    }
  }
  //}
  /*===========================================================================*/

  private static int compareBytes (byte[] b1, byte[] b2) {
    if (b1.length != b2.length) {
      return -1;
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] > b2[i]) {
        return 1;
      } else if (b1[i] < b2[i]) {
        return -1;
      }
    }
    return 0;
  }

  public static String getSourceIPFromReply (byte[] retBuffer) {
    String retIPV6 = "";
    for (int i = 7; i >= 0; i--) {
      String firstOctet = Integer.toHexString((int) retBuffer[2 * i + 8] & 0xff);
      if (firstOctet.equals("0")) {
        firstOctet = "";
      }
      String secondOctet = Integer.toHexString((int) retBuffer[2 * i + 9] & 0xff);
      if (secondOctet.length() == 1 && firstOctet.length() != 0) {
        secondOctet = "0" + secondOctet;
      }
      retIPV6 = firstOctet + secondOctet + ":" + retIPV6;
    }
    retIPV6 = retIPV6.substring(0, retIPV6.length() - 1);
    return retIPV6;
  }

  public static String int2OctetString (int ori) {
    String payloadLengthStr = Integer.toHexString(ori);
    switch (payloadLengthStr.length()) {
      case 1:
        payloadLengthStr = "00 0" + payloadLengthStr;
        break;
      case 2:
        payloadLengthStr = "00 " + payloadLengthStr;
        break;
      case 3:
        payloadLengthStr = "0" + payloadLengthStr.substring(0, 1) + " " + payloadLengthStr.substring(1);
        break;
      case 4:
        break;
      default:
        System.err.println("something wrong with int2OctetString");
    }
    return payloadLengthStr;
  }

  private static byte[] ipv6HeaderAddAttribute (Ipv6Header extensionHeaders, byte[] nextHeader, int headerLength,
    byte[] data, byte[] headerSchema) {
    if (extensionHeaders != Ipv6Header.EXTENSION_HEADERS) {
      return headerSchema;
    }
    int dataLength = (data == null
                      ? 0
                      : data.length);
    byte[] ret = new byte[headerSchema.length + 1 + 1 + dataLength];
    for (int i = 0; i < headerSchema.length; i++) {
      ret[i] = headerSchema[i];
    }
    int offset = headerSchema.length;// new 0
    ret[offset] = nextHeader[0];
    ret[offset + 1] = (byte) headerLength;
    if (dataLength == 0) {
    } else {
      for (int i = 0; i < data.length; i++) {
        ret[offset + 2 + i] = data[i];
      }
    }
    return ret;
  }

  private static byte[] ipv6HeaderAddAttribute (Ipv6Header attribute, byte[] values, byte[] headerSchema) {
    switch (attribute) {
      case PAYLOAD_LENGTH:
        headerSchema[4] = values[0];
        headerSchema[5] = values[1];
        break;
      case NEXT_HEADER:
        headerSchema[6] = values[0];
        break;
      case HOP_LIMIT:
        headerSchema[7] = values[0];
        break;
      case SOURCE_IP:
        for (int i = 0; i < 16; i++) {
          headerSchema[8 + i] = values[i];
        }
        break;
      case DESTINATION_IP:
        for (int i = 0; i < 16; i++) {
          headerSchema[24 + i] = values[i];
        }
        break;
      default:
        System.err.println("something wrong with ipv6HeaderAddAttribute()");
    }
    return headerSchema;
  }

  /**
   * @param ipv6Str
   * @return
   */
  public static byte[] string2Bytes (String ipv6Str) {
    if (ipv6Str.equals("*")) {
      return new byte[0];
    }
    int doubleColon = ipv6Str.indexOf("::");
    if (doubleColon != -1) {
      //Destination: 2a00:1450:4001:81d::200e
      int numberPart = ipv6Str.split(":").length - 1;
      int wholePart = 8;
      int need2add = wholePart - numberPart;
      String addTmp = "";
      for (int i = 0; i < need2add; i++) {
        addTmp += "0:";
      }
      addTmp = addTmp.substring(0, addTmp.length() - 1);
      ipv6Str = ipv6Str.substring(0, doubleColon + 1) + addTmp + ipv6Str.substring(doubleColon + 1);
    }
    //just translate it dst: 2001:16b8:2ded:5100:742b:b50a:fb75:d561
    String[] parts = ipv6Str.split(":");
    String tmp = "";
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      //for all parts 2 octets
      if (part.length() < 4) {
        for (int j = part.length(); j < 4; j++) {
          part = "0" + part;
        }
      }
      //divided into 1 octet
      tmp += part.substring(0, 2);
      tmp += " ";
      tmp += part.substring(2, 4);
      tmp += " ";
    }
    tmp = tmp.substring(0, tmp.length() - 1);
    return dumpString(tmp);
  }

  private static void printOutBuffer (byte[] buffer, int retSize) {
    for (int i = 0; i < retSize; i++) {
      System.err.printf("%x,", buffer[i]);
    }
  }

  private static byte[] dumpString (String raw) {
    //    String raw =  "00 1c 73 00 00 99 8c 85 90 71 c0 99 86 dd 60 05 90 88 00 14 11 01 20 01 4c a0 20 03 19 20 1d 60 ff d0 4c 7f a4 72 2a 00 14 50 40 01 08 09 00"
    //      + " 00 00 00 00 00 20 0e d9 75 82 9b 00 14 5a 46 01 01 00 00 5d 09 2f 8c 00 0d 61 7b";
    List<Integer> il = new ArrayList<>();
    if (raw.indexOf(" ") == -1) {
      il.add(Integer.parseInt(raw, 16));
    } else {
      il = Arrays.stream(raw.split(" ")).map(x -> Integer.parseInt(x, 16)).collect(Collectors.toList());
    }
    byte[] buffer = new byte[il.size()];
    for (int i = 0; i < il.size(); i++) {
      buffer[i] = (byte) il.get(i).intValue();
    }
    return buffer;
  }

  private static byte[] combineBytes (byte[] b1, byte[] b2) {
    byte[] ret = new byte[b1.length + b2.length];
    for (int i = 0; i < b1.length; i++) {
      ret[i] = b1[i];
    }
    for (int i = 0; i < b2.length; i++) {
      ret[i + b1.length] = b2[i];
    }
    return ret;
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
