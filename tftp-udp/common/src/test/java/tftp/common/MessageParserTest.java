package tftp.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageParserTest {
  private final MessageFactory factory = new MessageFactory();
  private final MessageParser underTest = new MessageParser();

  @Test
  void parse_RRQ() {
    String path = "sample.txt";
    byte[] buffer = factory.createRRQ(path);
    Message rrq = underTest.parse(buffer);
    assertThat(rrq.opcode()).isEqualTo(Opcode.RRQ);
    assertThat(rrq.path()).isEqualTo(path);
    assertThat(rrq.data()).isNull();
  }
}
