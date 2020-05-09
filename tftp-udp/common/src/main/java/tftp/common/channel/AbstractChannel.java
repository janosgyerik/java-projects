package tftp.common.channel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.ErrorCode;
import tftp.common.Message;
import tftp.common.MessageParser;
import tftp.common.Opcode;
import tftp.common.PayloadFactory;

abstract class AbstractChannel implements Channel {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractChannel.class);

  protected static final int TIMEOUT_SECONDS = 1;

  protected final MessageParser messageParser = new MessageParser();
  private final PayloadFactory payloadFactory = new PayloadFactory();

  protected final DatagramSocket socket;
  protected final DatagramPacket packet;

  private volatile boolean stop = false;

  public AbstractChannel(DatagramSocket socket, DatagramPacket packet) {
    this.socket = socket;
    this.packet = packet;
  }

  @Override
  public abstract boolean receiveAck(int blockNum);

  protected abstract Message receiveData();

  protected boolean isExpectedAck(Message message, int blockNum) {
    if (message.opcode() != Opcode.ACK) {
      LOG.error("Expected ACK with blockNum = {}; got: {}", blockNum, message);
      return false;
    }

    if (message.blockNum() != blockNum) {
      LOG.error("Expected ACK with blockNum = {}; got: {}", blockNum, message.blockNum());
      return false;
    }
    return true;
  }

  public boolean sendAck(int blockNum) {
    LOG.info("Sending ACK {} ...", blockNum);
    packet.setData(payloadFactory.createAck(blockNum));
    try {
      socket.send(packet);
      return true;
    } catch (IOException e) {
      LOG.error("I/O error while sending ACK {}", blockNum);
      return false;
    }
  }

  private void sendError(ErrorCode error, String message) {
    packet.setData(payloadFactory.createError(error, message));
    try {
      socket.send(packet);
    } catch (IOException e) {
      LOG.error("I/O error while sending ERROR: {}", e.getMessage(), e);
    }
  }

  private void sendData(int blockNum, byte[] data, int size) throws IOException {
    packet.setData(payloadFactory.createData(blockNum, data, size));
    socket.send(packet);
  }

  @Override
  public void sendFile(String path) {
    try (InputStream inputStream = new FileInputStream(path)) {
      int blockNum = 1;
      byte[] data = new byte[512];
      while (!stop) {
        int size = inputStream.read(data);
        if (size == -1) {
          LOG.info("Sending file '{}' done!", path);
          break;
        }

        LOG.info("Sending {} bytes of '{}' ...", size, path);
        try {
          sendData(blockNum, data, size);
        } catch (IOException e) {
          LOG.error("I/O error while sending data: {}", e.getMessage(), e);
          sendError(ErrorCode.NOT_DEFINED, e.getMessage());
          return;
        }

        if (!receiveAck(blockNum)) {
          sendError(ErrorCode.NOT_DEFINED, "Did not receive ACK for " + blockNum);
          return;
        }

        // max 2 bytes for blockNum -> wrap around after 0xffff
        blockNum = (blockNum + 1) & 0xffff;
      }
    } catch (FileNotFoundException e) {
      LOG.error("File not found: {}", path);
      sendError(ErrorCode.FILE_NOT_FOUND, e.getMessage());
    } catch (IOException e) {
      LOG.error("I/O error while sending file: {}", e.getMessage(), e);
      sendError(ErrorCode.NOT_DEFINED, e.getMessage());
    }
  }

  public void receiveFile(String localPath) {
    try (FileOutputStream out = new FileOutputStream(localPath)) {
      while (!stop) {
        Message message = receiveData();
        if (message == null) {
          return;
        }

        byte[] data = message.data();
        out.write(data);

        sendAck(message.blockNum());

        if (data.length < 512) {
          break;
        }
      }
    } catch (IOException e) {
      LOG.error("I/O error while receiving data: {}", e.getMessage(), e);
      sendError(ErrorCode.NOT_DEFINED, e.getMessage());
    }
  }

  public void shutdown() {
    // stop receiving or sending data on this channel
    stop = true;
  }
}
