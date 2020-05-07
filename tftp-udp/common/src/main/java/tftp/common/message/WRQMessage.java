package tftp.common.message;

import static tftp.common.Opcode.WRQ;

class WRQMessage extends PathMessage {
  WRQMessage(String path) {
    super(WRQ, path);
  }
}
