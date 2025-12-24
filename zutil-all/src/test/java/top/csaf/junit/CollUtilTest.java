package top.csaf.junit;

import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.coll.CollUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("集合工具类测试")
class CollUtilTest {

  @DisplayName("生成调用 org.apache.commons.collections4.CollectionUtils 的方法代码")
  @Disabled
  @Test
  void genCollectionUtilsFn() {
    // 获取 org.apache.commons.collections4.CollectionUtils 中所有的方法名
    Class<?> clazz = org.apache.commons.collections4.CollectionUtils.class;
    Method[] methods = clazz.getDeclaredMethods();
    // 根据方法生成调用方法代码
    for (Method method : methods) {
      // 修饰符不为 public static 时跳过
      if (method.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC)) {
        continue;
      }
      // 有 @Deprecated 注解时跳过
      if (method.getAnnotation(Deprecated.class) != null) {
        continue;
      }
      // 参数名
      String[] paramNames = new org.springframework.core.DefaultParameterNameDiscoverer().getParameterNames(method);

      // 如果无法获取参数名（第三方Jar包通常没有debug信息），则生成默认参数名 arg0, arg1...
      if (paramNames == null) {
        int count = method.getParameterCount();
        paramNames = new String[count];
        for (int i = 0; i < count; i++) {
          paramNames[i] = "arg" + i;
        }
      }

      // 方法描述，比如 public static <K,V> java.util.Map$Entry<K, V> org.apache.commons.collections4.CollectionUtils.get(java.util.Map<K, V>,int)
      String genericStr = method.toGenericString();
      // TODO 获取方法返回值泛型的继承类，比如 public static <O extends Comparable<? super O>> List<O> collate(final Iterable<? extends O> a, final Iterable<? extends O> b) {
      // 将 java.util.Map$Entry<K, V> 改为 java.util.Map.Entry<K, V>
      genericStr = genericStr.replaceAll("\\$", ".");
      // 去除 org.apache.commons.collections4.CollectionUtils
      genericStr = genericStr.replace("org.apache.commons.collections4.CollectionUtils.", "");
      // 给参数类型加上参数名
      StringBuilder paramsStr = new StringBuilder();
      String paramsType = genericStr.substring(genericStr.indexOf("(") + 1, genericStr.indexOf(")"));
      if (cn.hutool.core.util.StrUtil.isNotBlank(paramsType)) {
        String[] paramTypes = paramsType.split(",");
        int j = 0;
        for (int i = 0; i < paramTypes.length; i++) {
          String param = paramTypes[i];
          // 如果为泛型，当前参数为当前参数 + 下一个参数
          if (param.contains("<") && !param.contains(">")) {
            param = param + "," + paramTypes[i + 1];
            i++;
          }
          // 此时 paramNames 已经过判空处理，不会报 NPE
          paramsStr.append(param).append(" ").append(paramNames[j]);
          if (i != paramTypes.length - 1) {
            paramsStr.append(", ");
          }
          j++;
        }
      }
      // 生成代码
      System.out.println(genericStr.replace(genericStr.substring(genericStr.indexOf("("), genericStr.indexOf(")") + 1), "(" + paramsStr + ")") + " {");
      System.out.println("  " + (method.getReturnType().getSimpleName().equals("void") ? "" : "return ") + "org.apache.commons.collections4.CollectionUtils." + method.getName() + "(" + cn.hutool.core.util.StrUtil.join(", ", (Object[]) paramNames) + ");");
      System.out.println("}\n");
    }
  }

  @DisplayName("是否 每个集合 都为 null 或 没有元素")
  @Test
  void isEmptys() {
    assertTrue(CollUtil.isEmptys(null));
    assertTrue(CollUtil.isEmptys(new ArrayList<>()));

    List<Object> list = new ArrayList<>();
    List<Object> list1 = new ArrayList<>();
    assertTrue(CollUtil.isEmptys(list, list1));
    list1.add(1);
    assertFalse(CollUtil.isEmptys(list, list1));
  }

  @DisplayName("是否不满足 每个集合 都为 null 或 没有元素")
  @Test
  void isNotEmptys() {
    assertFalse(CollUtil.isNotEmptys(null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    List<Object> list1 = new ArrayList<>();
    list1.add(1);
    assertTrue(CollUtil.isNotEmptys(list, list1));
    assertFalse(CollUtil.isNotEmptys(new ArrayList<>(), new ArrayList<>()));
  }

  @DisplayName("是否不满足 对象为 null 或 没有元素")
  @Test
  void sizeIsNotEmpty() {
    assertFalse(CollUtil.sizeIsNotEmpty(null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    assertTrue(CollUtil.sizeIsNotEmpty(list));
    assertFalse(CollUtil.sizeIsNotEmpty(new ArrayList<>()));
  }

  @DisplayName("是否 每个对象 都为 null 或 没有元素")
  @Test
  void sizeIsEmptys() {
    assertTrue(CollUtil.sizeIsEmptys(null));
    assertTrue(CollUtil.sizeIsEmptys(new ArrayList<>()));

    List<Object> list = new ArrayList<>();
    List<Object> list1 = new ArrayList<>();
    assertTrue(CollUtil.sizeIsEmptys(list, list1));
    list1.add(1);
    assertFalse(CollUtil.sizeIsEmptys(list, list1));
  }

  @DisplayName("是否不满足 每个对象 都为 null 或 没有元素")
  @Test
  void sizeIsNotEmptys() {
    assertFalse(CollUtil.sizeIsNotEmptys(null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    List<Object> list1 = new ArrayList<>();
    list1.add(1);
    assertTrue(CollUtil.sizeIsNotEmptys(list, list1));
    assertFalse(CollUtil.sizeIsNotEmptys(new ArrayList<>(), new ArrayList<>()));
  }

  @DisplayName("是否 每个对象的 所有元素都为 null 或 没有元素")
  @Test
  void isAllEmpty() {
    assertTrue(CollUtil.isAllEmpty(null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    assertFalse(CollUtil.isAllEmpty(list));
    assertFalse(CollUtil.isAllEmpty(list.iterator()));
    List<Object> emptyList = new ArrayList<>();
    assertTrue(CollUtil.isAllEmpty(emptyList.iterator()));
    emptyList.add(null);
    assertTrue(CollUtil.isAllEmpty(emptyList));
    assertTrue(CollUtil.isAllEmpty(emptyList.iterator()));

    Map<String, Object> map = new HashMap<>();
    map.put("1", 1);
    assertFalse(CollUtil.isAllEmpty(map));
    assertFalse(CollUtil.isAllEmpty(new Object[]{1}));

    assertFalse(CollUtil.isAllEmpty(new Vector<>(list).elements()));
    assertTrue(CollUtil.isAllEmpty(new Vector<>().elements()));
    assertTrue(CollUtil.isAllEmpty(new Vector<>(emptyList).elements()));
  }

  @DisplayName("是否不满足 对象所有元素都为 null 或 没有元素")
  @Test
  void isNotAllEmpty() {
    assertFalse(CollUtil.isNotAllEmpty(null));

    List<Object> list1 = new ArrayList<>();
    list1.add(1);
    assertTrue(CollUtil.isNotAllEmpty(list1));
    assertFalse(CollUtil.isNotAllEmpty(new ArrayList<>()));
  }

  @DisplayName("是否 每个对象的 所有元素都为 null 或 没有元素")
  @Test
  void isAllEmptys() {
    assertTrue(CollUtil.isAllEmptys(null));
    assertTrue(CollUtil.isAllEmptys(new ArrayList<>()));

    List<Object> list = new ArrayList<>();
    list.add(1);
    Object[] array = {1};
    assertFalse(CollUtil.isAllEmptys(list, array));
    assertTrue(CollUtil.isAllEmptys(new ArrayList<>(), new Object[]{}));
  }

  @DisplayName("是否不满足 每个对象的 所有元素都为 null 或 没有元素")
  @Test
  void isNotAllEmptys() {
    assertFalse(CollUtil.isNotAllEmptys(null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    Object[] array = {1};
    assertTrue(CollUtil.isNotAllEmptys(list, array));
    assertFalse(CollUtil.isNotAllEmptys(new ArrayList<>(), new Object[]{}));
  }

  @DisplayName("是否 任意对象 为 null 或 没有元素 或 任意元素为 null")
  @Test
  void isAnyEmpty() {
    assertTrue(CollUtil.isAnyEmpty(null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    assertFalse(CollUtil.isAnyEmpty(list));
    list.add(null);
    assertTrue(CollUtil.isAnyEmpty(list));
    assertTrue(CollUtil.isAnyEmpty(list.iterator()));
    Map<String, Object> map = new HashMap<>();
    map.put("1", 1);
    map.put("2", null);
    assertTrue(CollUtil.isAnyEmpty(map));
    assertTrue(CollUtil.isAnyEmpty(new Object[]{null}));
    assertTrue(CollUtil.isAnyEmpty(new Vector<>(list).elements()));
  }

  @DisplayName("是否不满足 任意对象 为 null 或 没有元素 或 任意元素为 null")
  @Test
  void isNotAnyEmpty() {
    List<Object> list = new ArrayList<>();
    assertFalse(CollUtil.isNotAnyEmpty(list));
    list.add(1);
    assertTrue(CollUtil.isNotAnyEmpty(list));
  }

  @DisplayName("是否 任意对象 为 null 或 没有元素 或 任意元素为 null")
  @Test
  void isAnyEmptys() {
    assertTrue(CollUtil.isAnyEmptys(null));
    List<Object> list = new ArrayList<>();
    assertTrue(CollUtil.isAnyEmptys(list));
    list.add(1);
    assertTrue(CollUtil.isAnyEmptys(list, new Object[]{}));
    assertFalse(CollUtil.isAnyEmptys(list, new Object[]{1}));
  }

  @DisplayName("是否不满足 任意对象 为 null 或 没有元素 或 任意元素为 null")
  @Test
  void isNotAnyEmptys() {
    assertTrue(CollUtil.isNotAnyEmptys(new Object[]{1}, new Object[]{1}));
    assertFalse(CollUtil.isNotAnyEmptys(null));
  }

  @DisplayName("是否 每个对象的每个元素都相等")
  @Test
  void isAllEquals() {
    /** {@link CollUtil#isAllEquals(boolean, Function, Object...) } */
    assertThrows(NullPointerException.class, () -> CollUtil.isAllEquals(true, null, null));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.isAllEquals(true, null, new Object[]{}));

    List<Object> list = new ArrayList<>();
    list.add("1");
    List<Object> list1 = new ArrayList<>();
    list1.add(new BigInteger("1"));
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(1);
    jsonArray.add('1');
    Map<String, Object> map = new HashMap<>();
    map.put("1", new BigDecimal("1"));
    Object[] array = new Object[]{1.0};
    Vector<Object> vector = new Vector<>();
    vector.add(1.0f);
    assertTrue(CollUtil.isAllEquals(true, CollUtil::sizeIsEmpty, list, null, list1.iterator(), jsonArray, map, array, vector.elements()));
    // continueFunction 为 null，后续判断到 Iterator。数组排在第一个用于填充上一个元素。
    assertTrue(CollUtil.isAllEquals(true, null, array, list1.iterator()));
    // continueFunction 为 null，后续判断到 Enumeration。Enumeration 排在第一个用于填充上一个元素。
    assertTrue(CollUtil.isAllEquals(true, null, vector.elements(), list));
    // Iterable 元素不一致结束
    list.set(0, 2);
    assertFalse(CollUtil.isAllEquals(true, null, list, list1));
    // 数组元素不一致结束
    array[0] = 2;
    assertFalse(CollUtil.isAllEquals(true, null, list1, array));
    // Enumeration 元素不一致结束
    vector.set(0, 2);
    assertFalse(CollUtil.isAllEquals(true, null, list1, vector.elements()));
    // 基础类型数组
    assertFalse(CollUtil.isAllEquals(true, null, new int[]{1}, new long[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new long[]{1}, new int[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new double[]{1}, new float[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new float[]{1}, new double[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new char[]{'1'}, new byte[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new byte[]{1}, new char[]{'2'}));
    assertFalse(CollUtil.isAllEquals(true, null, new boolean[]{true}, new short[]{1}));
    assertFalse(CollUtil.isAllEquals(true, null, new short[]{1}, new boolean[]{true}));

    // isToString = false
    assertFalse(CollUtil.isAllEquals(false, null, list, null));

    /** {@link CollUtil#isAllEquals(Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isAllEquals(null, null));
    assertFalse(CollUtil.isAllEquals(null, new ArrayList<>(), null));
  }

  @DisplayName("是否不满足 每个对象的每个元素都相等")
  @Test
  void isNotAllEquals() {
    /** {@link CollUtil#isNotAllEquals(boolean, Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isNotAllEquals(true, null, null));

    List<Object> list = new ArrayList<>();
    list.add(1);
    List<Object> list1 = new ArrayList<>();
    list1.add(2);
    assertTrue(CollUtil.isNotAllEquals(true, null, list, list1));

    /** {@link CollUtil#isNotAllEquals(Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isNotAllEquals(null, null));
    assertTrue(CollUtil.isNotAllEquals(null, new ArrayList<>(), null));
  }

  @DisplayName("是否 每个对象的同一位置的元素都相等")
  @Test
  void isAllEqualsSameIndex() {
    /** {@link CollUtil#isAllEqualsSameIndex(boolean, Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isAllEqualsSameIndex(true, null, null));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.isAllEqualsSameIndex(true, null, new ArrayList<>()));

    List<Object> list = new ArrayList<>();
    list.add("1");
    list.add(2);
    list.add(new BigDecimal("3.0"));
    List<Object> list1 = new ArrayList<>();
    list1.add(new BigInteger("1"));
    list1.add(2.0f);
    list1.add(3L);
    JsonArray jsonArray = new JsonArray();
    jsonArray.add('1');
    jsonArray.add(2.0);
    jsonArray.add(3);
    Map<String, Object> map = new HashMap<>();
    map.put("1", '1');
    map.put("2", 2.0);
    map.put("3", 3);
    Object[] array = new Object[]{'1', 2.0, 3};
    Vector<Object> vector = new Vector<>();
    vector.add('1');
    vector.add(2.0);
    vector.add(3);
    assertTrue(CollUtil.isAllEqualsSameIndex(true, CollUtil::sizeIsEmpty, list, null, list1.iterator(), jsonArray, map, array, vector.elements()));
    // 长度不一致判断结束
    list.remove(2);
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new ArrayList<>(), list));
    // 列表元素不一致结束
    list.add(4);
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, list, list1.iterator()));
    // 数组排在第一个用于填充上一次列表
    assertTrue(CollUtil.isAllEqualsSameIndex(true, CollUtil::sizeIsEmpty, array, list1));
    // 数组元素不一致结束
    array[2] = 4;
    assertFalse(CollUtil.isAllEqualsSameIndex(true, CollUtil::sizeIsEmpty, list1, array));
    // 基础类型数组
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new long[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new long[]{1}, new int[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new double[]{1}, new float[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new float[]{1}, new double[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new char[]{'1'}, new byte[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new byte[]{1}, new char[]{'2'}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new boolean[]{true}, new short[]{1}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new short[]{1}, new boolean[]{true}));
    // 基础类型数组元素相等
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new double[]{1}));
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new float[]{1}));
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new char[]{'1'}));
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new byte[]{1}));
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new boolean[]{true}, new boolean[]{true}));
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new short[]{1}));

    /** {@link CollUtil#isAllEqualsSameIndex(Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isAllEqualsSameIndex(null, null));
    assertFalse(CollUtil.isAllEqualsSameIndex(null, new ArrayList<>(), null));
  }

  @DisplayName("是否不满足 每个对象的同一位置的元素都相等")
  @Test
  void isNotAllEqualsSameIndex() {
    /** {@link CollUtil#isNotAllEqualsSameIndex(boolean, Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isNotAllEqualsSameIndex(true, null, null));

    List<Object> list = new ArrayList<>();
    list.add("1");
    list.add(2);
    list.add(new BigDecimal("3.0"));
    List<Object> list1 = new ArrayList<>();
    list1.add(new BigInteger("1"));
    list1.add(2.0f);
    list1.add(3L);
    assertFalse(CollUtil.isNotAllEqualsSameIndex(true, null, list, list1));

    /** {@link CollUtil#isNotAllEqualsSameIndex(Function, Object...)} */
    assertThrows(NullPointerException.class, () -> CollUtil.isNotAllEqualsSameIndex(null, null));
    assertTrue(CollUtil.isNotAllEqualsSameIndex(null, new ArrayList<>(), null));
  }
}
