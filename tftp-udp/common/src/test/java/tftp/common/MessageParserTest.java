package tftp.common;

import java.net.DatagramPacket;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageParserTest {
  private final PayloadFactory factory = new PayloadFactory();
  private final MessageParser underTest = new MessageParser();

  @Test
  void parse_RRQ() {
    String path = "sample.txt";
    byte[] bytes = factory.createRRQ(path);
    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
    Message rrq = underTest.parse(packet);
    assertThat(rrq.opcode()).isEqualTo(Opcode.RRQ);
    assertThat(rrq.path()).isEqualTo(path);
  }
}
