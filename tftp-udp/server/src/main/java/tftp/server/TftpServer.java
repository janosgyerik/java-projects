package tftp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.Channel;
import tftp.common.Message;
import tftp.common.MessageParser;

import static tftp.common.Opcode.RRQ;
import static tftp.common.Opcode.WRQ;

public class TftpServer {

  private static final Logger LOG = LoggerFactory.getLogger(TftpServer.class);

  private final MessageParser messageParser = new MessageParser();
  private final int port;

  private volatile boolean stop = false;

  public TftpServer(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    try (DatagramSocket socket = new DatagramSocket(port)) {
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
          LOG.error("Invalid packet from peer");
        } else if (message.opcode() == RRQ) {
          new Channel(socket, packet).sendFile(message.path());
        } else if (message.opcode() == WRQ) {
          final Channel channel = new Channel(socket, packet);
          channel.sendAck(0);
          channel.receiveFile(message.path());
        } else {
          LOG.error("Unexpected opcode {}, ignoring packet", message.opcode());
        }
      }
    }
  }
}

