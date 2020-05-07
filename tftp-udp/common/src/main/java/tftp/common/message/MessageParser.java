package tftp.common.message;

import java.net.DatagramPacket;
import java.util.Arrays;
import tftp.common.Opcode;

public class MessageParser {

  public Message parse(DatagramPacket packet) {
    byte[] bytes = packet.getData();
    // less than or equal to bytes.length
    int payloadSize = packet.getLength();

    if (payloadSize < 4) {
      // all valid packets must have at least 4 bytes!
      return null;
    }

    final byte opcode = bytes[1];
    if (opcode == Opcode.RRQ.opcode()) {
      return new RRQMessage(extractStringAt(bytes, 2));
    }

    if (opcode == Opcode.WRQ.opcode()) {
      return new WRQMessage(extractStringAt(bytes, 2));
    }

    if (opcode == Opcode.DATA.opcode()) {
      return new DataMessage(Arrays.copyOfRange(bytes, 4, payloadSize));
    }

    if (opcode == Opcode.ERROR.opcode()) {
      return new ErrorMessage(bytes[3], extractStringAt(bytes, 4));
    }

    return null;
  }

  private String extractStringAt(byte[] bytes, int start) {
    int end = start + 1;
    for (; end < bytes.length; end++) {
      if (bytes[end] == 0) {
        break;
      }
    }
    return new String(bytes, 2, end - 2);
  }
}
