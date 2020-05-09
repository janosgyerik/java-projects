package tftp.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TftpClientCli {

  public static void main(String[] args) throws IOException {
    Params params = parseArgs(args);
    if (params.args.isEmpty()) {
      printUsage();
      System.exit(1);
    }

    TftpClient client = new TftpClient(params.serverHost, params.serverPort, params.clientPort);

    final String command = params.args.get(0);
    if (command.equals("get")) {
      client.get(params.args.get(1), params.args.get(2));
    } else if (command.equals("put")) {
      client.put(params.args.get(1), params.args.get(2));
    } else {
      throw new UnsupportedOperationException("Unsupported command: " + command);
    }
  }

  private static Params parseArgs(String[] args) {
    String serverHost = "localhost";
    int serverPort = 2000;
    int clientPort = serverPort + 1;
    List<String> cmdWithArgs = new ArrayList<>();

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h":
        case "--help":
          printUsage();
          System.exit(0);

        case "--host":
          serverHost = args[i + 1];
          i++;
          break;

        case "-p":
        case "--port":
          serverPort = Integer.parseInt(args[i + 1]);
          i++;
          break;

        case "--client-port":
          clientPort = Integer.parseInt(args[i + 1]);
          i++;
          break;

        default:
          cmdWithArgs.add(args[i]);
      }
    }

    return new Params(serverHost, serverPort, clientPort, cmdWithArgs);
  }

  private static void printUsage() {
    System.err.println("usage: java -jar tftp-client.jar [OPTION]... COMMAND...");
    System.err.println("");
    System.err.println("Supported commands:");
    System.err.println("");
    System.err.println("get remotePath localPath");
    System.err.println("put localPath remotePath");
    System.err.println("");
    System.err.println("Supported options:");
    System.err.println("");
    System.err.println(" -h, --help");
    System.err.println("     --host SERVER-HOST");
    System.err.println(" -p, --port SERVER-PORT");
    System.err.println("     --client-port PORT");
    System.err.println("");
  }

  private static class Params {
    private final String serverHost;
    private final int serverPort;
    private final int clientPort;
    private final List<String> args;

    private Params(String serverHost, int serverPort, int clientPort, List<String> args) {
      this.serverHost = serverHost;
      this.serverPort = serverPort;
      this.clientPort = clientPort;
      this.args = args;
    }
  }
}
