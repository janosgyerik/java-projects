package tftp.common.channel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.Message;
import tftp.common.Opcode;

public class AsyncChannel extends AbstractChannel {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncChannel.class);

  private final BlockingDeque<Message> messageQueue = new LinkedBlockingDeque<>();

  public AsyncChannel(DatagramSocket socket, DatagramPacket packet) {
    super(socket, packet);
  }

  public void receive(Message message) {
    messageQueue.add(message);
  }

  @Override
  public boolean receiveAck(int blockNum) {
    LOG.info("Waiting for ACK {} ...", blockNum);
    Message message;
    try {
      message = messageQueue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOG.error("Interrupted while waiting for ACK {}", blockNum);
      return false;
    }

    if (message == null) {
      LOG.error("Did not receive ACK {} on time", blockNum);
      return false;
    }

    if (!isExpectedAck(message, blockNum)) {
      return false;
    }

    LOG.info("Received ACK {}", message.blockNum());
    return true;
  }

  @Override
  protected Message receiveData() {
    LOG.info("Waiting for DATA ...");
    Message message;
    try {
      message = messageQueue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOG.error("Interrupted while waiting for DATA");
      return null;
    }

    if (message == null) {
      LOG.error("Did not receive DATA on time");
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
