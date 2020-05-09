package tftp.common.channel;

public interface Channel {

  int MAX_PACKET_LENGTH = 516;

  boolean receiveAck(int blockNum);

  void sendFile(String path);
}
