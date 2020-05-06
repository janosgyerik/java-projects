package tftp.server;

import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TftpServerCli {

  private static final Logger LOG = LoggerFactory.getLogger(TftpServerCli.class);

  public static void main(String[] args) throws SocketException {
    int port = 2000;
    LOG.info("Starting server on port {} ...", port);
    new TftpServer(port).start();
  }
}
