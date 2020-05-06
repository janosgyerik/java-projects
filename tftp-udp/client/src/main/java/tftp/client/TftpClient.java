package tftp.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tftp.common.Message;
import tftp.common.MessageFactory;
import tftp.common.MessageParser;
import tftp.common.Opcode;

public class TftpClient {

  private static final Logger LOG = LoggerFactory.getLogger(TftpClient.class);

  private final MessageParser messageParser = new MessageParser();
  private final MessageFactory messageFactory = new MessageFactory();
  private int serverPort;
  private DatagramSocket socket;

  public void connect(int serverPort) {
    this.serverPort = serverPort;
  }

  public void get(String remotePath, String localPath) throws SocketException, UnknownHostException {
    try (DatagramSocket socket = new DatagramSocket(serverPort + 1)) {
      this.socket = socket;

      InetAddress address = InetAddress.getByName("localhost");

      byte[] rrq = messageFactory.createRRQ(remotePath);
      DatagramPacket packet = new DatagramPacket(rrq, rrq.length, address, serverPort);

      try {
        LOG.info("Sending RRQ for path '{}'...", remotePath);
        socket.send(packet);
      } catch (IOException e) {
        LOG.error("Sending RRQ failed: {}", e.getMessage(), e);
        return;
      }

      receiveData(packet, localPath);
    }
  }

  private void receiveData(DatagramPacket packet, String path) {
    try (FileOutputStream ostream = new FileOutputStream(path)) {
      packet.setData(new byte[516]);

      while (true) {
        socket.receive(packet);

        Message message = messageParser.parse(packet.getData(), packet.getLength());
        if (message == null) {
          String msg = "Invalid packet from server";
          LOG.error(msg);
          throw new IllegalStateException(msg);
        } else if (message.opcode() == Opcode.DATA) {
          byte[] data = message.data();
          ostream.write(data);

          // TODO send ack

          if (data.length < 512) {
            break;
          }
        } else if (message.opcode() == Opcode.ERROR) {
          LOG.error("Received error from server. Aborting download.");
          break;
        } else {
          String msg = "Unexpected opcode: " + message.opcode();
          LOG.error(msg);
          throw new IllegalStateException(msg);
        }
      }
    } catch (IOException e) {
      LOG.error("I/O error while receiving data from server. Aborting download.", e);
    }
  }
}