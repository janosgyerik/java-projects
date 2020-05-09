package tftp.common.channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.Message;
import tftp.common.Opcode;

public class SyncChannel extends AbstractChannel {

  private static final Logger LOG = LoggerFactory.getLogger(SyncChannel.class);

  public SyncChannel(DatagramSocket socket, DatagramPacket packet) {
    super(socket, packet);
  }

  private Message receiveMessage() {
    try {
      packet.setData(new byte[MAX_PACKET_LENGTH]);
      socket.setSoTimeout(TIMEOUT_SECONDS * 1000);
      socket.receive(packet);
    } catch (SocketTimeoutException e) {
      LOG.error("Socket timed out while waiting for packet");
      return null;
    } catch (IOException e) {
      LOG.error("I/O error while receiving packet");
      return null;
    }

    Message message = messageParser.parse(packet);
    if (message == null) {
      LOG.error("Invalid packet from peer");
      return null;
    }

    return message;
  }

  @Override
  public boolean receiveAck(int blockNum) {
    final Message message = receiveMessage();
    if (message == null) {
      return false;
    }

    if (!isExpectedAck(message, blockNum)) {
      return false;
    }

    LOG.info("Received ACK {}", message.blockNum());
    return true;
  }

  protected Message receiveData() {
    final Message message = receiveMessage();
    if (message == null) {
      return null;
    }

    if (message.opcode() != Opcode.DATA) {
      LOG.error("Expected DATA; got: {}", message);
      return null;
    }

    LOG.info("Received {}", message);
    return message;
  }
}
