package packets;

public class ICMPv6EchoRequest extends ICMPv6 {

  //rfc 4443
  byte[] identifier; //16
  byte[] sequenceNumber;  //16
  byte[] data;//32 * n

  public ICMPv6EchoRequest () {
    super.type = Util.dumpString("80");
    super.code = Util.dumpString("00");
    super.checksum = Util.dumpString("00 00");
  }

  @Override
  public byte[] dump () {
    if (data == null || data.length == 0) {
      return Util.mergeBytes(super.dump(), identifier, sequenceNumber);
    } else {
      return Util.mergeBytes(super.dump(), identifier, sequenceNumber, data);
    }
  }

  public byte[] getIdentifier () {
    return identifier;
  }

  public void setIdentifier (byte[] identifier) {
    this.identifier = identifier;
  }

  public byte[] getSequenceNumber () {
    return sequenceNumber;
  }

  public void setSequenceNumber (byte[] sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public byte[] getData () {
    return data;
  }

  public void setData (byte[] data) {
    this.data = data;
  }

  @Override
  public String toString () {
    String ret = super.toString();
    ret += Util.byteArraytoString(identifier, 2);
    ret += Util.byteArraytoString(sequenceNumber, 2);
    if (data != null) {
      ret += Util.byteArraytoString(data, 2);
    }
    return ret;
  }

}
