package parser.exceptions;

public class PacketFormatWrongException extends RuntimeException {

  private String description;

  public PacketFormatWrongException (String description) {
    this.description = description;
  }

  public String getDescription () {
    return description;
  }

  public void setDescription (String description) {
    this.description = description;
  }

  @Override
  public String toString () {
    return "PacketFormatWrongException{}:" + description;
  }

}
