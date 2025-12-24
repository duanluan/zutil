package top.csaf.junit.id;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.id.NanoIdUtil;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@DisplayName("NanoId 工具类测试")
class NanoIdUtilTest {

  @DisplayName("生成 NanoID")
  @Test
  void randomNanoId() {
    /** {@link NanoIdUtil#randomNanoId(int, char[], java.util.Random) } */
    assertThrows(NullPointerException.class, () -> NanoIdUtil.randomNanoId(0, (char[]) null, NanoIdUtil.DEFAULT_ID_GENERATOR));
    assertThrows(NullPointerException.class, () -> NanoIdUtil.randomNanoId(0, new char[0], null));
    assertThrows(IllegalArgumentException.class, () -> NanoIdUtil.randomNanoId(0, new char[0], NanoIdUtil.DEFAULT_ID_GENERATOR));
    assertThrows(IllegalArgumentException.class, () -> NanoIdUtil.randomNanoId(1, new char[0], NanoIdUtil.DEFAULT_ID_GENERATOR));
    assertThrows(IllegalArgumentException.class, () -> NanoIdUtil.randomNanoId(1, new char[256], NanoIdUtil.DEFAULT_ID_GENERATOR));
    assertDoesNotThrow(() -> NanoIdUtil.randomNanoId(NanoIdUtil.DEFAULT_SIZE, NanoIdUtil.DEFAULT_ALPHABET, NanoIdUtil.DEFAULT_ID_GENERATOR));

    /** {@link NanoIdUtil#randomNanoId(int, java.lang.String, java.util.Random) } */
    assertThrows(NullPointerException.class, () -> NanoIdUtil.randomNanoId(0, (String) null, NanoIdUtil.DEFAULT_ID_GENERATOR));
    assertThrows(NullPointerException.class, () -> NanoIdUtil.randomNanoId(0, Arrays.toString(new char[0]), null));
    assertDoesNotThrow(() -> NanoIdUtil.randomNanoId(NanoIdUtil.DEFAULT_SIZE, Arrays.toString(NanoIdUtil.DEFAULT_ALPHABET), NanoIdUtil.DEFAULT_ID_GENERATOR));

    /** {@link NanoIdUtil#randomNanoId(int, char[]) } */
    assertThrows(NullPointerException.class, () -> NanoIdUtil.randomNanoId(0, (char[]) null));
    assertDoesNotThrow(() -> NanoIdUtil.randomNanoId(NanoIdUtil.DEFAULT_SIZE, NanoIdUtil.DEFAULT_ALPHABET));

    /** {@link NanoIdUtil#randomNanoId(int, java.lang.String) } */
    assertThrows(NullPointerException.class, () -> NanoIdUtil.randomNanoId(0, (String) null));
    assertDoesNotThrow(() -> NanoIdUtil.randomNanoId(NanoIdUtil.DEFAULT_SIZE, Arrays.toString(NanoIdUtil.DEFAULT_ALPHABET)));

    /** {@link NanoIdUtil#randomNanoId(int) } */
    assertDoesNotThrow(() -> NanoIdUtil.randomNanoId(NanoIdUtil.DEFAULT_SIZE));

    /** {@link NanoIdUtil#randomNanoId() } */
    assertDoesNotThrow(() -> NanoIdUtil.randomNanoId());
  }
}
