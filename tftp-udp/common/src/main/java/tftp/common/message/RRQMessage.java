package tftp.common.message;

import static tftp.common.Opcode.RRQ;

class RRQMessage extends PathMessage {
  RRQMessage(String path) {
    super(RRQ, path);
  }
}
