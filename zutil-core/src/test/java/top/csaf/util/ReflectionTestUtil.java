package top.csaf.util;

import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 测试工具类
 */
public class ReflectionTestUtil extends org.springframework.test.util.ReflectionTestUtils {

  /**
   * 调用方法
   *
   * @param targetClass    目标类
   * @param name           方法名
   * @param parameterTypes 参数类型
   * @param args           参数
   * @param <T>            返回值类型
   * @return 返回值
   * @throws Throwable 异常
   */
  public static <T> T invokeMethod(@NonNull Class<?> targetClass, String name, Class<?>[] parameterTypes, Object... args) throws Throwable {
    // 根据方法名和参数类型获取方法
    Method method = targetClass.getDeclaredMethod(name, parameterTypes);
    method.setAccessible(true);
    T t;
    try {
      t = (T) method.invoke(targetClass, args);
    } catch (InvocationTargetException e) {
      // 抛出异常
      throw e.getTargetException();
    }
    return t;
  }
}
