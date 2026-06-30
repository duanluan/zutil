package top.csaf.lang;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.csaf.charset.StandardCharsets;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 数组工具类
 */
@Slf4j
public class ArrayUtil extends org.apache.commons.lang3.ArrayUtils {

  /**
   * 字符数组转换为指定字符集的字节数组
   *
   * @param chars    字符数组
   * @param charsets 字符集
   * @return 指定字符集的字节数组
   */
  public static byte[] toBytes(@NonNull final char[] chars, @NonNull final Charset charsets) {
    return new String(chars).getBytes(charsets);
  }

  /**
   * 字符数组转换为指定字符集的字节数组
   *
   * @param chars       字符数组
   * @param charsetName 字符集
   * @return 指定字符集的字节数组
   */
  public static byte[] toBytes(@NonNull final char[] chars, @NonNull final String charsetName) {
    try {
      return new String(chars).getBytes(charsetName);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 字符数组转换为 UTF-8 字符集的字节数组
   *
   * @param chars 字符数组
   * @return UTF-8 字符集的字节数组
   */
  public static byte[] toBytes(@NonNull final char[] chars) {
    return new String(chars).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * 字节数组转换为指定字符集的字符串
   *
   * @param bytes    字节数组
   * @param charsets 字符集
   * @return 指定字符集的字符串
   */
  public static String toString(@NonNull final byte[] bytes, @NonNull final Charset charsets) {
    return new String(bytes, charsets);
  }

  /**
   * 字节数组转换为指定字符集的字符串
   *
   * @param bytes       字节数组
   * @param charsetName 字符集
   * @return 指定字符集的字符串
   */
  public static String toString(@NonNull final byte[] bytes, @NonNull final String charsetName) {
    try {
      return new String(bytes, charsetName);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 字节数组转换为 UTF-8 字符集的字符串
   *
   * @param bytes 字节数组
   * @return UTF-8 字符集的字符串
   */
  public static String toString(@NonNull final byte[] bytes) {
    return new String(bytes);
  }

  /**
   * 字节数组转换为指定字符集的字符数组
   *
   * @param bytes    字节数组
   * @param charsets 字符集
   * @return 指定字符集的字符数组
   */
  public static char[] toChars(@NonNull final byte[] bytes, @NonNull final Charset charsets) {
    return toString(bytes, charsets).toCharArray();
  }

  /**
   * 字节数组转换为指定字符集的字符数组
   *
   * @param bytes       字节数组
   * @param charsetName 字符集
   * @return 指定字符集的字符数组
   */
  public static char[] toChars(@NonNull final byte[] bytes, @NonNull final String charsetName) {
    return toString(bytes, charsetName).toCharArray();
  }

  /**
   * 字节数组转换为 UTF-8 字符集的字符数组
   *
   * @param bytes 字节数组
   * @return UTF-8 字符集的字符数组
   */
  public static char[] toChars(@NonNull final byte[] bytes) {
    return toString(bytes).toCharArray();
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  private static Object moveForward(@NonNull final Object array, final int startIndex, final int length) {
    // 开始下标或位数不正确
    int arrayLen = getLength(array);
    if (arrayLen == 0) {
      throw new IllegalArgumentException("Array: should not be empty");
    }
    if (startIndex < 0 || length < 1 || startIndex >= arrayLen || arrayLen - length < 0) {
      throw new IndexOutOfBoundsException("StartIndex: " + startIndex + ", Length:" + length);
    }

    // 根据 array 创建结果数组
    final Object result = Array.newInstance(array.getClass().getComponentType(), arrayLen);
    // 将 0 ~ 开始下标 的内容先 copy 到结果数组
    System.arraycopy(array, 0, result, 0, startIndex);
    // 再将 开始下标 + 位数 ~ 结尾 的内容 copy 到结果数组
    System.arraycopy(array, startIndex + length, result, startIndex, arrayLen - length - startIndex);
    return result;
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static int[] moveForward(@NonNull final int[] array, final int startIndex, final int length) {
    return (int[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static long[] moveForward(@NonNull final long[] array, final int startIndex, final int length) {
    return (long[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static double[] moveForward(@NonNull final double[] array, final int startIndex, final int length) {
    return (double[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static float[] moveForward(@NonNull final float[] array, final int startIndex, final int length) {
    return (float[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static char[] moveForward(@NonNull final char[] array, final int startIndex, final int length) {
    return (char[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static byte[] moveForward(@NonNull final byte[] array, final int startIndex, final int length) {
    return (byte[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static boolean[] moveForward(@NonNull final boolean[] array, final int startIndex, final int length) {
    return (boolean[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @return 数组
   */
  public static short[] moveForward(@NonNull final short[] array, final int startIndex, final int length) {
    return (short[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 从指定下标开始，将后面的元素往前复制指定位数
   *
   * @param array      数组
   * @param startIndex 开始下标
   * @param length     位数
   * @param <T>        数组类型
   * @return 数组
   */
  public static <T> T[] moveForward(@NonNull final T[] array, final int startIndex, final int length) {
    return (T[]) moveForward((Object) array, startIndex, length);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  private static Object remove(@NonNull final Object array, final int index, @NonNull final Object lastElementValue) {
    Object result = moveForward(array, index, 1);
    // 给数组最后一个元素赋值
    if (array instanceof boolean[]) {
      ((boolean[]) result)[((boolean[]) result).length - 1] = Boolean.parseBoolean(lastElementValue.toString());
    } else if (array instanceof byte[]) {
      ((byte[]) result)[((byte[]) result).length - 1] = Byte.parseByte(lastElementValue.toString());
    } else if (array instanceof char[]) {
      String s = lastElementValue.toString();
      if (s.length() == 1) {
        ((char[]) result)[((char[]) result).length - 1] = s.charAt(0);
      } else {
        throw new IllegalArgumentException("LastElementValue: should be a char");
      }
    } else if (array instanceof short[]) {
      ((short[]) result)[((short[]) result).length - 1] = Short.parseShort(lastElementValue.toString());
    } else if (array instanceof int[]) {
      ((int[]) result)[((int[]) result).length - 1] = Integer.parseInt(lastElementValue.toString());
    } else if (array instanceof long[]) {
      ((long[]) result)[((long[]) result).length - 1] = Long.parseLong(lastElementValue.toString());
    } else if (array instanceof float[]) {
      ((float[]) result)[((float[]) result).length - 1] = Float.parseFloat(lastElementValue.toString());
    } else if (array instanceof double[]) {
      ((double[]) result)[((double[]) result).length - 1] = Double.parseDouble(lastElementValue.toString());
    } else {
      ((Object[]) result)[((Object[]) result).length - 1] = lastElementValue;
    }
    return result;
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static int[] remove(@NonNull final int[] array, final int index, final int lastElementValue) {
    return (int[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static long[] remove(@NonNull final long[] array, final int index, final long lastElementValue) {
    return (long[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static double[] remove(@NonNull final double[] array, final int index, final double lastElementValue) {
    return (double[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static float[] remove(@NonNull final float[] array, final int index, final float lastElementValue) {
    return (float[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static char[] remove(@NonNull final char[] array, final int index, final char lastElementValue) {
    return (char[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static byte[] remove(@NonNull final byte[] array, final int index, final byte lastElementValue) {
    return (byte[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static boolean[] remove(@NonNull final boolean[] array, final int index, final boolean lastElementValue) {
    return (boolean[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @return 数组
   */
  public static short[] remove(@NonNull final short[] array, final int index, final short lastElementValue) {
    return (short[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 删除指定下标的元素
   *
   * @param array            数组
   * @param index            下标
   * @param lastElementValue 最后一个元素的值
   * @param <T>              数组类型
   * @return 数组
   */
  public static <T> T[] remove(@NonNull final T[] array, final int index, @NonNull final T lastElementValue) {
    return (T[]) remove((Object) array, index, lastElementValue);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  private static Object fill(@NonNull final Object array, final int fromIndex, final int toIndex, final Object value) {
    int arrayLen = getLength(array);
    Object result = array;
    // 此处不减 1，因为 Arrays.fill 的 toIndex 要 +1 才能填充完最后一个元素
    if (toIndex > arrayLen) {
      // 根据结束下标创建结果数组
      result = Array.newInstance(array.getClass().getComponentType(), toIndex);
      // 将内容 copy 到结果数组
      System.arraycopy(array, 0, result, 0, arrayLen);
    }
    if (result instanceof int[]) {
      if (!(value instanceof Integer)) {
        throw new IllegalArgumentException("Value: should be Integer");
      }
      Arrays.fill((int[]) result, fromIndex, toIndex, (int) value);
    } else if (result instanceof long[]) {
      if (!(value instanceof Long)) {
        throw new IllegalArgumentException("Value: should be Long");
      }
      Arrays.fill((long[]) result, fromIndex, toIndex, (long) value);
    } else if (result instanceof double[]) {
      if (!(value instanceof Double)) {
        throw new IllegalArgumentException("Value: should be Double");
      }
      Arrays.fill((double[]) result, fromIndex, toIndex, (double) value);
    } else if (result instanceof float[]) {
      if (!(value instanceof Float)) {
        throw new IllegalArgumentException("Value: should be Float");
      }
      Arrays.fill((float[]) result, fromIndex, toIndex, (float) value);
    } else if (result instanceof char[]) {
      if (!(value instanceof Character)) {
        throw new IllegalArgumentException("Value: should be Character");
      }
      Arrays.fill((char[]) result, fromIndex, toIndex, (char) value);
    } else if (result instanceof byte[]) {
      if (!(value instanceof Byte)) {
        throw new IllegalArgumentException("Value: should be Byte");
      }
      Arrays.fill((byte[]) result, fromIndex, toIndex, (byte) value);
    } else if (result instanceof boolean[]) {
      if (!(value instanceof Boolean)) {
        throw new IllegalArgumentException("Value: should be Boolean");
      }
      Arrays.fill((boolean[]) result, fromIndex, toIndex, (boolean) value);
    } else if (result instanceof short[]) {
      if (!(value instanceof Short)) {
        throw new IllegalArgumentException("Value: should be Short");
      }
      Arrays.fill((short[]) result, fromIndex, toIndex, (short) value);
    } else {
      Arrays.fill((Object[]) result, fromIndex, toIndex, value);
    }
    return result;
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static int[] fill(@NonNull final int[] array, final int fromIndex, final int toIndex, final int value) {
    return (int[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static long[] fill(@NonNull final long[] array, final int fromIndex, final int toIndex, final long value) {
    return (long[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static double[] fill(@NonNull final double[] array, final int fromIndex, final int toIndex, final double value) {
    return (double[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static float[] fill(@NonNull final float[] array, final int fromIndex, final int toIndex, final float value) {
    return (float[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static char[] fill(@NonNull final char[] array, final int fromIndex, final int toIndex, final char value) {
    return (char[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static byte[] fill(@NonNull final byte[] array, final int fromIndex, final int toIndex, final byte value) {
    return (byte[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static boolean[] fill(@NonNull final boolean[] array, final int fromIndex, final int toIndex, final boolean value) {
    return (boolean[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @return 数组
   */
  public static short[] fill(@NonNull final short[] array, final int fromIndex, final int toIndex, final short value) {
    return (short[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 填充，结束下标超出数组长度时，超出部分也填充
   *
   * @param array     数组
   * @param fromIndex 开始下标
   * @param toIndex   结束下标
   * @param value     填充值
   * @param <T>       数组类型
   * @return 数组
   */
  public static <T> T[] fill(@NonNull final T[] array, final int fromIndex, final int toIndex, final T value) {
    return (T[]) fill((Object) array, fromIndex, toIndex, value);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  private static Object removeAllElements(@NonNull final Object array, @NonNull final Object... values) {
    if (getLength(array) == 0) {
      return array;
    }
    Object result;
    if (array instanceof int[]) {
      result = ((int[]) array).clone();
      for (Object value : values) {
        // 此处 value 为数组
        if (contains((int[]) array, ((int[]) value)[0])) {
          result = removeElement((int[]) result, ((int[]) value)[0]);
        }
      }
    } else if (array instanceof long[]) {
      result = ((long[]) array).clone();
      for (Object value : values) {
        if (contains((long[]) array, ((long[]) value)[0])) {
          result = removeElement((long[]) result, ((long[]) value)[0]);
        }
      }
    } else if (array instanceof double[]) {
      result = ((double[]) array).clone();
      for (Object value : values) {
        if (contains((double[]) array, ((double[]) value)[0])) {
          result = removeElement((double[]) result, ((double[]) value)[0]);
        }
      }
    } else if (array instanceof float[]) {
      result = ((float[]) array).clone();
      for (Object value : values) {
        if (contains((float[]) array, ((float[]) value)[0])) {
          result = removeElement((float[]) result, ((float[]) value)[0]);
        }
      }
    } else if (array instanceof char[]) {
      result = ((char[]) array).clone();
      for (Object value : values) {
        if (contains((char[]) array, ((char[]) value)[0])) {
          result = removeElement((char[]) result, ((char[]) value)[0]);
        }
      }
    } else if (array instanceof byte[]) {
      result = ((byte[]) array).clone();
      for (Object value : values) {
        if (contains((byte[]) array, ((byte[]) value)[0])) {
          result = removeElement((byte[]) result, ((byte[]) value)[0]);
        }
      }
    } else if (array instanceof boolean[]) {
      result = ((boolean[]) array).clone();
      for (Object value : values) {
        if (contains((boolean[]) array, ((boolean[]) value)[0])) {
          result = removeElement((boolean[]) result, ((boolean[]) value)[0]);
        }
      }
    } else if (array instanceof short[]) {
      result = ((short[]) array).clone();
      for (Object value : values) {
        if (contains((short[]) array, ((short[]) value)[0])) {
          result = removeElement((short[]) result, ((short[]) value)[0]);
        }
      }
    } else {
      result = ((Object[]) array).clone();
      for (Object value : values) {
        if (contains((Object[]) array, ((Object[]) value)[0])) {
          result = removeElement((Object[]) result, ((Object[]) value)[0]);
        }
      }
    }
    return result;
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static int[] removeAllElements(@NonNull final int[] array, @NonNull final int... values) {
    return (int[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static long[] removeAllElements(@NonNull final long[] array, @NonNull final long... values) {
    return (long[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static double[] removeAllElements(@NonNull final double[] array, @NonNull final double... values) {
    return (double[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static float[] removeAllElements(@NonNull final float[] array, @NonNull final float... values) {
    return (float[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static char[] removeAllElements(@NonNull final char[] array, @NonNull final char... values) {
    return (char[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static byte[] removeAllElements(@NonNull final byte[] array, @NonNull final byte... values) {
    return (byte[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static boolean[] removeAllElements(@NonNull final boolean[] array, @NonNull final boolean... values) {
    return (boolean[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @return 数组
   */
  public static short[] removeAllElements(@NonNull final short[] array, @NonNull final short... values) {
    return (short[]) removeAllElements(array, (Object) values);
  }

  /**
   * 删除所有匹配项
   *
   * @param array  数组
   * @param values 需要删除的内容
   * @param <T>    数组类型
   * @return 数组
   */
  public static <T> T[] removeAllElements(@NonNull final T[] array, @NonNull final T... values) {
    return (T[]) removeAllElements((Object) array, (Object) values);
  }

  /**
   * 从指定索引开始查找数组中指定的字符串
   *
   * @param array       字符数组
   * @param valueToFind 要查找的字符串
   * @param startIndex  开始查找的索引
   * @return 要查找的字符串在数组中的开始位置
   */
  public static int indexOf(@NonNull final char[] array, @NonNull final String valueToFind, final int startIndex) {
    if (ArrayUtil.isEmpty(array)) {
      return -1;
    }
    if (StrUtil.isBlank(valueToFind)) {
      throw new IllegalArgumentException("ValueToFind: should not be blank");
    }
    if (startIndex < 0) {
      throw new IllegalArgumentException("StartIndex: should be greater than or equal to 0");
    }
    // 查找字符串长度为 2 时使用以下写法更快
    if (valueToFind.length() == 2) {
      int charsLastElementIndex = array.length - 1;
      char[] valChars = valueToFind.toCharArray();
      char lChar = valChars[0];
      char rChar = valChars[1];

      int lCharIndex = ArrayUtil.indexOf(array, lChar, startIndex);
      // 如果找不到左边字符
      if (lCharIndex == -1 || lCharIndex == charsLastElementIndex) {
        return -1;
      }
      int rCharIndex = ArrayUtil.indexOf(array, rChar, lCharIndex + 1);
      // 如果找不到右边字符 则 -1
      if (rCharIndex == -1) {
        return -1;
      }
      // 如果右边字符下标为左边字符下标 +1，则返回左边字符下标
      else if (rCharIndex == lCharIndex + 1) {
        return lCharIndex;
      }
    }
    return new String(array).indexOf(valueToFind, startIndex);
  }

  /**
   * 查找数组中指定的字符串
   *
   * @param array       字符数组
   * @param valueToFind 要查找的字符串
   * @return 要查找的字符串在数组中的开始位置
   */
  public static int indexOf(@NonNull final char[] array, @NonNull final String valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * 所有元素的长度总和
   *
   * @param strs 字符串数组
   * @return 所有元素的长度总和
   */
  public static int allLength(@NonNull final String... strs) {
    if (StrUtil.isAllBlank(strs)) {
      return 0;
    }
    int result = 0;
    for (String str : strs) {
      result += str.length();
    }
    return result;
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  private static Object deduplicate(@NonNull final Object array) {
    Object[] array1 = (Object[]) array;
    int arrayLen = array1.length;
    Object[] newArr = (Object[]) Array.newInstance(array1.getClass().getComponentType(), arrayLen);
    int x = 0;
    for (int i = 0; i < arrayLen; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (array1[i].equals(array1[j])) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array1[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static int[] deduplicate(@NonNull final int[] array) {
    int[] newArr = new int[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static long[] deduplicate(@NonNull final long[] array) {
    long[] newArr = new long[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static double[] deduplicate(@NonNull final double[] array) {
    double[] newArr = new double[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (Double.compare(array[i], array[j]) == 0) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static float[] deduplicate(@NonNull final float[] array) {
    float[] newArr = new float[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (Float.compare(array[i], array[j]) == 0) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static char[] deduplicate(@NonNull final char[] array) {
    char[] newArr = new char[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static byte[] deduplicate(@NonNull final byte[] array) {
    byte[] newArr = new byte[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static boolean[] deduplicate(@NonNull final boolean[] array) {
    boolean[] newArr = new boolean[Math.min(array.length, 2)];
    int x = 0;
    boolean hasTrue = false;
    boolean hasFalse = false;
    for (boolean item : array) {
      if (item) {
        if (hasTrue) {
          continue;
        }
        hasTrue = true;
      } else {
        if (hasFalse) {
          continue;
        }
        hasFalse = true;
      }
      if (x < newArr.length) {
        newArr[x] = item;
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留第一个重复元素
   */
  public static short[] deduplicate(@NonNull final short[] array) {
    short[] newArr = new short[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = 0;
      for (; j < i; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (i == j) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @param <T>   数组元素类型
   * @return 去重后的数组，保留第一个重复元素
   */
  public static <T> T[] deduplicate(@NonNull final T[] array) {
    return (T[]) deduplicate((Object) array);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static Object deduplicatePreceding(@NonNull final Object array) {
    Object[] array1 = (Object[]) array;
    // 用来记录去除重复之后的数组长度和给临时数组作为下标索引
    int t = 0;
    int arrayLen = array1.length;
    // 临时数组
    Object[] tempArr = new Object[arrayLen];
    // 遍历原数组
    for (int i = 0; i < arrayLen; i++) {
      // 声明一个标记，并每次重置
      boolean isTrue = true;
      // 内层循环将原数组的元素逐个对比
      for (int j = i + 1; j < arrayLen; j++) {
        // 如果发现有重复元素，改变标记状态并结束当次内层循环
        if (array1[i].equals(array1[j])) {
          isTrue = false;
          break;
        }
      }
      // 判断标记是否被改变，如果没被改变就是没有重复元素
      if (isTrue) {
        // 没有元素就将原数组的元素赋给临时数组
        tempArr[t] = array1[i];
        // 走到这里证明当前元素没有重复，那么记录自增
        t++;
      }
    }
    // 声明需要返回的数组，这个才是去重后的数组
    Object[] newArr = (Object[]) Array.newInstance(array1.getClass().getComponentType(), t);
    // 用 arraycopy 方法将刚才去重的数组拷贝到新数组并返回
    System.arraycopy(tempArr, 0, newArr, 0, t);
    return newArr;
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static int[] deduplicatePreceding(@NonNull final int[] array) {
    int[] newArr = new int[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static long[] deduplicatePreceding(@NonNull final long[] array) {
    long[] newArr = new long[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static double[] deduplicatePreceding(@NonNull final double[] array) {
    double[] newArr = new double[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (Double.compare(array[i], array[j]) == 0) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static float[] deduplicatePreceding(@NonNull final float[] array) {
    float[] newArr = new float[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (Float.compare(array[i], array[j]) == 0) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static char[] deduplicatePreceding(@NonNull final char[] array) {
    char[] newArr = new char[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static byte[] deduplicatePreceding(@NonNull final byte[] array) {
    byte[] newArr = new byte[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static boolean[] deduplicatePreceding(@NonNull final boolean[] array) {
    boolean[] newArr = new boolean[Math.min(array.length, 2)];
    int x = newArr.length;
    boolean hasTrue = false;
    boolean hasFalse = false;
    for (int i = array.length - 1; i >= 0; i--) {
      boolean item = array[i];
      if (item) {
        if (hasTrue) {
          continue;
        }
        hasTrue = true;
      } else {
        if (hasFalse) {
          continue;
        }
        hasFalse = true;
      }
      if (x > 0) {
        newArr[--x] = item;
      }
    }
    return Arrays.copyOfRange(newArr, x, newArr.length);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static short[] deduplicatePreceding(@NonNull final short[] array) {
    short[] newArr = new short[array.length];
    int x = 0;
    for (int i = 0, l = array.length; i < l; i++) {
      int j = i + 1;
      for (; j < l; j++) {
        if (array[i] == array[j]) {
          break;
        }
      }
      if (j == l) {
        newArr[x] = array[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @param <T>   数组元素类型
   * @return 去重后的数组，保留最后一个重复元素
   */
  public static <T> T[] deduplicatePreceding(@NonNull final T[] array) {
    return (T[]) deduplicatePreceding((Object) array);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static Object deduplicateReverse(@NonNull final Object array) {
    Set<Object> treeSet = new TreeSet<>(Arrays.asList((Object[]) array)).descendingSet();
    return new ArrayList<>(treeSet).toArray((Object[]) Array.newInstance(array.getClass().getComponentType(), treeSet.size()));
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static int[] deduplicateReverse(@NonNull final int[] array) {
    int[] sorted = array.clone();
    Arrays.sort(sorted);
    int[] newArr = new int[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || sorted[i] != sorted[i + 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static long[] deduplicateReverse(@NonNull final long[] array) {
    long[] sorted = array.clone();
    Arrays.sort(sorted);
    long[] newArr = new long[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || sorted[i] != sorted[i + 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static double[] deduplicateReverse(@NonNull final double[] array) {
    double[] sorted = array.clone();
    Arrays.sort(sorted);
    double[] newArr = new double[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || Double.compare(sorted[i], sorted[i + 1]) != 0) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static float[] deduplicateReverse(@NonNull final float[] array) {
    float[] sorted = array.clone();
    Arrays.sort(sorted);
    float[] newArr = new float[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || Float.compare(sorted[i], sorted[i + 1]) != 0) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static char[] deduplicateReverse(@NonNull final char[] array) {
    char[] sorted = array.clone();
    Arrays.sort(sorted);
    char[] newArr = new char[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || sorted[i] != sorted[i + 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static byte[] deduplicateReverse(@NonNull final byte[] array) {
    byte[] sorted = array.clone();
    Arrays.sort(sorted);
    byte[] newArr = new byte[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || sorted[i] != sorted[i + 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static boolean[] deduplicateReverse(@NonNull final boolean[] array) {
    boolean hasTrue = false;
    boolean hasFalse = false;
    for (boolean item : array) {
      if (item) {
        hasTrue = true;
      } else {
        hasFalse = true;
      }
      if (hasTrue && hasFalse) {
        break;
      }
    }
    if (hasTrue && hasFalse) {
      return new boolean[]{true, false};
    }
    if (hasTrue) {
      return new boolean[]{true};
    }
    if (hasFalse) {
      return new boolean[]{false};
    }
    return new boolean[]{};
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，倒序
   */
  public static short[] deduplicateReverse(@NonNull final short[] array) {
    short[] sorted = array.clone();
    Arrays.sort(sorted);
    short[] newArr = new short[sorted.length];
    int x = 0;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (i == sorted.length - 1 || sorted[i] != sorted[i + 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @param <T>   数组元素类型
   * @return 去重后的数组，倒序
   */
  public static <T> T[] deduplicateReverse(@NonNull final T[] array) {
    return (T[]) deduplicateReverse((Object) array);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static Object deduplicateHashSort(@NonNull final Object array) {
    Map<Object, Object> map = new HashMap<>();
    Object[] array1 = (Object[]) array;
    Object[] newArr = (Object[]) Array.newInstance(array1.getClass().getComponentType(), array1.length);
    int x = 0;
    for (Object item : array1) {
      if (map.containsKey(item)) {
        continue;
      }
      map.put(item, item);
      newArr[x] = item;
      x++;
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static int[] deduplicateHashSort(@NonNull final int[] array) {
    int[] sorted = array.clone();
    Arrays.sort(sorted);
    int[] newArr = new int[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || sorted[i] != sorted[i - 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static long[] deduplicateHashSort(@NonNull final long[] array) {
    long[] sorted = array.clone();
    Arrays.sort(sorted);
    long[] newArr = new long[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || sorted[i] != sorted[i - 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static double[] deduplicateHashSort(@NonNull final double[] array) {
    double[] sorted = array.clone();
    Arrays.sort(sorted);
    double[] newArr = new double[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || Double.compare(sorted[i], sorted[i - 1]) != 0) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static float[] deduplicateHashSort(@NonNull final float[] array) {
    float[] sorted = array.clone();
    Arrays.sort(sorted);
    float[] newArr = new float[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || Float.compare(sorted[i], sorted[i - 1]) != 0) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static char[] deduplicateHashSort(@NonNull final char[] array) {
    char[] sorted = array.clone();
    Arrays.sort(sorted);
    char[] newArr = new char[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || sorted[i] != sorted[i - 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static byte[] deduplicateHashSort(@NonNull final byte[] array) {
    byte[] sorted = array.clone();
    Arrays.sort(sorted);
    byte[] newArr = new byte[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || sorted[i] != sorted[i - 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static boolean[] deduplicateHashSort(@NonNull final boolean[] array) {
    boolean hasTrue = false;
    boolean hasFalse = false;
    for (boolean item : array) {
      if (item) {
        hasTrue = true;
      } else {
        hasFalse = true;
      }
      if (hasTrue && hasFalse) {
        break;
      }
    }
    if (hasFalse && hasTrue) {
      return new boolean[]{false, true};
    }
    if (hasFalse) {
      return new boolean[]{false};
    }
    if (hasTrue) {
      return new boolean[]{true};
    }
    return new boolean[]{};
  }

  /**
   * 去重
   *
   * @param array 数组
   * @return 去重后的数组，hash 排序
   */
  public static short[] deduplicateHashSort(@NonNull final short[] array) {
    short[] sorted = array.clone();
    Arrays.sort(sorted);
    short[] newArr = new short[sorted.length];
    int x = 0;
    for (int i = 0; i < sorted.length; i++) {
      if (i == 0 || sorted[i] != sorted[i - 1]) {
        newArr[x] = sorted[i];
        x++;
      }
    }
    return Arrays.copyOf(newArr, x);
  }

  /**
   * 去重
   *
   * @param array 数组
   * @param <T>   数组元素类型
   * @return 去重后的数组，hash 排序
   */
  public static <T> T[] deduplicateHashSort(@NonNull final T[] array) {
    return (T[]) deduplicateHashSort((Object) array);
  }
}
