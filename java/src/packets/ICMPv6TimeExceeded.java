package packets;

public class ICMPv6TimeExceeded extends ICMPv6 {

  byte[] unused; //32
  IPv6Packet iPv6Packet;//for our cases only this packet

  @Override
  public byte[] dump () {
    return Util.mergeBytes(super.dump(), unused, iPv6Packet.dump());
  }

  @Override
  public String toString () {
    String ret = super.toString();
    ret += Util.byteArraytoString(unused, 2);
    ret += iPv6Packet.toString();
    return ret;
  }

  public byte[] getUnused () {
    return unused;
  }

  public void setUnused (byte[] unused) {
    this.unused = unused;
  }

  public IPv6Packet getiPv6Packet () {
    return iPv6Packet;
  }

  public void setiPv6Packet (IPv6Packet iPv6Packet) {
    this.iPv6Packet = iPv6Packet;
  }

}
