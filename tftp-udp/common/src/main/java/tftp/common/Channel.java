package tftp.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Channel {
  private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

  private final MessageParser messageParser = new MessageParser();
  private final PayloadFactory payloadFactory = new PayloadFactory();

  private final DatagramSocket socket;
  private final DatagramPacket packet;

  public Channel(DatagramSocket socket) {
    this(socket, new DatagramPacket(new byte[0], 0));
  }

  public Channel(DatagramSocket socket, DatagramPacket packet) {
    this.socket = socket;
    this.packet = packet;
  }

  private void sendAck(int blockNum) throws IOException {
    LOG.info("Sending ACK {} ...", blockNum);
    packet.setData(payloadFactory.createAck(blockNum));
    socket.send(packet);
  }

  private void receiveAck(int blockNum) throws IOException {
    LOG.info("Waiting for ACK {} ...", blockNum);
    socket.receive(packet);

    Message message = messageParser.parse(packet);

    if (message == null) {
      String msg = "Invalid packet from peer";
      LOG.error(msg);
      throw new IllegalStateException(msg);
    }

    if (message.opcode() != Opcode.ACK) {
      String msg = "Expected ACK; got: " + message.opcode();
      LOG.error(msg);
      throw new IllegalStateException(msg);
    }

    if (message.blockNum() != blockNum) {
      String msg = String.format("Expected blockNum = %s; got: %s", blockNum, message.blockNum());
      LOG.error(msg);
      throw new IllegalStateException(msg);
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
      while (true) {
        int size = inputStream.read(data);
        if (size == -1) {
          LOG.info("Sending file '{}' done!", path);
          break;
        }

        LOG.info("Sending {} bytes of '{}' ...", size, path);
        sendData(blockNum, data, size);

        receiveAck(blockNum);

        // max 2 bytes for blockNum -> wrap around after 0xffff
        blockNum = (blockNum + 1) & 0xffff;
      }
    } catch (FileNotFoundException e) {
      LOG.error("File not found: {}", path);
      sendError(ErrorCode.FILE_NOT_FOUND, e.getMessage());
    } catch (IOException e) {
      LOG.error("Error while sending data: {}", e.getMessage(), e);
      sendError(ErrorCode.NOT_DEFINED, e.getMessage());
    }
  }

  public void receiveFile(String localPath) {
    try (FileOutputStream out = new FileOutputStream(localPath)) {
      while (true) {
        packet.setData(new byte[516]);
        socket.receive(packet);

        Message message = messageParser.parse(packet);
        if (message == null) {
          String msg = "Invalid packet from peer";
          LOG.error(msg);
          throw new IllegalStateException(msg);
        } else if (message.opcode() == Opcode.DATA) {
          LOG.info("Received data of {} bytes", message.data().length);

          byte[] data = message.data();
          out.write(data);

          sendAck(message.blockNum());

          if (data.length < 512) {
            break;
          }
        } else if (message.opcode() == Opcode.ERROR) {
          LOG.error("Received error, will abort transfer: {} ({})", message.errorCode(), message.errorMessage());
          break;
        } else {
          String msg = "Unexpected opcode: " + message.opcode();
          LOG.error(msg);
          throw new IllegalStateException(msg);
        }
      }
    } catch (IOException e) {
      LOG.error("I/O error while receiving data from peer. Abort.", e);
    }
  }
}
