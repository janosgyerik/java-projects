package tftp.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Channel {
  private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

  public static final int MAX_PACKET_LENGTH = 516;

  private static final int TIMEOUT_SECONDS = 1;

  private final MessageParser messageParser = new MessageParser();
  private final PayloadFactory payloadFactory = new PayloadFactory();
  private final BlockingDeque<Message> messageQueue = new LinkedBlockingDeque<>();

  private final DatagramSocket socket;
  private final DatagramPacket packet;
  private final boolean sync;

  private volatile boolean stop = false;

  public Channel(DatagramSocket socket, DatagramPacket packet, boolean sync) {
    this.socket = socket;
    this.packet = packet;
    this.sync = sync;
  }

  public void receive(Message message) {
    messageQueue.add(message);
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

  private boolean isExpectedAck(Message message, int blockNum) {
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

  private boolean awaitAck(int blockNum) {
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

  public boolean receiveAck(int blockNum) {
    if (!sync) {
      return awaitAck(blockNum);
    }

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

  private Message awaitData() {
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

  private Message receiveData() {
    if (!sync) {
      return awaitData();
    }

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
        if (message != null) {
          byte[] data = message.data();
          out.write(data);

          sendAck(message.blockNum());

          if (data.length < 512) {
            break;
          }
        } else {
          return;
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
