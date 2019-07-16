import java.util.Arrays;

public class Netstrings {

  private int length;
  private String message;

  public Netstrings (String message) {
    this.message = message;
    this.length = message.length();
  }

  public static Netstrings fromString (String str) {
    try {
      int spl = str.indexOf(":");
      String[] strs = new String[] { str.substring(0, spl), str.substring(spl + 1) };
      int len = Integer.parseInt(strs[0]);
      String message = strs[1];
      if (len != message.length()) {
        throw new NetstringsFormatWrongException("Error: invalid message: len != message.length");
      }
      return new Netstrings(message);
    } catch (RuntimeException e) {
      throw new NetstringsFormatWrongException("Error: invalid message: Netstrings Data wrong");
    }
  }
  //  public int isSameString (String s) {
  //    return s.compareTo(message);
  //  }

  public String getSData () {
    if (message.charAt(0) != 'S') {
      throw new NetstringsFormatWrongException("Error: invalid message: S Data flag wrong");
    }
    try {
      return message.substring(2);
    } catch (RuntimeException e) {
      throw new NetstringsFormatWrongException("Error: invalid message: S Data message wrong");
    }
  }

  public String getTData () {
    if (message.charAt(0) != 'T') {
      throw new NetstringsFormatWrongException("Error: invalid message: T Data flag wrong");
    }
    try {
      return message.substring(2);
    } catch (RuntimeException e) {
      throw new NetstringsFormatWrongException("Error: invalid message: T Data message wrong");
    }
  }

  public String getDData () {
    if (message.charAt(0) != 'D') {
      throw new NetstringsFormatWrongException("Error: invalid message: D Data flag wrong");
    }
    try {
      return message.substring(2);
    } catch (RuntimeException e) {
      throw new NetstringsFormatWrongException("Error: invalid message: D Data message wrong");
    }
  }

  @Override
  public String toString () {
    return this.length + ":" + message + ",";
  }

  public static void main (String[] args) {
    System.out.println(Netstrings.fromString("13:S GRNVS V:1.0").getSData());
  }

}
