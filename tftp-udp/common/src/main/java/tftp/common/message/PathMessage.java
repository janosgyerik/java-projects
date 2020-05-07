package tftp.common.message;

import tftp.common.Opcode;

abstract class PathMessage implements Message {
  private final Opcode opcode;
  private final String path;

  PathMessage(Opcode opcode, String path) {
    this.opcode = opcode;
    this.path = path;
  }

  @Override
  public Opcode opcode() {
    return opcode;
  }

  @Override
  public String path() {
    return path;
  }
}
