package tftp.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.Message;
import tftp.common.MessageFactory;
import tftp.common.MessageParser;

import static tftp.common.Opcode.RRQ;

public class TftpServer {

  private static final Logger LOG = LoggerFactory.getLogger(TftpServer.class);

  private final MessageParser messageParser = new MessageParser();
  private final MessageFactory messageFactory = new MessageFactory();
  private final int port;

  private DatagramSocket socket;

  private volatile boolean stop = false;

  public TftpServer(int port) {
    this.port = port;
  }

  private void sendData(DatagramPacket packet, byte[] data, int size) throws IOException {
    packet.setData(messageFactory.createData(0, data, size));
    socket.send(packet);
  }

  private void sendFile(DatagramPacket packet, String path) {
    LOG.info("Try to serve file '{}'...", path);
    try (InputStream inputStream = new FileInputStream(path)) {
      byte[] data = new byte[512];
      while (true) {
        int size = inputStream.read(data);
        if (size == -1) {
          break;
        }

        sendData(packet, data, size);

        // TODO
        //receiveAck(packet);
      }
    } catch (IOException e) {
      LOG.info(e.getMessage(), e);
      // TODO
      // sendError(packet);
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
          throw new UnsupportedOperationException("TODO: handle error while receiving packet");
        }

        Message message = messageParser.parse(buffer);

        if (message == null) {
          throw new UnsupportedOperationException("TODO: handle unknown message");
        } else if (message.opcode() == RRQ) {
          sendFile(packet, message.path());
        } else {
          throw new UnsupportedOperationException("TODO: handle unsupported message");
        }
      }
    }
  }
}

