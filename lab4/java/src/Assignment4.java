/* Do NOT!!! put a package statement here, that would break the build system */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Assignment4 {

  /**
   * This function returns the IP address from an address string or host name
   *
   * @param host The address string or host
   * @return A newly allocated byte array filled with the ip address
   * Or null if the String is not an ipv6-address
   */
  private static InetAddress getip (String host) {
    try {
      return InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      return null;
    }
  }

  /**
   * This is the entry function for asciiclient.
   * It establishes the connection to the server and opens a listening
   * port.
   * It uses requestTransfer, sendMessage and commitMessage to post a
   * unicode message to the server.
   *
   * @param dst  The IP of the server in ASCII representation. IPv6 MUST be
   *             supported, support for IPv4 is optional.
   * @param port The server port to connect to
   * @param nick The nickname to use in the protocol
   * @param msg  The message to send
   */
  public static void run (String dst, int port, String nick, String msg) {
    InetAddress dstIp;
    if ((dstIp = getip(dst)) == null) {
      System.err.println("Could not get IP address for destination");
      return;
    }

    /*====================================TODO===================================*/
        /*
(a) "C GRNVS V:1.0" Client Steuerkanal C GRNVS V:1.0
(b) "S GRNVS V:1.0" Server Steuerkanal S GRNVS V:1.0
(c) "C <nick>" Client Steuerkanal C Neo
(d) "S <token>" Server Steuerkanal S W@:JFKXT
(e) "C <dport>" Client Steuerkanal C 4242
(f) "T GRNVS V:1.0" Server Datenkanal T GRNVS V:1.0
(g) "D <nick>" Client Datenkanal D Neo
(h) "T <token>" Server Datenkanal T W@:JFKXT
(i) "D <msg>" Client Datenkanal D Help?
(j) "T <dtoken>" Server Datenkanal T E:9A0SLY
(k) "S <msglen>" Server Steuerkanal S 5
(l) "C <dtoken>" Client Steuerkanal C E:9A0SLY
(m) "S ACK" Server Steuerkanal S ACK
        * */
    Socket socket = null;
    PrintWriter out = null;
    InputStream in = null;
    try {
      socket = new Socket(getip(dst), port);
      in = socket.getInputStream();
      out = new PrintWriter(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    String ret = null;
    //1
    out.print(new Netstrings("C GRNVS V:1.0").toString());
    out.flush();
    ret = readUntilComma(in);
    try {
      if (!Netstrings.fromString(ret).getSData().equals("GRNVS V:1.0")) {
        System.out.println("Error: invalid message: " + Netstrings.fromString(ret).getSData());
        return;
      }
      //    System.out.println("1 finished");
      //2
      out.print(new Netstrings("C " + nick).toString());
      out.flush();
      ret = readUntilComma(in);
      //    System.out.println("ret" + ret);
      String sToken = Netstrings.fromString(ret).getSData();
      //    System.out.println("2 finished");
      //3build server
      ServerSocket myServer = null;
      Socket myServerSocket = null;
      PrintWriter mout = null;
      InputStream min = null;
      try {
        myServer = new ServerSocket(24427);
      } catch (IOException e) {
        e.printStackTrace();
      }
      //    System.out.println("3 finished");
      //    System.out.println(myServer.getInetAddress().getHostAddress());
      //4ready
      out.print(new Netstrings("C " + 24427).toString());
      out.flush();
      //    System.out.println("4 finished");
      //5data channel
      try {
        myServerSocket = myServer.accept();
        min = myServerSocket.getInputStream();
        mout = new PrintWriter(myServerSocket.getOutputStream());
      } catch (IOException e) {
        e.printStackTrace();
      }
      ret = readUntilComma(min);
      if (!Netstrings.fromString(ret).getTData().equals("GRNVS V:1.0")) {
        System.out.println("Error: invalid message: " + Netstrings.fromString(ret).getTData());
        return;
      }
      //    System.out.println("5 finished");
      //6D <nick>
      mout.print(new Netstrings("D " + nick).toString());
      mout.flush();
      ret = readUntilComma(min);
      if (!Netstrings.fromString(ret).getTData().equals(sToken)) {
        System.out.println("Error: invalid message: " + Netstrings.fromString(ret).getTData());
        return;
      }
      //    System.out.println("6 finished");
      //    D <msg>
      mout.print(new Netstrings("D " + msg).toString());
      mout.flush();
      //    T <dtoken>
      ret = readUntilComma(min);
      String dtoken = Netstrings.fromString(ret).getTData();
      //close
      //    try {
      //      Thread.sleep(1000);
      //    } catch (InterruptedException e) {
      //      e.printStackTrace();
      //    }
      if (readUntilComma(min) == null) {
        try {
          myServer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //    S <msglen>
      ret = readUntilComma(in);
      if (Integer.parseInt(Netstrings.fromString(ret).getSData()) != msg.length()) {
        System.out.println("Error: invalid message: " + Netstrings.fromString(ret).getSData());
        return;
      }
      //    C <dtoken>
      out.print(new Netstrings("C " + dtoken));
      out.flush();
      //    S ACK
      ret = readUntilComma(in);
      if (!Netstrings.fromString(ret).getSData().equals("ACK")) {
        System.out.println("Error: invalid message: " + Netstrings.fromString(ret).getSData());
        return;
      }
      //    try {
      //      Thread.sleep(1000);
      //    } catch (InterruptedException e) {
      //      e.printStackTrace();
      //    }
      if (readUntilComma(in) == null) {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (NetstringsFormatWrongException pp) {
      System.out.println(pp.getMessage());
      return;
    }

    /*===========================================================================*/
  }

  /**
   * read the data with data length and check if the last one is comma.
   *
   * @param in
   * @return
   */
  private static String readUntilComma (InputStream in) {
    System.err.println("newReaduntilComma");
    int readLen = readLen(in);
    System.err.println("len"+readLen);
    if (-1 == readLen) {
      return null;
    }
    String header=""+readLen+":";
    readLen++;
    try {
      int charOne = 0;
      StringBuffer sb = new StringBuffer();
      while ((charOne = in.read()) != -1 && readLen > 0) {

        readLen--;
        if (readLen == 0) {
          if (charOne != ',') {
            throw new NetstringsFormatWrongException("Error: invalid message: read Netstrings data end without comma");
          } else {
            return header+sb.toString();
          }
        }
        sb.append((char) charOne);
        System.err.print(((char) charOne) + ";");
      }
      System.err.println("-1!!!");
      System.err.println("sb" + sb.toString());
    } catch (IOException e) {
      throw new NetstringsFormatWrongException("Error: invalid message: read Netstrings data wrong");
    }
    return null;
  }

  private static int readLen (InputStream in) {
    //1
    try {
      boolean start = true;
      int charOne = 0;
      StringBuffer sb = new StringBuffer();
      while ((charOne = in.read()) != -1) {
        if (charOne != ':') {
          if (start){
            if(charOne=='0'){
              throw new NetstringsFormatWrongException("Error: invalid message: start with 0");
            }else {
              start=false;
            }
          }
          sb.append((char) charOne);
          System.err.print(((char) charOne) + ";");
        } else {
          return Integer.parseInt(sb.toString());
        }
      }
      System.err.println("-1!!!");
      System.err.println("sb" + sb.toString());
    } catch (IOException e) {
      throw new NetstringsFormatWrongException("Error: invalid message: read Netstrings len wrong");
    }
    return -1;
  }

  public static void main (String[] argv) {
    Arguments args = new Arguments(argv);
    String msg = args.msg;
    if (args.file != null) {
      try {
        msg = new String(Files.readAllBytes(Paths.get(args.file)));
      } catch (IOException e) {
        System.err.format("Could not open the file: %s\n", e.getMessage());
        return;
      }
    }
    try {
      run(args.dst, args.port, args.nick, msg);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

}
