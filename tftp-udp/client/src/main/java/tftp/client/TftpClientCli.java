package tftp.client;

import java.net.SocketException;
import java.net.UnknownHostException;

public class TftpClientCli {

  public static void main(String[] args) throws SocketException, UnknownHostException {
    int port = 2000;
    TftpClient client = new TftpClient();
    client.connect(port);
    client.get("/tmp/sample.txt", "/tmp/sample.txt.copy");
  }
}
