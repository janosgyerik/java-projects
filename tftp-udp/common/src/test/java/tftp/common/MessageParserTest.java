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

  @Test
  void parse_ACK_0xef() {
    final int blockNum = 0xef;
    byte[] bytes = factory.createAck(blockNum);
    assertThat(bytes[2]).isEqualTo((byte) 0);
    assertThat(bytes[3]).isEqualTo((byte) 0xef);

    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
    Message message = underTest.parse(packet);
    assertThat(message.opcode()).isEqualTo(Opcode.ACK);
    assertThat(message.blockNum()).isEqualTo(blockNum);
  }

  @Test
  void parse_ACK_0xcdef() {
    final int blockNum = 0xcdef;
    byte[] bytes = factory.createAck(blockNum);
    assertThat(bytes[2]).isEqualTo((byte) 0xcd);
    assertThat(bytes[3]).isEqualTo((byte) 0xef);

    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
    Message message = underTest.parse(packet);
    assertThat(message.opcode()).isEqualTo(Opcode.ACK);
    assertThat(message.blockNum()).isEqualTo(blockNum);
  }

  @Test
  void parse_ACK_0xabcdef() {
    final int blockNum = 0xabcdef;
    byte[] bytes = factory.createAck(blockNum);
    assertThat(bytes[2]).isEqualTo((byte) 0xcd);
    assertThat(bytes[3]).isEqualTo((byte) 0xef);

    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
    Message message = underTest.parse(packet);
    assertThat(message.opcode()).isEqualTo(Opcode.ACK);
    assertThat(message.blockNum()).isEqualTo(0xcdef);
  }
}
