package tftp.common;

// See https://tools.ietf.org/html/rfc1350
public enum ErrorCode {
  NOT_DEFINED(0),
  FILE_NOT_FOUND(1),
  ACCESS_VIOLATION(2),
  DISK_FULL(3),
  ILLEGAL_OPERATION(4),
  UNKNOWN_TRANSFER_ID(5),
  FILE_EXISTS(6),
  NO_SUCH_USER(7);

  private final byte code;

  ErrorCode(int code) {
    this.code = (byte) code;
  }

  public byte code() {
    return code;
  }
}
