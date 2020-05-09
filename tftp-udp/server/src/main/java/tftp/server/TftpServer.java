package tftp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.Message;
import tftp.common.MessageParser;
import tftp.common.channel.AsyncChannel;

import static tftp.common.channel.Channel.MAX_PACKET_LENGTH;

public class TftpServer {

  private static final Logger LOG = LoggerFactory.getLogger(TftpServer.class);

  private final MessageParser messageParser = new MessageParser();
  private final int port;

  private volatile boolean stop = false;

  public TftpServer(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    LOG.info("Starting server on port {} ...", port);

    Map<String, AsyncChannel> channels = new HashMap<>();

    try (DatagramSocket socket = new DatagramSocket(port)) {
      while (!stop) {
        // a buffer big enough for all valid operations
        byte[] buffer = new byte[MAX_PACKET_LENGTH];

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
          socket.receive(packet);
        } catch (SocketTimeoutException e) {
          LOG.error("Socket timed out while waiting for packet");
          // reset timeout to infinity
          socket.setSoTimeout(0);
          continue;
        } catch (IOException e) {
          LOG.error("I/O error while receiving data: {}", e.getMessage(), e);
          continue;
        }

        String key = computeKey(packet);
        Message message = messageParser.parse(packet);

        if (message == null) {
          LOG.error("Invalid packet from peer {}", key);
          continue;
        }

        switch (message.opcode()) {
          case RRQ: {
            if (channels.containsKey(key)) {
              channels.remove(key).shutdown();
            }
            final AsyncChannel channel = new AsyncChannel(socket, packet);
            channels.put(key, channel);
            new Thread(() -> channel.sendFile(message.path())).start();
            break;
          }

          case WRQ: {
            if (channels.containsKey(key)) {
              channels.remove(key).shutdown();
            }
            final AsyncChannel channel = new AsyncChannel(socket, packet);
            if (channel.sendAck(0)) {
              channels.put(key, channel);
              new Thread(() -> channel.receiveFile(message.path())).start();
            }
            break;
          }

          default:
            if (channels.containsKey(key)) {
              channels.get(key).receive(message);
            } else {
              LOG.error("Unexpected message {} for non-existent client {}. Ignore.", message, key);
            }
            break;
        }
      }
    }
  }

  private String computeKey(DatagramPacket packet) {
    return String.format("%s:%s", packet.getAddress().getHostName(), packet.getPort());
  }
}

