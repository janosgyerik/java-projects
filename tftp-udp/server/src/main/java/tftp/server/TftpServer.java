package tftp.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.message.Message;
import tftp.common.PayloadFactory;
import tftp.common.message.MessageParser;
import tftp.common.ErrorCode;

import static tftp.common.Opcode.RRQ;

public class TftpServer {

  private static final Logger LOG = LoggerFactory.getLogger(TftpServer.class);

  private final MessageParser messageParser = new MessageParser();
  private final PayloadFactory payloadFactory = new PayloadFactory();
  private final int port;

  private DatagramSocket socket;

  private volatile boolean stop = false;

  public TftpServer(int port) {
    this.port = port;
  }

  private void sendError(DatagramPacket packet, ErrorCode error, String message) {
    packet.setData(payloadFactory.createError(error, message));
    try {
      socket.send(packet);
    } catch (IOException e) {
      LOG.error("I/O error while sending ERROR: {}", e.getMessage(), e);
    }
  }

  private void sendData(DatagramPacket packet, byte[] data, int size) throws IOException {
    LOG.info("Sending data of {} bytes...", size);
    packet.setData(payloadFactory.createData(0, data, size));
    socket.send(packet);
  }

  private void sendFile(DatagramPacket packet, String path) {
    LOG.info("Sending file '{}'...", path);
    try (InputStream inputStream = new FileInputStream(path)) {
      byte[] data = new byte[512];
      while (true) {
        int size = inputStream.read(data);
        if (size == -1) {
          LOG.info("Sending file '{}' done!", path);
          break;
        }

        sendData(packet, data, size);

        // TODO
        //receiveAck(packet);
      }
    } catch (IOException e) {
      LOG.error("Error while sending data: {}", e.getMessage(), e);
      sendError(packet, ErrorCode.NOT_DEFINED, e.getMessage());
    }
  }

  public void start() throws SocketException {
    try (DatagramSocket socket = new DatagramSocket(port)) {
      this.socket = socket;

      // a buffer big enough for all operations
      byte[] buffer = new byte[516];

      while (!stop) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
          socket.receive(packet);
        } catch (IOException e) {
          LOG.error("I/O error while receiving data: {}", e.getMessage(), e);
          continue;
        }

        Message message = messageParser.parse(packet);

        if (message == null) {
          LOG.error("Invalid packet from client");
        } else if (message.opcode() == RRQ) {
          sendFile(packet, message.path());
        } else {
          LOG.error("Unexpected opcode {}, ignoring packet", message.opcode());
        }
      }
    }
  }
}

