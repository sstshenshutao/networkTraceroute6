package packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Util {

  /**
   * 5 -> 0x05
   *
   * @param ori 5
   * @return 05
   */
  public static String intToHexString2B (int ori) {
    String a = Integer.toHexString(ori);
    if (a.length() == 1) {
      a = "0" + a;
    }
    return a;
  }

  /**
   * 5 -> 0x5
   *
   * @param ori 5
   * @return 5
   */
  public static String intToHexString (int ori) {
    String a = Integer.toHexString(ori);
    return a;
  }

  /**
   * cited from :
   * https://www.techiedelight.com/merge-multiple-arrays-java/
   *
   * @param arrays
   * @return
   */
  public static byte[] mergeBytes (byte[]... arrays) {
    int finalLength = 0;
    for (byte[] array : arrays) {
      finalLength += array.length;
    }
    byte[] dest = null;
    int destPos = 0;
    for (byte[] array : arrays) {
      if (dest == null) {
        dest = Arrays.copyOf(array, finalLength);
        destPos = array.length;
      } else {
        System.arraycopy(array, 0, dest, destPos, array.length);
        destPos += array.length;
      }
    }
    return dest;
  }

  public static String byteArraytoString (byte[] bytes, int len) {
    String ret = "";
    for (int i = 0; i < bytes.length; i++) {
      ret += String.format("%0" + len + "x", bytes[i]);
    }
    return ret;
  }

  /**
   * parse the ipv6 address from string to byte[]
   *
   * @param ipv6Str
   * @return
   */
  public static byte[] parseIpv6Address (String ipv6Str) {
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

  /**
   * dump a space split string to byte[]
   *
   * @param raw
   * @return
   */
  public static byte[] dumpString (String raw) {
    //    String raw =  "00 1c 73 00 00 99 8c 85 90 71 c0 99 86 dd 60 05 90 88 00 14 11 01 20 01 4c a0 20 03 19 20 1d 60 ff d0 4c 7f a4 72 2a 00 14 50 40 01 08 09 00"
    //      + " 00 00 00 00 00 20 0e d9 75 82 9b 00 14 5a 46 01 01 00 00 5d 09 2f 8c 00 0d 61 7b";
    if (raw == null || raw.length() == 0) {
      return new byte[0];
    }
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

  /**
   * print out with x or u or o or d
   *
   * @param buffer   bytes
   * @param retSize  size can be -1 if print all
   * @param flag     x or u or o or d
   * @param splitter " " or ","
   */
  public static void printOutBytes (byte[] buffer, int retSize, String flag, String splitter) {
    retSize = (retSize == -1)
              ? buffer.length
              : retSize;
    for (int i = 0; i < retSize; i++) {
      System.err.printf("%" + flag + splitter, buffer[i]);
    }
  }

  /**
   * compare two bytes
   *
   * @param b1
   * @param b2
   * @return
   */
  public static int compareBytes (byte[] b1, byte[] b2) {
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

  public static String intTo2BString (int ori) {
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

  public static String getSourceIPFromReply (byte[] retBuffer) {
    String retIPV6 = "";
    boolean continueZero = false;
    boolean wShort = false;
    for (int i = 0; i < 8; i++) {
      String firstOctet = Integer.toHexString((int) retBuffer[2 * i] & 0xff);
      if (firstOctet.equals("0")) {
        firstOctet = "";
      }
      String secondOctet = Integer.toHexString((int) retBuffer[2 * i + 1] & 0xff);
      if (secondOctet.length() == 1 && firstOctet.length() != 0) {
        secondOctet = "0" + secondOctet;
      }
      if (!wShort||continueZero) {
        if (firstOctet.length() == 0 && secondOctet.equals("0")) {
          if (continueZero) {
            if (retIPV6.length() >= 2 && retIPV6.charAt(retIPV6.length() - 2) != ':') {
              retIPV6 += ":";
              wShort = true;
              continue;
            } else if (retIPV6.length() == 0) {
              retIPV6 += "::";
              wShort = true;
              continue;
            } else {
              continue;
            }
          } else {
            continueZero = true;
            continue;
          }
        } else {
          continueZero = false;
        }
      }
      retIPV6 += firstOctet + secondOctet + ":";
    }
    retIPV6 = retIPV6.substring(0, retIPV6.length() - 1);
    return retIPV6;
  }

}
