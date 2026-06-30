package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.crypto.AesUtil;
import top.csaf.crypto.DesUtil;
import top.csaf.crypto.Sm4Util;
import top.csaf.crypto.enums.EncodingType;
import top.csaf.crypto.enums.Mode;
import top.csaf.crypto.enums.Padding;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("具体分组密码工具反射覆盖测试")
class CipherWrapperCoverageTest {

  @Test
  void invokeAllCipherWrappers() {
    assertAllWrapperMethods(AesUtil.class, "1234567890123456", "1234567890123456");
    assertAllWrapperMethods(DesUtil.class, "12345678", "12345678");
    assertAllWrapperMethods(Sm4Util.class, "1234567890123456", "1234567890123456");
  }

  @Test
  void directWrapperOverloads() {
    assertCipher(AesUtil.class, "1234567890123456", "1234567890123456");
    assertCipher(DesUtil.class, "12345678", "12345678");
    assertCipher(Sm4Util.class, "1234567890123456", "1234567890123456");
  }

  @Test
  void directNullEncodingBranches() {
    assertNullEncodingCipher(AesUtil.class, "1234567890123456", "1234567890123456");
    assertNullEncodingCipher(DesUtil.class, "12345678", "12345678");
    assertNullEncodingCipher(Sm4Util.class, "1234567890123456", "1234567890123456");
  }

  @Test
  void wrapperNonNullGuards() {
    assertWrapperNullGuards(AesUtil.class, "1234567890123456", "1234567890123456");
    assertWrapperNullGuards(DesUtil.class, "12345678", "12345678");
    assertWrapperNullGuards(Sm4Util.class, "1234567890123456", "1234567890123456");
  }

  private void assertCipher(Class<?> clazz, String key, String iv) {
    try {
      String base64 = (String) clazz.getMethod("encryptBase64", String.class, String.class, String.class, Mode.class, Padding.class).invoke(null, "hello", key, iv, Mode.CBC, Padding.PKCS7);
      assertEquals("hello", clazz.getMethod("decryptBase64", String.class, String.class, String.class, Mode.class, Padding.class).invoke(null, base64, key, iv, Mode.CBC, Padding.PKCS7));
      String base64Full = (String) clazz.getMethod("encryptBase64", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class).invoke(null, "hello", key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7);
      assertEquals("hello", clazz.getMethod("decryptBase64", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class).invoke(null, base64Full, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7));
      String hex = (String) clazz.getMethod("encryptHex", String.class, String.class, String.class, Mode.class, Padding.class).invoke(null, "hello", key, iv, Mode.CBC, Padding.PKCS7);
      assertEquals("hello", clazz.getMethod("decryptHex", String.class, String.class, String.class, Mode.class, Padding.class).invoke(null, hex, key, iv, Mode.CBC, Padding.PKCS7));
      String hexFull = (String) clazz.getMethod("encryptHex", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class).invoke(null, "hello", key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7);
      assertEquals("hello", clazz.getMethod("decryptHex", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class).invoke(null, hexFull, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.CBC, Padding.PKCS7));
      String encoded = (String) clazz.getMethod("encrypt", String.class, String.class, String.class, Mode.class, Padding.class, EncodingType.class).invoke(null, "hello", key, iv, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64);
      assertEquals("hello", clazz.getMethod("decrypt", String.class, String.class, String.class, Mode.class, Padding.class, EncodingType.class).invoke(null, encoded, key, iv, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64));
      String encodedFull = (String) clazz.getMethod("encrypt", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class, EncodingType.class).invoke(null, "hello", key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64);
      assertEquals("hello", clazz.getMethod("decrypt", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class, EncodingType.class).invoke(null, encodedFull, key, EncodingType.UTF_8, iv, EncodingType.UTF_8, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64));
      assertThrows(Exception.class, () -> clazz.getMethod("encrypt", String.class, String.class, String.class, Mode.class, Padding.class).invoke(null, "123", key, iv, Mode.CBC, Padding.NO));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private void assertNullEncodingCipher(Class<?> clazz, String key, String iv) {
    try {
      for (Mode mode : new Mode[]{Mode.CBC, Mode.CTR, Mode.CFB, Mode.OFB}) {
        String encrypted = (String) clazz.getMethod("encrypt", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class, EncodingType.class)
          .invoke(null, "hello", key, null, iv, null, mode, Padding.PKCS7, EncodingType.BASE_64);
        assertEquals("hello", clazz.getMethod("decrypt", String.class, String.class, EncodingType.class, String.class, EncodingType.class, Mode.class, Padding.class, EncodingType.class)
          .invoke(null, encrypted, key, null, iv, null, mode, Padding.PKCS7, EncodingType.BASE_64));
      }
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private void assertWrapperNullGuards(Class<?> clazz, String key, String iv) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      Class<?>[] types = method.getParameterTypes();
      for (int i = 0; i < types.length; i++) {
        if (!isWrapperNonNullParam(types, i)) {
          continue;
        }
        Object[] args = args(clazz, method.getName(), types, key, iv);
        args[i] = null;
        assertThrows(Exception.class, () -> method.invoke(null, args));
      }
    }
  }

  private boolean isWrapperNonNullParam(Class<?>[] types, int index) {
    return types[index].equals(Mode.class) || types[index].equals(Padding.class) || types[index].equals(String.class) && index == 1;
  }

  private void assertAllWrapperMethods(Class<?> clazz, String key, String iv) {
    List<String> failed = new ArrayList<>();
    int invoked = 0;
    for (Method method : clazz.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.invoke(null, args(clazz, method.getName(), method.getParameterTypes(), key, iv));
        invoked++;
      } catch (Exception e) {
        failed.add(method.toGenericString() + ": " + e.getClass().getSimpleName());
      }
    }
    assertEquals(new ArrayList<>(), failed);
    assertTrue(invoked >= 16);
  }

  private Object[] args(Class<?> clazz, String methodName, Class<?>[] types, String key, String iv) {
    Object[] args = new Object[types.length];
    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];
      if (type.equals(String.class)) {
        if (i == 0) {
          args[i] = methodName.contains("decryptBase64") ? encryptedBase64(clazz, key, iv) : methodName.contains("decryptHex") ? encryptedHex(clazz, key, iv) : "hello";
        } else if (i == 1) {
          args[i] = key;
        } else if (i == 2) {
          args[i] = types.length > 3 && types[2].equals(EncodingType.class) ? iv : iv;
        } else {
          args[i] = iv;
        }
      } else if (type.equals(EncodingType.class)) {
        args[i] = EncodingType.UTF_8;
      } else if (type.equals(Mode.class)) {
        args[i] = Mode.ECB;
      } else if (type.equals(Padding.class)) {
        args[i] = Padding.PKCS7;
      }
    }
    return args;
  }

  private String encryptedBase64(Class<?> clazz, String key, String iv) {
    try {
      return (String) clazz.getMethod("encryptBase64", String.class, String.class, String.class, Mode.class, Padding.class)
        .invoke(null, "hello", key, iv, Mode.ECB, Padding.PKCS7);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private String encryptedHex(Class<?> clazz, String key, String iv) {
    try {
      return (String) clazz.getMethod("encryptHex", String.class, String.class, String.class, Mode.class, Padding.class)
        .invoke(null, "hello", key, iv, Mode.ECB, Padding.PKCS7);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
