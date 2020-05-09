package tftp.common;

public interface Message {

  Opcode opcode();

  default String path() {
    throw new UnsupportedOperationException("This message doesn't have path (check opcode first!)");
  }

  default int blockNum() {
    throw new UnsupportedOperationException("This message doesn't have blockNum (check opcode first!)");
  }

  default byte[] data() {
    throw new UnsupportedOperationException("This message doesn't have data (check opcode first!)");
  }
}
