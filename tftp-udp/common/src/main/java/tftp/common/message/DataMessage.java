package tftp.common.message;

import tftp.common.Opcode;

class DataMessage implements Message {
  private final byte[] data;

  DataMessage(byte[] data) {
    this.data = data;
  }

  @Override
  public Opcode opcode() {
    return Opcode.DATA;
  }

  @Override
  public byte[] data() {
    return data;
  }
}
