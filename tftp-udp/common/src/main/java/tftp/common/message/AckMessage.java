package tftp.common.message;

import tftp.common.Opcode;

class AckMessage implements Message {
  @Override
  public Opcode opcode() {
    return Opcode.ACK;
  }
}
