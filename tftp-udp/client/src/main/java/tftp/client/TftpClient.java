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

      byte[] rrq = payloadFactory.createRRQ(remotePath);
      DatagramPacket packet = new DatagramPacket(rrq, rrq.length, address, serverPort);

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

  public void put(String localPath, String remotePath) {

  }
}
