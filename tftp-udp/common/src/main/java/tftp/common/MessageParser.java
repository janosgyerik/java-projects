package tftp.common;

import java.net.DatagramPacket;
import java.util.Arrays;

import static tftp.common.Opcode.RRQ;
import static tftp.common.Opcode.WRQ;

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
      return new DataMessage(wordToInt(bytes, 2), Arrays.copyOfRange(bytes, 4, payloadSize));
    }

    if (opcode == Opcode.ACK.opcode()) {
      return new AckMessage(wordToInt(bytes, 2));
    }

    if (opcode == Opcode.ERROR.opcode()) {
      return new ErrorMessage(bytes[3], extractStringAt(bytes, 4));
    }

    return null;
  }

  private static int wordToInt(byte[] bytes, int start) {
    // The "& 0xff" is to drop the sign bits
    return ((bytes[start] & 0xff) << 8) + (bytes[start + 1] & 0xff);
  }

  private static String extractStringAt(byte[] bytes, int start) {
    int end = start + 1;
    for (; end < bytes.length; end++) {
      if (bytes[end] == 0) {
        break;
      }
    }
    return new String(bytes, 2, end - 2);
  }

  private abstract static class PathMessage implements Message {
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

  private static class RRQMessage extends PathMessage {
    RRQMessage(String path) {
      super(RRQ, path);
    }
  }

  private static class WRQMessage extends PathMessage {
    WRQMessage(String path) {
      super(WRQ, path);
    }
  }

  private static class DataMessage implements Message {
    private final int blockNum;
    private final byte[] data;

    DataMessage(int blockNum, byte[] data) {
      this.blockNum = blockNum;
      this.data = data;
    }

    @Override
    public Opcode opcode() {
      return Opcode.DATA;
    }

    @Override
    public int blockNum() {
      return blockNum;
    }

    @Override
    public byte[] data() {
      return data;
    }
  }

  private static class ErrorMessage implements Message {
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

  private static class AckMessage implements Message {
    private final int blockNum;

    AckMessage(int blockNum) {
      this.blockNum = blockNum;
    }

    @Override
    public Opcode opcode() {
      return Opcode.ACK;
    }

    @Override
    public int blockNum() {
      return blockNum;
    }
  }
}
