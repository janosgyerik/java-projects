package tftp.common;

import java.util.Arrays;

public class MessageParser {
  public Message parse(byte[] buffer) {
    if (buffer.length < 4) {
      return null;
    }

    if (buffer[1] == Opcode.RRQ.getOpcode()) {
      return new Message(Opcode.RRQ, extractFilename(buffer), null);
    }

    return null;
  }

  private String extractFilename(byte[] buffer) {
    int end = 3;
    for (; end < buffer.length; end++) {
      if (buffer[end] == 0) {
        break;
      }
    }
    return new String(buffer, 2, end - 2);
  }

  public Message parse(byte[] buffer, int size) {
    if (buffer.length < 4) {
      return null;
    }
    return new Message(Opcode.DATA, null, Arrays.copyOfRange(buffer, 4, size));
  }
}
