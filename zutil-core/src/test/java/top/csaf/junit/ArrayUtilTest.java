package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.charset.StandardCharsets;
import top.csaf.lang.ArrayUtil;
import top.csaf.util.ReflectionTestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("数组工具类测试")
class ArrayUtilTest {

  @DisplayName("toBytesAndToString：转字节数组和转字符串")
  @Test
  void toBytesAndToString() throws UnsupportedEncodingException {
    String str = "中国 No.1";
    String str1 = "?? No.1";
    char[] chars = str.toCharArray();

    assertThrows(NullPointerException.class, () -> ArrayUtil.toBytes(null, StandardCharsets.GB2312));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toBytes(chars, (Charset) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toString(null, StandardCharsets.GB2312));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toString(new byte[]{}, (Charset) null));
    // 指定字符集的字符数组转字节数组
    assertEquals(str1, ArrayUtil.toString(ArrayUtil.toBytes(chars, StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
    assertEquals(str, ArrayUtil.toString(ArrayUtil.toBytes(chars, StandardCharsets.GB2312), StandardCharsets.GB2312));

    assertThrows(NullPointerException.class, () -> ArrayUtil.toBytes(null, StandardCharsets.GB2312.name()));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toBytes(chars, (String) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toString(null, StandardCharsets.GB2312.name()));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toString(new byte[]{}, (String) null));
    assertEquals(str1, ArrayUtil.toString(ArrayUtil.toBytes(chars, StandardCharsets.ISO_8859_1.name()), StandardCharsets.ISO_8859_1.name()));
    assertEquals(str, ArrayUtil.toString(ArrayUtil.toBytes(chars, StandardCharsets.GB2312.name()), StandardCharsets.GB2312.name()));

    assertThrows(NullPointerException.class, () -> ArrayUtil.toBytes(null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toString(null));
    // 默认 UTF-8 字符集的字符数组转字节数组
    assertEquals(str, ArrayUtil.toString(ArrayUtil.toBytes(chars)));
  }

  @DisplayName("toChars：转字符数组")
  @Test
  void toChars() throws UnsupportedEncodingException {
    char[] chars = "中国 No.1".toCharArray();
    char[] chars1 = "?? No.1".toCharArray();

    // 异常
    assertThrows(NullPointerException.class, () -> ArrayUtil.toChars(null, StandardCharsets.GB2312));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toChars(ArrayUtil.toBytes(chars), (Charset) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toChars(null, StandardCharsets.GB2312.name()));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toChars(ArrayUtil.toBytes(chars), (String) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.toChars(null));

    assertArrayEquals(chars1, ArrayUtil.toChars(ArrayUtil.toBytes(chars, StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
    assertArrayEquals(chars, ArrayUtil.toChars(ArrayUtil.toBytes(chars, StandardCharsets.GB2312), StandardCharsets.GB2312));
    assertArrayEquals(chars1, ArrayUtil.toChars(ArrayUtil.toBytes(chars, StandardCharsets.ISO_8859_1.name()), StandardCharsets.ISO_8859_1.name()));
    assertArrayEquals(chars, ArrayUtil.toChars(ArrayUtil.toBytes(chars, StandardCharsets.GB2312.name()), StandardCharsets.GB2312.name()));
    assertArrayEquals(chars, ArrayUtil.toChars(ArrayUtil.toBytes(chars)));
    assertArrayEquals(chars, ArrayUtil.toChars(ArrayUtil.toBytes(chars)));
  }

  @DisplayName("removeAndMoveForward：删除元素和移动复制元素")
  @Test
  void removeAndMoveForward() {
    // assertThrows(NullPointerException.class, () -> ArrayUtils.moveForward(null, 1, 1));
    // 不是数组
    // assertThrows(IllegalArgumentException.class, () -> ArrayUtils.moveForward(1, 1, 1));
    // 下标错误
    assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtil.moveForward(new int[]{1}, -1, 1));
    // assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtils.moveForward(new int[]{}, 1, -1));
    // assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtils.moveForward(new int[]{}, 1, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtil.moveForward(new int[]{1}, 0, 2));

    // null
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new Class[]{Object.class, int.class, Object.class}, null, 0, 1));
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new Class[]{Object.class, int.class, Object.class}, new Integer[]{1}, 0, null));
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new Class[]{Object.class, int.class, Object.class}, null, 0, null));

    // lastElementValue 类型错误
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new int[]{1}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new int[]{1}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new long[]{1}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new double[]{1}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new float[]{1}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new byte[]{1}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new char[]{1}, 0, ""));
    // assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtils.invokeMethod(ArrayUtils.class, "remove", new boolean[]{true}, 0, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new short[]{1}, 0, ""));

    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((Integer[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove(new Integer[]{}, 0, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((int[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((long[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((double[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((float[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((byte[]) null, 0, (byte) 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((char[]) null, 0, '1'));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove(null, 0, true));
    assertThrows(NullPointerException.class, () -> ArrayUtil.remove((short[]) null, 0, (short) 1));
    // 下标错误
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "remove", new int[]{1}, 0, null));

    int[] ints = {1, 2, 3, 4, 5};
    // 删除下标为 3 的元素后，最后一个元素默认为 9，即保持元素个数不变
    ints = ArrayUtil.remove(ints, 3, 9);
    // 下标为 3 的元素
    assertEquals(5, ints[3]);
    // 最后一个元素
    assertEquals(9, ints[ints.length - 1]);

    long[] longs = {1, 2, 3, 4, 5};
    longs = ArrayUtil.remove(longs, 3, 9L);
    assertEquals(5, longs[3]);
    assertEquals(9, longs[longs.length - 1]);

    double[] doubles = {1, 2, 3, 4, 5};
    doubles = ArrayUtil.remove(doubles, 3, 9.0);
    assertEquals(5, doubles[3]);
    assertEquals(9, doubles[doubles.length - 1]);

    float[] floats = {1, 2, 3, 4, 5};
    floats = ArrayUtil.remove(floats, 3, 9f);
    assertEquals(5, floats[3]);
    assertEquals(9, floats[floats.length - 1]);

    byte[] bytes = {1, 2, 3, 4, 5};
    bytes = ArrayUtil.remove(bytes, 3, (byte) 9);
    assertEquals(5, bytes[3]);
    assertEquals(9, bytes[bytes.length - 1]);

    char[] chars = {'1', '2', '3', '4', '5'};
    chars = ArrayUtil.remove(chars, 3, '9');
    assertEquals('5', chars[3]);
    assertEquals('9', chars[chars.length - 1]);

    boolean[] booleans = {true, false, true, false, true};
    booleans = ArrayUtil.remove(booleans, 3, false);
    assertTrue(booleans[3]);
    assertFalse(booleans[booleans.length - 1]);

    short[] shorts = {1, 2, 3, 4, 5};
    shorts = ArrayUtil.remove(shorts, 3, (short) 9);
    assertEquals(5, shorts[3]);
    assertEquals(9, shorts[shorts.length - 1]);

    Integer[] ints1 = {1, 2, 3, 4, 5};
    ints1 = ArrayUtil.remove(ints1, 3, 9);
    assertEquals(5, ints1[3]);
    assertEquals(9, ints1[ints1.length - 1]);
  }

  @DisplayName("fill：填充元素")
  @Test
  void fill() {
    // 不是数组
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", 1, 1, 2, 1));

    // null
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new Class[]{Object.class, int.class, int.class, Object.class}, null, 0, 1, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((int[]) null, 0, 1, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((long[]) null, 0, 1, 1L));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill(null, 0, 1, 1.0));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((float[]) null, 0, 1, 1f));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((byte[]) null, 0, 1, (byte) 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((char[]) null, 0, 1, '1'));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill(null, 0, 1, true));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((short[]) null, 0, 1, (short) 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.fill((Integer[]) null, 0, 1, 9));

    // lastElementValue 类型错误
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new int[]{1}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new long[]{1}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new double[]{1}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new float[]{1}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new byte[]{1}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new char[]{1}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new boolean[]{true}, 0, 1, ""));
    assertThrows(IllegalArgumentException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "fill", new short[]{1}, 0, 1, ""));

    int[] ints = {1, 2, 3, 4, 5};
    // 从下标为 3 开始填充 1，填充到下标为 5（含前不含后）的位置
    ints = ArrayUtil.fill(ints, 3, ints.length, 1);
    // 最后一个元素
    assertEquals(1, ints[ints.length - 1]);
    // 数组长度
    assertEquals(5, ints.length);

    long[] longs = {1, 2, 3, 4, 5};
    // 从下标为 3 开始填充 1，填充到下标为 6（就算含前不含后也超出了数组长度，会先扩充再填充）的位置
    longs = ArrayUtil.fill(longs, 3, longs.length + 1, 1L);
    assertEquals(1, longs[longs.length - 1]);
    assertEquals(6, longs.length);

    double[] doubles = {1, 2, 3, 4, 5};
    doubles = ArrayUtil.fill(doubles, 3, doubles.length, 1.0);
    assertEquals(1, doubles[doubles.length - 1]);
    assertEquals(5, doubles.length);

    float[] floats = {1, 2, 3, 4, 5};
    floats = ArrayUtil.fill(floats, 3, floats.length, 1f);
    assertEquals(1, floats[floats.length - 1]);
    assertEquals(5, floats.length);

    byte[] bytes = {1, 2, 3, 4, 5};
    bytes = ArrayUtil.fill(bytes, 3, bytes.length, (byte) 1);
    assertEquals(1, bytes[bytes.length - 1]);
    assertEquals(5, bytes.length);

    char[] chars = {'1', '2', '3', '4', '5'};
    chars = ArrayUtil.fill(chars, 3, chars.length, '1');
    assertEquals('1', chars[chars.length - 1]);
    assertEquals(5, chars.length);

    boolean[] booleans = {true, false, true, false, true};
    booleans = ArrayUtil.fill(booleans, 3, booleans.length, false);
    assertFalse(booleans[booleans.length - 1]);
    assertEquals(5, booleans.length);

    short[] shorts = {1, 2, 3, 4, 5};
    shorts = ArrayUtil.fill(shorts, 3, shorts.length, (short) 1);
    assertEquals(1, shorts[shorts.length - 1]);
    assertEquals(5, shorts.length);

    Integer[] ints1 = {1, 2, 3, 4, 5};
    ints1 = ArrayUtil.fill(ints1, 3, ints1.length, 9);
    assertEquals(9, ints1[ints1.length - 1]);
    assertEquals(5, ints1.length);
  }

  @DisplayName("removeAllElements：删除所有匹配元素")
  @Test
  void removeAllElements() {
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((int[]) null, 2));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new int[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((int[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((long[]) null, 2L));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new long[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((long[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((double[]) null, 2.0));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new double[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((double[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((float[]) null, 2f));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new float[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((float[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((byte[]) null, (byte) 2));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new byte[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((byte[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((char[]) null, '2'));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new char[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((char[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((boolean[]) null, true));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new boolean[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((boolean[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((short[]) null, (short) 2));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new short[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((short[]) null, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((Integer[]) null, 2));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements(new Integer[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.removeAllElements((Integer[]) null, null));
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "removeAllElements", new Class[]{Object.class, Object[].class}, null, new Object[]{2}));
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "removeAllElements", new Class[]{Object.class, Object[].class}, new int[]{}, null));
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "removeAllElements", new Class[]{Object.class, Object[].class}, null, null));

    assertArrayEquals(new int[]{1}, ArrayUtil.removeAllElements(new int[]{1, 2}, 2));
    assertArrayEquals(new long[]{1}, ArrayUtil.removeAllElements(new long[]{1, 2}, 2L));
    assertArrayEquals(new double[]{1}, ArrayUtil.removeAllElements(new double[]{1, 2}, 2.0));
    assertArrayEquals(new float[]{1}, ArrayUtil.removeAllElements(new float[]{1, 2}, 2f));
    assertArrayEquals(new byte[]{1}, ArrayUtil.removeAllElements(new byte[]{1, 2}, (byte) 2));
    assertArrayEquals(new char[]{'1'}, ArrayUtil.removeAllElements(new char[]{'1', '2'}, '2'));
    assertArrayEquals(new boolean[]{true}, ArrayUtil.removeAllElements(new boolean[]{true, false}, false));
    assertArrayEquals(new short[]{1}, ArrayUtil.removeAllElements(new short[]{1, 2}, (short) 2));
    assertArrayEquals(new Integer[]{1}, ArrayUtil.removeAllElements(new Integer[]{1, 2}, 2));
  }

  @DisplayName("indexOf：查找数组中指定的字符串")
  @Test
  void indexOf() {
    assertThrows(NullPointerException.class, () -> ArrayUtil.indexOf((char[]) null, "", 0));
    assertThrows(NullPointerException.class, () -> ArrayUtil.indexOf(new char[]{}, null, 0));
    assertThrows(NullPointerException.class, () -> ArrayUtil.indexOf((char[]) null, null, 0));
    assertThrows(NullPointerException.class, () -> ArrayUtil.indexOf((char[]) null, ""));
    assertThrows(NullPointerException.class, () -> ArrayUtil.indexOf(new char[]{}, null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.indexOf((char[]) null, null));

    // assertThrows(IllegalArgumentException.class, () -> ArrayUtils.indexOf(new char[]{}, ""));
    assertThrows(IllegalArgumentException.class, () -> ArrayUtil.indexOf(new char[]{'0'}, ""));
    assertThrows(IllegalArgumentException.class, () -> ArrayUtil.indexOf(new char[]{'0'}, "0", -1));

    char[] chars = "{1}{}".toCharArray();
    assertEquals(-1, ArrayUtil.indexOf(chars, "0}"));
    assertEquals(-1, ArrayUtil.indexOf(chars, "0}"));
    assertEquals(-1, ArrayUtil.indexOf(chars, "}0", 1));
    assertEquals(-1, ArrayUtil.indexOf(chars, "}0", 3));
    assertEquals(3, ArrayUtil.indexOf(chars, "{}"));
    assertEquals(3, ArrayUtil.indexOf(chars, "{}", 1));
    assertEquals(2, ArrayUtil.indexOf(chars, "}{}"));
  }

  @DisplayName("allLength：所有元素的长度总和")
  @Test
  void allLength() {
    assertThrows(NullPointerException.class, () -> ArrayUtil.allLength(null));
    // assertThrows(IllegalArgumentException.class, () -> ArrayUtils.allLength("", ""));

    assertEquals(0, ArrayUtil.allLength("", null));
    assertEquals(2, ArrayUtil.allLength("1", "2"));
  }

  @DisplayName("ArrayUtil null 参数分支")
  @Test
  void nullGuardBranches() {
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((int[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((long[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((double[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((float[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((char[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((byte[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((boolean[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((short[]) null, 0, 1));
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((Integer[]) null, 0, 1));

    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((int[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((long[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((double[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((float[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((char[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((byte[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((boolean[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((short[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((Integer[]) null));

    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((Object) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((int[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((long[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((double[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((float[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((char[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((byte[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((boolean[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((short[]) null));

    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((Object) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((int[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((long[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((double[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((float[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((char[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((byte[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((boolean[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((short[]) null));

    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((Object) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((int[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((long[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((double[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((float[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((char[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((byte[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((boolean[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((short[]) null));
  }

  @DisplayName("ArrayUtil 剩余分支")
  @Test
  void remainingBranches() {
    assertArrayEquals(new int[]{}, ArrayUtil.removeAllElements(new int[]{}, 1));
    assertArrayEquals(new int[]{1}, ArrayUtil.removeAllElements(new int[]{1}, 2));
    assertArrayEquals(new long[]{1}, ArrayUtil.removeAllElements(new long[]{1}, 2L));
    assertArrayEquals(new double[]{1}, ArrayUtil.removeAllElements(new double[]{1}, 2.0));
    assertArrayEquals(new float[]{1}, ArrayUtil.removeAllElements(new float[]{1}, 2f));
    assertArrayEquals(new char[]{'1'}, ArrayUtil.removeAllElements(new char[]{'1'}, '2'));
    assertArrayEquals(new byte[]{1}, ArrayUtil.removeAllElements(new byte[]{1}, (byte) 2));
    assertArrayEquals(new boolean[]{true}, ArrayUtil.removeAllElements(new boolean[]{true}, false));
    assertArrayEquals(new short[]{1}, ArrayUtil.removeAllElements(new short[]{1}, (short) 2));
    assertArrayEquals(new Integer[]{1}, ArrayUtil.removeAllElements(new Integer[]{1}, 2));

    assertArrayEquals(new int[]{1}, ArrayUtil.deduplicate(new int[]{1, 1}));
    assertArrayEquals(new long[]{1}, ArrayUtil.deduplicate(new long[]{1, 1}));
    assertArrayEquals(new double[]{1}, ArrayUtil.deduplicate(new double[]{1, 1}));
    assertArrayEquals(new float[]{1}, ArrayUtil.deduplicate(new float[]{1, 1}));
    assertArrayEquals(new char[]{'1'}, ArrayUtil.deduplicate(new char[]{'1', '1'}));
    assertArrayEquals(new byte[]{1}, ArrayUtil.deduplicate(new byte[]{1, 1}));
    assertArrayEquals(new boolean[]{true}, ArrayUtil.deduplicate(new boolean[]{true, true}));
    assertArrayEquals(new short[]{1}, ArrayUtil.deduplicate(new short[]{1, 1}));
    assertArrayEquals(new Integer[]{1}, ArrayUtil.deduplicate(new Integer[]{1, 1}));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate(new Integer[]{null, 1, null}));

    assertArrayEquals(new int[]{2, 1}, ArrayUtil.deduplicatePreceding(new int[]{1, 2, 1}));
    assertArrayEquals(new long[]{2, 1}, ArrayUtil.deduplicatePreceding(new long[]{1, 2, 1}));
    assertArrayEquals(new double[]{2, 1}, ArrayUtil.deduplicatePreceding(new double[]{1, 2, 1}));
    assertArrayEquals(new float[]{2, 1}, ArrayUtil.deduplicatePreceding(new float[]{1, 2, 1}));
    assertArrayEquals(new char[]{'2', '1'}, ArrayUtil.deduplicatePreceding(new char[]{'1', '2', '1'}));
    assertArrayEquals(new byte[]{2, 1}, ArrayUtil.deduplicatePreceding(new byte[]{1, 2, 1}));
    assertArrayEquals(new boolean[]{false, true}, ArrayUtil.deduplicatePreceding(new boolean[]{true, false, true}));
    assertArrayEquals(new short[]{2, 1}, ArrayUtil.deduplicatePreceding(new short[]{1, 2, 1}));
    assertArrayEquals(new Integer[]{2, 1}, ArrayUtil.deduplicatePreceding(new Integer[]{1, 2, 1}));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding(new Integer[]{1, null, 1}));

    assertArrayEquals(new int[]{2, 1}, ArrayUtil.deduplicateReverse(new int[]{1, 2, 1}));
    assertArrayEquals(new long[]{2, 1}, ArrayUtil.deduplicateReverse(new long[]{1, 2, 1}));
    assertArrayEquals(new double[]{2, 1}, ArrayUtil.deduplicateReverse(new double[]{1, 2, 1}));
    assertArrayEquals(new float[]{2, 1}, ArrayUtil.deduplicateReverse(new float[]{1, 2, 1}));
    assertArrayEquals(new char[]{'2', '1'}, ArrayUtil.deduplicateReverse(new char[]{'1', '2', '1'}));
    assertArrayEquals(new byte[]{2, 1}, ArrayUtil.deduplicateReverse(new byte[]{1, 2, 1}));
    assertArrayEquals(new boolean[]{true, false}, ArrayUtil.deduplicateReverse(new boolean[]{true, false, true}));
    assertArrayEquals(new short[]{2, 1}, ArrayUtil.deduplicateReverse(new short[]{1, 2, 1}));
    assertArrayEquals(new Integer[]{2, 1}, ArrayUtil.deduplicateReverse(new Integer[]{1, 2, 1}));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse(new Integer[]{1, null, 1}));

    assertArrayEquals(new int[]{1, 2}, ArrayUtil.deduplicateHashSort(new int[]{1, 2, 1}));
    assertArrayEquals(new long[]{1, 2}, ArrayUtil.deduplicateHashSort(new long[]{1, 2, 1}));
    assertArrayEquals(new double[]{1, 2}, ArrayUtil.deduplicateHashSort(new double[]{1, 2, 1}));
    assertArrayEquals(new float[]{1, 2}, ArrayUtil.deduplicateHashSort(new float[]{1, 2, 1}));
    assertArrayEquals(new char[]{'1', '2'}, ArrayUtil.deduplicateHashSort(new char[]{'1', '2', '1'}));
    assertArrayEquals(new byte[]{1, 2}, ArrayUtil.deduplicateHashSort(new byte[]{1, 2, 1}));
    assertArrayEquals(new boolean[]{false, true}, ArrayUtil.deduplicateHashSort(new boolean[]{true, false, true}));
    assertArrayEquals(new short[]{1, 2}, ArrayUtil.deduplicateHashSort(new short[]{1, 2, 1}));
    assertArrayEquals(new Integer[]{1, 2}, ArrayUtil.deduplicateHashSort(new Integer[]{1, 2, 1}));
    assertArrayEquals(new Integer[]{1, null}, ArrayUtil.deduplicateHashSort(new Integer[]{1, null, 1}));
  }
}
