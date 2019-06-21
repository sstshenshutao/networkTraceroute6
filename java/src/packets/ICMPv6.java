package packets;

public class ICMPv6 extends OptionalPart {
  //general Format by Rfc 4443

  byte[] type; //8
  byte[] code;  //8
  byte[] checksum;  //16

  @Override
  public byte[] dump () {
    return Util.mergeBytes(type, code, checksum);
  }

  @Override
  public String toString () {
    String ret = "";
    ret += Util.byteArraytoString(type, 2);
    ret += Util.byteArraytoString(code, 2);
    ret += Util.byteArraytoString(checksum, 2);
    return ret;
  }

  public byte[] getType () {
    return type;
  }

  public void setType (byte[] type) {
    this.type = type;
  }

  public byte[] getCode () {
    return code;
  }

  public void setCode (byte[] code) {
    this.code = code;
  }

  public byte[] getChecksum () {
    return checksum;
  }

  public void setChecksum (byte[] checksum) {
    this.checksum = checksum;
  }

  public static ICMPv6 parse (byte[] data,byte[] srcIP,byte[] dstIP) {
    ICMPv6 icmPv6=null;
    switch (data[0]&0xff){
      case 3:
        //time exceeded
        icmPv6= new ICMPv6TimeExceeded();
        icmPv6.type=new byte[]{data[0]};
        icmPv6.code=new byte[]{data[1]};
        icmPv6.checksum=new byte[]{data[2],data[3]};
        ((ICMPv6TimeExceeded) icmPv6).unused=new byte[]{data[4],data[5],data[6],data[7]};
        byte[] ipv6 = new byte[data.length-8];
        System.arraycopy(data, 8, ipv6, 0, data.length-8);
        ((ICMPv6TimeExceeded) icmPv6).setiPv6Packet(IPv6Packet.parse(ipv6));
        break;
      case 1:
        //destination unreachable
        icmPv6= new ICMPv6DestinationUnreachable();
        icmPv6.type=new byte[]{data[0]};
        icmPv6.code=new byte[]{data[1]};
        icmPv6.checksum=new byte[]{data[2],data[3]};
        ((ICMPv6DestinationUnreachable) icmPv6).unused=new byte[]{data[4],data[5],data[6],data[7]};
        ipv6 = new byte[data.length-8];
        System.arraycopy(data, 8, ipv6, 0, data.length-8);
        ((ICMPv6DestinationUnreachable) icmPv6).setiPv6Packet(IPv6Packet.parse(ipv6));
        break;
      case 129:
        //echo reply
        icmPv6= new ICMPv6EchoReply();
        icmPv6.type=new byte[]{data[0]};
        icmPv6.code=new byte[]{data[1]};
        icmPv6.checksum=new byte[]{data[2],data[3]};
        ((ICMPv6EchoReply) icmPv6).identifier=new byte[]{data[4],data[5]};
        ((ICMPv6EchoReply) icmPv6).sequenceNumber=new byte[]{data[6],data[7]};
        break;
      case 128:
        icmPv6= new ICMPv6EchoRequest();
        icmPv6.type=new byte[]{data[0]};
        icmPv6.code=new byte[]{data[1]};
        icmPv6.checksum=new byte[]{data[2],data[3]};
        ((ICMPv6EchoRequest) icmPv6).identifier=new byte[]{data[4],data[5]};
        ((ICMPv6EchoRequest) icmPv6).sequenceNumber=new byte[]{data[6],data[7]};
        break;
      default:
        return null;
    }
    return icmPv6;

  }

}
