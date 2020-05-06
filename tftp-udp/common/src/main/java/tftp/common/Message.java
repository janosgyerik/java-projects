package tftp.common;

public class Message {
  private final Opcode opcode;
  private final String filename;
  private final byte[] data;

  public Message(Opcode opcode, String filename, byte[] data) {
    this.opcode = opcode;
    this.filename = filename;
    this.data = data;
  }

  public Opcode opcode() {
    return opcode;
  }

  public String path() {
    return filename;
  }

  public byte[] data() {
    return data;
  }
}
