package tftp.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadFactoryTest {
  private final PayloadFactory underTest = new PayloadFactory();

  @Test
  void createRRQ() {
    byte[] bytes = underTest.createRRQ("sample.txt");
    assertThat(bytes[1]).isEqualTo(Opcode.RRQ.opcode());
    assertThat((char) bytes[2]).isEqualTo('s');
    assertThat((char) bytes[3]).isEqualTo('a');
    assertThat((char) bytes[4]).isEqualTo('m');
    assertThat((char) bytes[5]).isEqualTo('p');
    assertThat((char) bytes[6]).isEqualTo('l');
    assertThat((char) bytes[7]).isEqualTo('e');
    assertThat((char) bytes[8]).isEqualTo('.');
    assertThat((char) bytes[9]).isEqualTo('t');
    assertThat((char) bytes[10]).isEqualTo('x');
    assertThat((char) bytes[11]).isEqualTo('t');
    assertThat(bytes[12]).isEqualTo((byte) 0);
  }
}
