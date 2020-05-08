package tftp.client;

import java.net.SocketException;
import java.net.UnknownHostException;

public class TftpClientCli {

  public static void main(String[] args) throws SocketException, UnknownHostException {
    int port = 2000;
    TftpClient client = new TftpClient(port);
    switch (args[0]) {
      case "get":
        client.get(args[1], args[2]);
        break;
      case "put":
        client.put(args[1], args[2]);
        break;
      default:
        System.err.println("usage: java -jar tftp.jar CMD [ARGS]");
        System.err.println("");
        System.err.println("Supported commands:");
        System.err.println("");
        System.err.println("get remotePath localPath");
        System.err.println("put localPath remotePath");
        System.err.println("");
    }
  }
}
