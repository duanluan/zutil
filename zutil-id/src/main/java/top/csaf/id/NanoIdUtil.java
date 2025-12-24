package top.csaf.id;

import lombok.NonNull;

import java.security.SecureRandom;
import java.util.Random;

/**
 * NanoID 工具类
 */
public class NanoIdUtil {

  /**
   * 默认生成器
   */
  public static final SecureRandom DEFAULT_ID_GENERATOR = new SecureRandom();

  /**
   * 默认字典
   */
  public static final char[] DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  /**
   * 默认长度
   */
  public static final int DEFAULT_SIZE = 21;

  /**
   * 生成 NanoID
   *
   * @param size      长度
   * @param alphabet  字典字符数组
   * @param generator 随机数生成器
   * @return NanoID
   */
  public static String randomNanoId(final int size, @NonNull final char[] alphabet, @NonNull final Random generator) {
    if (size < 1) {
      throw new IllegalArgumentException("Size: must be greater than 0");
    }
    if (alphabet.length == 0 || alphabet.length >= 256) {
      throw new IllegalArgumentException("Alphabet: must contain between 1 and 255 symbols.");
    }

    int mask = (2 << (int) Math.floor(Math.log(alphabet.length - 1) / Math.log(2))) - 1;
    int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);
    final StringBuilder idBuilder = new StringBuilder(size);
    while (true) {
      final byte[] bytes = new byte[step];
      generator.nextBytes(bytes);
      for (int i = 0; i < step; i++) {
        int alphabetIndex = bytes[i] & mask;
        if (alphabetIndex < alphabet.length) {
          idBuilder.append(alphabet[alphabetIndex]);
          if (idBuilder.length() == size) {
            return idBuilder.toString();
          }
        }
      }
    }
  }

  /**
   * 生成 NanoID
   *
   * @param size      长度
   * @param alphabet  字典字符串
   * @param generator 随机数生成器
   * @return NanoID
   */
  public static String randomNanoId(final int size, @NonNull final String alphabet, @NonNull final Random generator) {
    return randomNanoId(size, alphabet.toCharArray(), generator);
  }

  /**
   * 生成 NanoID
   *
   * @param size     长度
   * @param alphabet 字典
   * @return NanoID
   */
  public static String randomNanoId(final int size, @NonNull char[] alphabet) {
    return randomNanoId(size, alphabet, DEFAULT_ID_GENERATOR);
  }

  /**
   * 生成 NanoID
   *
   * @param size     长度
   * @param alphabet 字典
   * @return NanoID
   */
  public static String randomNanoId(int size, String alphabet) {
    return randomNanoId(size, alphabet, DEFAULT_ID_GENERATOR);
  }

  /**
   * 生成 NanoID
   *
   * @param size 长度
   * @return NanoID
   */
  public static String randomNanoId(int size) {
    return randomNanoId(size, DEFAULT_ALPHABET, DEFAULT_ID_GENERATOR);
  }

  /**
   * 生成 NanoID
   *
   * @return NanoID
   */
  public static String randomNanoId() {
    return randomNanoId(DEFAULT_SIZE, DEFAULT_ALPHABET, DEFAULT_ID_GENERATOR);
  }
}
