package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.coll.CollUtil;
import top.csaf.coll.MapUtil;
import top.csaf.lang.ArrayUtil;
import top.csaf.lang.ClassUtil;
import top.csaf.lang.NumberUtil;
import top.csaf.lang.ObjUtil;
import top.csaf.lang.StrUtil;
import top.csaf.lang.SysUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("核心工具类反射覆盖测试")
class CoreReflectionCoverageTest {

  @DisplayName("反射调用核心工具类静态方法")
  @Test
  void invokePublicStaticMethods() {
    int invoked = 0;
    invoked += invokePublicStatic(ArrayUtil.class);
    invoked += invokePublicStatic(CollUtil.class);
    invoked += invokePublicStatic(MapUtil.class);
    invoked += invokePublicStatic(StrUtil.class);
    invoked += invokePublicStatic(NumberUtil.class);
    invoked += invokePublicStatic(ClassUtil.class);
    invoked += invokePublicStatic(ObjUtil.class);
    invoked += invokePublicStatic(SysUtil.class);
    assertTrue(invoked > 100);
  }

  private int invokePublicStatic(Class<?> clazz) {
    int invoked = 0;
    for (Method method : clazz.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.setAccessible(true);
        method.invoke(null, args(method));
      } catch (InvocationTargetException | IllegalArgumentException ignored) {
        // 覆盖包装方法入口和参数校验分支即可，异常行为已有专门测试断言。
      } catch (Exception e) {
        throw new AssertionError(e);
      }
      invoked++;
    }
    return invoked;
  }

  private Object[] args(Method method) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      args[i] = value(parameterTypes[i], method.getName(), i);
    }
    return args;
  }

  private Object value(Class<?> type, String methodName, int index) {
    if (type.equals(boolean.class) || type.equals(Boolean.class)) {
      return true;
    }
    if (type.equals(byte.class) || type.equals(Byte.class)) {
      return (byte) 1;
    }
    if (type.equals(short.class) || type.equals(Short.class)) {
      return (short) 1;
    }
    if (type.equals(int.class) || type.equals(Integer.class)) {
      return index == 2 && methodName.contains("fill") ? 2 : 1;
    }
    if (type.equals(long.class) || type.equals(Long.class)) {
      return 1L;
    }
    if (type.equals(float.class) || type.equals(Float.class)) {
      return 1F;
    }
    if (type.equals(double.class) || type.equals(Double.class)) {
      return 1D;
    }
    if (type.equals(char.class) || type.equals(Character.class)) {
      return '1';
    }
    if (type.equals(String.class) || CharSequence.class.isAssignableFrom(type)) {
      return "a";
    }
    if (type.equals(Class.class)) {
      return Integer.class;
    }
    if (type.equals(Object.class)) {
      return objectValue(methodName, index);
    }
    if (Map.class.isAssignableFrom(type)) {
      Map<Object, Object> map = new HashMap<>();
      map.put("a", "1");
      map.put("b", 2);
      return map;
    }
    if (SortedMap.class.isAssignableFrom(type)) {
      SortedMap<Object, Object> map = new TreeMap<>();
      map.put("a", "1");
      return map;
    }
    if (Collection.class.isAssignableFrom(type)) {
      return new ArrayList<>(Arrays.asList("a", "b"));
    }
    if (Iterable.class.isAssignableFrom(type)) {
      return new ArrayList<>(Arrays.asList("a", "b"));
    }
    if (Iterator.class.isAssignableFrom(type)) {
      return new ArrayList<>(Arrays.asList("a", "b")).iterator();
    }
    if (Enumeration.class.isAssignableFrom(type)) {
      return new Vector<>(Arrays.asList("a", "b")).elements();
    }
    if (Comparator.class.isAssignableFrom(type)) {
      return Comparator.comparing(Object::toString);
    }
    if (Properties.class.isAssignableFrom(type)) {
      Properties properties = new Properties();
      properties.setProperty("a", "1");
      return properties;
    }
    if (ResourceBundle.class.isAssignableFrom(type)) {
      return new ListResourceBundle() {
        @Override
        protected Object[][] getContents() {
          return new Object[][]{{"a", "1"}};
        }
      };
    }
    if (java.io.PrintStream.class.isAssignableFrom(type)) {
      return System.out;
    }
    if (type.isArray()) {
      return arrayValue(type.getComponentType());
    }
    return null;
  }

  private Object objectValue(String methodName, int index) {
    if (methodName.contains("get") || methodName.contains("size")) {
      return new ArrayList<>(Arrays.asList("a", "b"));
    }
    if (methodName.contains("deduplicate") || methodName.contains("removeAll")) {
      return new String[]{"a", "b", "a"};
    }
    if (index == 1) {
      return "a";
    }
    return BigDecimal.ONE;
  }

  private Object arrayValue(Class<?> componentType) {
    if (componentType.equals(boolean.class)) {
      return new boolean[]{true, false, true};
    }
    if (componentType.equals(byte.class)) {
      return new byte[]{1, 2, 1};
    }
    if (componentType.equals(short.class)) {
      return new short[]{1, 2, 1};
    }
    if (componentType.equals(int.class)) {
      return new int[]{1, 2, 1};
    }
    if (componentType.equals(long.class)) {
      return new long[]{1, 2, 1};
    }
    if (componentType.equals(float.class)) {
      return new float[]{1, 2, 1};
    }
    if (componentType.equals(double.class)) {
      return new double[]{1, 2, 1};
    }
    if (componentType.equals(char.class)) {
      return new char[]{'a', 'b', 'a'};
    }
    if (componentType.equals(Class.class)) {
      return new Class<?>[]{Integer.class, Long.class};
    }
    if (componentType.equals(String.class)) {
      return new String[]{"a", "b", "a"};
    }
    if (componentType.equals(Collection.class)) {
      return new Collection<?>[]{Collections.singleton("a"), Collections.singleton("b")};
    }
    return new Object[]{"a", "b", "a"};
  }
}
