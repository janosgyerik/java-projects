package tftp.server;

import java.io.IOException;

public class TftpServerCli {

  public static void main(String[] args) throws IOException {
    Params params = parseArgs(args);
    new TftpServer(params.port).start();
  }

  private static Params parseArgs(String[] args) {
    int port = 2000;
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h":
        case "--help":
          printUsage();
          System.exit(0);
        case "-p":
        case "--port":
          port = Integer.parseInt(args[i+1]);
          i++;
          break;
      }
    }

    return new Params(port);
  }

  private static void printUsage() {
    System.out.println("usage: java -jar tftp-server.jar [-|--help] [-p|--port PORT]");
  }

  private static class Params {
    private final int port;

    private Params(int port) {
      this.port = port;
    }
  }
}
