package tftp.common;

public class MessageFactory {
  public byte[] createData(int blockNum, byte[] data, int size) {
    byte[] bytes = new byte[2 + 2 + size];
    bytes[1] = Opcode.DATA.getOpcode();
    bytes[2] = (byte) (blockNum >> 8);
    bytes[3] = (byte) (blockNum & 0xff);
    System.arraycopy(data, 0, bytes, 4, size);
    return bytes;
  }

  public byte[] createRRQ(String path) {
    byte[] pathBytes = path.getBytes();
    byte[] modeBytes = "octet".getBytes();
    byte[] bytes = new byte[2 + pathBytes.length + 1 + modeBytes.length + 1];
    bytes[1] = Opcode.RRQ.getOpcode();
    System.arraycopy(pathBytes, 0, bytes, 2, pathBytes.length);
    System.arraycopy(modeBytes, 0, bytes, 2 + pathBytes.length + 1, modeBytes.length);
    return bytes;
  }
}
