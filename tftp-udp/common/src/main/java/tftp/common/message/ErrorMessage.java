package tftp.common.message;

import tftp.common.Opcode;

class ErrorMessage implements Message {
  private final byte errorCode;
  private final String message;

  ErrorMessage(byte errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }

  @Override
  public Opcode opcode() {
    return Opcode.ERROR;
  }

  @Override
  public byte errorCode() {
    return errorCode;
  }

  @Override
  public String errorMessage() {
    return message;
  }
}
