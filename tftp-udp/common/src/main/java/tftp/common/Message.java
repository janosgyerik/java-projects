package tftp.common;

public interface Message {

  Opcode opcode();

  default String path() {
    throw new UnsupportedOperationException("This message doesn't have path (check opcode first!)");
  }

  default byte[] data() {
    throw new UnsupportedOperationException("This message doesn't have data (check opcode first!)");
  }

  default byte errorCode() {
    throw new UnsupportedOperationException("This message doesn't have errorCode (check opcode first!)");
  }

  default String errorMessage() {
    throw new UnsupportedOperationException("This message doesn't have errorMessage (check opcode first!)");
  }
}
