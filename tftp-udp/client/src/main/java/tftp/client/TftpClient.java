package tftp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.PayloadFactory;
import tftp.common.message.Channel;

public class TftpClient {

  private static final Logger LOG = LoggerFactory.getLogger(TftpClient.class);

  private final PayloadFactory payloadFactory = new PayloadFactory();
  private final int serverPort;

  public TftpClient(int serverPort) {
    this.serverPort = serverPort;
  }

  public void get(String remotePath, String localPath) throws SocketException, UnknownHostException {
    try (DatagramSocket socket = new DatagramSocket(serverPort + 1)) {
      InetAddress address = InetAddress.getByName("localhost");

      byte[] bytes = payloadFactory.createRRQ(remotePath);
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, serverPort);

      try {
        LOG.info("Sending RRQ for path '{}'...", remotePath);
        socket.send(packet);
      } catch (IOException e) {
        LOG.error("Sending RRQ failed: {}", e.getMessage(), e);
        return;
      }

      new Channel(socket, packet).receiveFile(localPath);
    }
  }

  public void put(String localPath, String remotePath) throws SocketException, UnknownHostException {
    try (DatagramSocket socket = new DatagramSocket(serverPort + 1)) {
      InetAddress address = InetAddress.getByName("localhost");

      byte[] bytes = payloadFactory.createWRQ(remotePath);
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, serverPort);

      try {
        LOG.info("Sending WRQ for path '{}'...", remotePath);
        socket.send(packet);
      } catch (IOException e) {
        LOG.error("Sending WRQ failed: {}", e.getMessage(), e);
        return;
      }

      new Channel(socket, packet).sendFile(localPath);
    }
  }
}
