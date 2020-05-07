package tftp.common;

// https://tools.ietf.org/html/rfc1350
public enum Opcode {
  RRQ(1),
  WRQ(2),
  DATA(3),
  ACK(4),
  ERROR(5);

  private final byte opcode;

  Opcode(int opcode) {
    this.opcode = (byte) opcode;
  }

  public byte opcode() {
    return opcode;
  }
}
