package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.crypto.BlockCipher;
import top.csaf.crypto.BlockCipherUtil;
import top.csaf.crypto.enums.BlockCipherType;
import top.csaf.crypto.enums.EncodingType;
import top.csaf.crypto.enums.Mode;
import top.csaf.crypto.enums.Padding;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分组密码补充测试")
class BlockCipherCoverageTest {

  @DisplayName("构造、Builder 和编解码分支")
  @Test
  void builderAndCodecBranches() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> new BlockCipher(null, null, null, null, null));
    BlockCipher des = new BlockCipher(BlockCipherType.DES, null, null, null, null);
    assertEquals(BlockCipherType.DES, des.getType());
    assertEquals(8, des.getKeyLength());
    assertEquals(8, des.getIvLength());
    BlockCipher aes = new BlockCipher(BlockCipherType.AES, 16, 16, EncodingType.UTF_8, EncodingType.UTF_8);
    assertEquals(16, aes.getKeyLength());
    assertEquals(EncodingType.UTF_8, aes.getKeyEncoding());
    aes.setIvEncoding(EncodingType.UTF_8);
    assertEquals(EncodingType.UTF_8, aes.getIvEncoding());

    BlockCipher sm4 = BlockCipher.builder(BlockCipherType.SM4).keyLength(16).ivLength(16).keyEncoding(EncodingType.UTF_8).ivEncoding(EncodingType.UTF_8).build();
    assertEquals(BlockCipherType.SM4, sm4.getType());

    Method encode = BlockCipher.class.getDeclaredMethod("encode", byte[].class, EncodingType.class);
    encode.setAccessible(true);
    assertEquals("abc", encode.invoke(aes, "abc".getBytes(), null));
    assertEquals("abc", encode.invoke(aes, "abc".getBytes(), EncodingType.UTF_8));
    assertEquals("YWJj", encode.invoke(aes, "abc".getBytes(), EncodingType.BASE_64));
    assertEquals("616263", encode.invoke(aes, "abc".getBytes(), EncodingType.HEX));

    Method decode = BlockCipher.class.getDeclaredMethod("decode", String.class, EncodingType.class);
    decode.setAccessible(true);
    assertArrayEquals(new byte[0], (byte[]) decode.invoke(aes, "", null));
    assertArrayEquals("abc".getBytes(), (byte[]) decode.invoke(aes, "abc", null));
    assertArrayEquals("abc".getBytes(), (byte[]) decode.invoke(aes, "abc", EncodingType.UTF_8));
    assertArrayEquals("abc".getBytes(), (byte[]) decode.invoke(aes, "YWJj", EncodingType.BASE_64));
    assertArrayEquals("abc".getBytes(), (byte[]) decode.invoke(aes, "616263", EncodingType.HEX));

    Method decodeAndPad = BlockCipher.class.getDeclaredMethod("decodeAndPad", Object.class, EncodingType.class, int.class);
    decodeAndPad.setAccessible(true);
    assertArrayEquals(new byte[]{'a', 0, 0}, (byte[]) decodeAndPad.invoke(aes, "a", null, 3));
    assertThrows(Exception.class, () -> decodeAndPad.invoke(aes, 1, null, 3));

    Method encrypt = BlockCipher.class.getDeclaredMethod("encrypt", Object.class, Object.class, Object.class, Mode.class, Padding.class, EncodingType.class);
    encrypt.setAccessible(true);
    Method decrypt = BlockCipher.class.getDeclaredMethod("decrypt", Object.class, Object.class, Object.class, Mode.class, Padding.class, EncodingType.class);
    decrypt.setAccessible(true);
    assertEquals("hello", decrypt.invoke(aes, encrypt.invoke(aes, "hello".getBytes(), "1234567890123456".getBytes(), null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64), "1234567890123456", null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64));
    String encrypted = (String) encrypt.invoke(aes, "hello", "1234567890123456", "1234567890123456", Mode.CBC, Padding.PKCS7, EncodingType.BASE_64);
    assertEquals("hello", decrypt.invoke(aes, encrypted, "1234567890123456", "1234567890123456", Mode.CBC, Padding.PKCS7, EncodingType.BASE_64));
    assertThrows(Exception.class, () -> encrypt.invoke(aes, new Object(), "1234567890123456", null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64));
  }

  @DisplayName("BlockCipherUtil：全部重载和异常分支")
  @Test
  void blockCipherUtilOverloads() throws Exception {
    String key = "1234567890123456";
    String iv = "1234567890123456";
    String data = "hello";
    String base64 = BlockCipherUtil.encryptBase64(BlockCipherType.AES, data, key, iv, Mode.CBC, Padding.PKCS7);
    assertEquals(data, BlockCipherUtil.decryptBase64(BlockCipherType.AES, base64, key, iv, Mode.CBC, Padding.PKCS7));
    String base64Full = BlockCipherUtil.encryptBase64(BlockCipherType.AES, data, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7);
    assertEquals(data, BlockCipherUtil.decryptBase64(BlockCipherType.AES, base64Full, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7));
    String hex = BlockCipherUtil.encryptHex(BlockCipherType.AES, data, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7);
    assertEquals(data, BlockCipherUtil.decryptHex(BlockCipherType.AES, hex, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7));
    String hexSimple = BlockCipherUtil.encryptHex(BlockCipherType.AES, data, key, iv, Mode.CBC, Padding.PKCS7);
    assertEquals(data, BlockCipherUtil.decryptHex(BlockCipherType.AES, hexSimple, key, iv, Mode.CBC, Padding.PKCS7));
    String raw = BlockCipherUtil.encrypt(BlockCipherType.AES, data, key, iv, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64);
    assertEquals(data, BlockCipherUtil.decrypt(BlockCipherType.AES, raw, key, iv, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64));
    String rawFull = BlockCipherUtil.encrypt(BlockCipherType.AES, data, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64);
    assertEquals(data, BlockCipherUtil.decrypt(BlockCipherType.AES, rawFull, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64));
    assertThrows(NullPointerException.class, () -> BlockCipherUtil.encrypt(BlockCipherType.AES, data, key, iv, null, Padding.PKCS7));
    assertThrows(NullPointerException.class, () -> BlockCipherUtil.encrypt(BlockCipherType.AES, data, key, iv, Mode.ECB, null));
    assertThrows(NullPointerException.class, () -> BlockCipherUtil.encrypt(BlockCipherType.AES, data, null, iv, Mode.ECB, Padding.PKCS7));
    assertThrows(NullPointerException.class, () -> BlockCipherUtil.decrypt(BlockCipherType.AES, raw, null, iv, Mode.ECB, Padding.PKCS7));
    assertThrows(NullPointerException.class, () -> BlockCipherUtil.decrypt(BlockCipherType.AES, raw, key, iv, null, Padding.PKCS7));
    assertThrows(NullPointerException.class, () -> BlockCipherUtil.decrypt(BlockCipherType.AES, raw, key, iv, Mode.ECB, null));
    assertThrows(IllegalArgumentException.class, () -> BlockCipherUtil.encrypt(BlockCipherType.AES, "123", key, iv, Mode.CBC, Padding.NO));

    int invoked = 0;
    for (Method method : BlockCipherUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      method.invoke(null, args(method.getName(), method.getParameterTypes()));
      assertBlockCipherUtilNullGuards(method);
      invoked++;
    }
    assertTrue(invoked >= 16);
  }

  private void assertBlockCipherUtilNullGuards(Method method) {
    Class<?>[] types = method.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      if (!isBlockCipherUtilNonNullParam(method.getName(), types, i)) {
        continue;
      }
      Object[] args = args(method.getName(), types);
      args[i] = null;
      assertThrows(Exception.class, () -> method.invoke(null, args));
    }
  }

  private boolean isBlockCipherUtilNonNullParam(String methodName, Class<?>[] types, int index) {
    return types[index].equals(Mode.class) || types[index].equals(Padding.class) || types[index].equals(String.class) && (index == 2 || methodName.startsWith("encrypt") && index == 1);
  }

  private Object[] args(String methodName, Class<?>[] types) {
    Object[] args = new Object[types.length];
    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];
      if (type.equals(BlockCipherType.class)) {
        args[i] = BlockCipherType.AES;
      } else if (type.equals(String.class)) {
        if (i == 1 && methodName.startsWith("decryptBase64")) {
          args[i] = BlockCipherUtil.encryptBase64(BlockCipherType.AES, "hello", "1234567890123456", "1234567890123456", Mode.ECB, Padding.PKCS7);
        } else if (i == 1 && methodName.startsWith("decryptHex")) {
          args[i] = BlockCipherUtil.encryptHex(BlockCipherType.AES, "hello", "1234567890123456", "1234567890123456", Mode.ECB, Padding.PKCS7);
        } else if (i == 1 && methodName.startsWith("decrypt")) {
          args[i] = BlockCipherUtil.encrypt(BlockCipherType.AES, "hello", "1234567890123456", "1234567890123456", Mode.ECB, Padding.PKCS7, EncodingType.BASE_64);
        } else if (i == 2) {
          args[i] = "1234567890123456";
        } else if (i == 4) {
          args[i] = "1234567890123456";
        } else {
          args[i] = "hello";
        }
      } else if (type.equals(EncodingType.class)) {
        args[i] = i == types.length - 1 ? EncodingType.BASE_64 : null;
      } else if (type.equals(Mode.class)) {
        args[i] = Mode.ECB;
      } else if (type.equals(Padding.class)) {
        args[i] = Padding.PKCS7;
      }
    }
    return args;
  }
}
