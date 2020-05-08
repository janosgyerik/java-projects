package tftp.common;

public class PayloadFactory {
  public byte[] createData(int blockNum, byte[] data, int size) {
    byte[] bytes = new byte[2 + 2 + size];
    bytes[1] = Opcode.DATA.opcode();
    bytes[2] = (byte) (blockNum >> 8);
    bytes[3] = (byte) (blockNum & 0xff);
    System.arraycopy(data, 0, bytes, 4, size);
    return bytes;
  }

  private byte[] createReadOrWriteRequest(Opcode opcode, String path) {
    byte[] pathBytes = path.getBytes();
    byte[] modeBytes = "octet".getBytes();
    byte[] bytes = new byte[2 + pathBytes.length + 1 + modeBytes.length + 1];
    bytes[1] = opcode.opcode();
    System.arraycopy(pathBytes, 0, bytes, 2, pathBytes.length);
    System.arraycopy(modeBytes, 0, bytes, 2 + pathBytes.length + 1, modeBytes.length);
    return bytes;
  }

  public byte[] createRRQ(String path) {
    return createReadOrWriteRequest(Opcode.RRQ, path);
  }

  public byte[] createWRQ(String path) {
    return createReadOrWriteRequest(Opcode.WRQ, path);
  }

  public byte[] createError(ErrorCode error, String message) {
    byte[] messageBytes = message.getBytes();
    byte[] bytes = new byte[2 + 2 + messageBytes.length + 1];
    bytes[1] = Opcode.ERROR.opcode();
    bytes[3] = error.code();
    System.arraycopy(messageBytes, 0, bytes, 4, messageBytes.length);
    return bytes;
  }
}
