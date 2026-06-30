package top.csaf.junit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.MultiValueMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import top.csaf.lang.StrUtil;
import top.csaf.coll.MapUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static top.csaf.coll.MapUtil.*;

@Slf4j
@DisplayName("MapUtil 工具类测试")
public class MapUtilTest {

  // public static void main(String[] args) {
  //   genMapUtilsFn();
  // }

  /**
   * 调用 {@link org.apache.commons.collections4.MapUtils } 的方法
   */
  static void genMapUtilsFn() {
    // 获取 org.apache.commons.collections4.MapUtils 中所有的方法名
    Class<?> clazz = org.apache.commons.collections4.MapUtils.class;
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
      String[] paramNames = new DefaultParameterNameDiscoverer().getParameterNames(method);

      // 方法描述，比如 public static <K,V> java.util.Map$Entry<K, V> org.apache.commons.collections4.MapUtils.get(java.util.Map<K, V>,int)
      String genericStr = method.toGenericString();
      // TODO 获取方法返回值泛型的继承类，比如 public static <O extends Comparable<? super O>> List<O> collate(final Iterable<? extends O> a, final Iterable<? extends O> b) {
      // 将 java.util.Map$Entry<K, V> 改为 java.util.Map.Entry<K, V>
      genericStr = genericStr.replaceAll("\\$", ".");
      // 去除 org.apache.commons.collections4.MapUtils
      genericStr = genericStr.replace("org.apache.commons.collections4.MapUtils.", "");
      // 给参数类型加上参数名
      StringBuilder paramsStr = new StringBuilder();
      String paramsType = genericStr.substring(genericStr.indexOf("(") + 1, genericStr.indexOf(")"));
      if (StrUtil.isNotBlank(paramsType)) {
        String[] paramTypes = paramsType.split(",");
        int j = 0;
        for (int i = 0; i < paramTypes.length; i++) {
          String param = paramTypes[i];
          // 如果为泛型，当前参数为当前参数 + 下一个参数
          if (param.contains("<") && !param.contains(">")) {
            param = param + "," + paramTypes[i + 1];
            i++;
          }
          paramsStr.append(param).append(" ").append(paramNames[j]);
          if (i != paramTypes.length - 1) {
            paramsStr.append(", ");
          }
          j++;
        }
      }
      // 生成代码
      System.out.println(genericStr.replace(genericStr.substring(genericStr.indexOf("("), genericStr.indexOf(")") + 1), "(" + paramsStr + ")") + " {");
      System.out.println("  " + (method.getReturnType().getSimpleName().equals("void") ? "" : "return ") + "org.apache.commons.collections4.MapUtils." + method.getName() + "(" + StrUtil.join(paramNames, ", ") + ");");
      System.out.println("}\n");
    }
  }

  @DisplayName("空 Map 和 null key 数组分支")
  @Test
  void testEmptyMapAndNullKeysBranches() {
    Map<String, Integer> empty = new HashMap<>();

    assertTrue(containsKeys(empty, "a").isEmpty());
    assertTrue(containsKeys(new HashMap<String, Integer>(), (String[]) null).isEmpty());
    assertNull(containsKeysFirst(empty, "a"));
    assertNull(containsKeysFirst(new HashMap<String, Integer>(), (String[]) null));
    assertTrue(getAll(empty, true, "a").isEmpty());
    assertTrue(getAll(new HashMap<String, Integer>(), true, (String[]) null).isEmpty());
    assertTrue(getAllNotNull(empty, "a").isEmpty());
    assertTrue(getAllNotNull(new HashMap<String, Integer>(), (String[]) null).isEmpty());
    assertNull(getAny(empty, "a"));
    assertNull(getAny(new HashMap<String, Integer>(), (String[]) null));
    assertNull(getAnyNotNull(empty, "a"));
    assertNull(getAnyNotNull(new HashMap<String, Integer>(), (String[]) null));
  }
  @DisplayName("containsKeys：根据 Key 数组从 Map 中获取包含的 Key 列表")
  @Test
  void testContainsKeys() {
    // 准备测试数据
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", 3);

    // 测试正常情况
    List<String> result = containsKeys(map, "a", "c", "d");
    assertEquals(2, result.size());
    assertTrue(result.contains("a"));
    assertTrue(result.contains("c"));
    assertFalse(result.contains("d"));

    // 测试空 key 数组
    result = containsKeys(map);
    assertEquals(0, result.size());

    // 测试 map 为 null
    result = containsKeys(null, "a", "b");
    assertEquals(0, result.size());
  }

  @DisplayName("containsKeysFirst：根据 Key 数组从 Map 中获取第一个包含的 Key")
  @Test
  void testContainsKeysFirst() {
    // 准备测试数据
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", 3);

    // 测试正常情况
    assertEquals("b", containsKeysFirst(map, "b", "a", "d"));
    assertEquals("c", containsKeysFirst(map, "d", "c", "a"));

    // 测试都不包含的情况
    assertNull(containsKeysFirst(map, "d", "e"));

    // 测试空 key 数组
    assertNull(containsKeysFirst(map));

    // 测试 map 为 null
    assertNull(containsKeysFirst(null, "a", "b"));
  }

  @DisplayName("getAll：根据 Key 数组从 Map 中获取多个 Value")
  @Test
  void testGetAll() {
    // 准备测试数据
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", null);

    // 测试有 isAddNull 参数的方法，isAddNull=true
    List<Integer> result = getAll(map, true, "a", "d", "c", "b");
    assertEquals(4, result.size());
    assertEquals(1, result.get(0)); // a->1
    assertNull(result.get(1));      // d->null (不存在，添加null)
    assertNull(result.get(2));      // c->null (存在，值为null)
    assertEquals(2, result.get(3)); // b->2

    // 测试有 isAddNull 参数的方法，isAddNull=false
    result = getAll(map, false, "a", "d", "c", "b");
    assertEquals(3, result.size());
    assertEquals(1, result.get(0)); // a->1
    assertNull(result.get(1));      // c->null
    assertEquals(2, result.get(2)); // b->2

    // 测试无 isAddNull 参数的方法（默认false）
    result = getAll(map, "a", "d", "c", "b");
    assertEquals(3, result.size());

    // 测试空 key 数组
    result = getAll(map, true);
    assertEquals(0, result.size());

    // 测试 map 为 null
    result = getAll((Map<String, Integer>) null, true, "a", "b");
    assertEquals(0, result.size());
  }

  @DisplayName("getAllNotNull：根据 Key 数组从 Map 中获取 Value 列表（不包含 null）")
  @Test
  void testGetAllNotNull() {
    // 准备测试数据
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", null);

    // 测试正常情况
    List<Integer> result = getAllNotNull(map, "a", "c", "d", "b");
    assertEquals(2, result.size());
    assertEquals(1, result.get(0)); // a->1
    assertEquals(2, result.get(1)); // b->2

    // 测试空 key 数组
    result = getAllNotNull(map);
    assertEquals(0, result.size());

    // 测试 map 为 null
    result = getAllNotNull(null, "a", "b");
    assertEquals(0, result.size());
  }

  @DisplayName("getAny：根据 Key 数组从 Map 中获取第一个符合的 Value")
  @Test
  void testGetAny() {
    // 准备测试数据
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", null);

    // 测试正常情况
    assertEquals(1, getAny(map, "a", "b"));
    assertEquals(2, getAny(map, "d", "b", "a"));
    assertNull(getAny(map, "c", "d")); // c 存在但值为 null

    // 测试都不包含的情况
    assertNull(getAny(map, "d", "e"));

    // 测试空 key 数组
    assertNull(getAny(map));

    // 测试 map 为 null
    assertNull(getAny(null, "a", "b"));
  }

  @DisplayName("getAnyNotNull：根据 Key 数组从 Map 中获取第一个符合的 Value（不包含 null）")
  @Test
  void testGetAnyNotNull() {
    // 准备测试数据
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", null);

    // 测试正常情况
    assertEquals(1, getAnyNotNull(map, "a", "b"));
    assertEquals(2, getAnyNotNull(map, "c", "b", "a")); // c 的值为 null，跳过

    // 测试都不含有非null值的情况
    assertNull(getAnyNotNull(map, "c", "d", "e"));

    // 测试空 key 数组
    assertNull(getAnyNotNull(map));

    // 测试 map 为 null
    assertNull(getAnyNotNull(null, "a", "b"));
  }

  @DisplayName("MapUtil 代理方法补充覆盖")
  @Test
  void testWrapperMethods() {
    TreeMap<String, Integer> sortedMap = new TreeMap<>();
    sortedMap.put("a", 1);
    assertThrows(UnsupportedOperationException.class, () -> unmodifiableSortedMap(sortedMap).put("b", 2));
    assertEquals(1, synchronizedSortedMap(sortedMap).get("a"));
    assertThrows(IllegalArgumentException.class, () -> predicatedSortedMap(sortedMap, key -> key.length() == 1, value -> value > 0).put("bb", 2));
    SortedMap<String, Integer> transformed = transformedSortedMap(new TreeMap<String, Integer>(), String::trim, value -> value * 2);
    assertNull(transformed.put(" a ", 1));
    assertEquals(Integer.valueOf(2), transformed.get("a"));

    Map<String, Integer> map = new HashMap<>();
    assertEquals(Integer.valueOf(1), lazyMap(map, (Factory<Integer>) () -> 1).get("a"));
    assertEquals(Integer.valueOf(2), lazyMap(new HashMap<String, Integer>(), (Transformer<String, Integer>) String::length).get("ab"));
    assertEquals(Integer.valueOf(1), lazySortedMap(new TreeMap<String, Integer>(), (Factory<Integer>) () -> 1).get("a"));
    assertEquals(Integer.valueOf(2), lazySortedMap(new TreeMap<String, Integer>(), (Transformer<String, Integer>) String::length).get("ab"));
    assertEquals(Integer.valueOf(1), iterableSortedMap(sortedMap).get("a"));
    assertThrows(IllegalArgumentException.class, () -> fixedSizeSortedMap(sortedMap).put("b", 2));

    MultiValueMap<String, Integer> multiValueMap = new MultiValueMap<>();
    MapUtil.<String, Integer, Integer>populateMap((org.apache.commons.collections4.MultiMap<String, Integer>) multiValueMap, java.util.Arrays.asList(1, 2), value -> "k" + value, value -> value + 10);
    assertTrue(multiValueMap.containsValue("k1", 11));
    MultiValueMap<String, Integer> multiValueMap1 = new MultiValueMap<>();
    MapUtil.<String, Integer>populateMap((org.apache.commons.collections4.MultiMap<String, Integer>) multiValueMap1, java.util.Arrays.asList(1, 2), value -> "k" + value);
    assertTrue(multiValueMap1.containsValue("k1", 1));

    Map<String, Integer> populated = new HashMap<>();
    MapUtil.<String, Integer>populateMap(populated, java.util.Arrays.asList(1, 2), value -> "k" + value);
    assertEquals(Integer.valueOf(1), populated.get("k1"));
    Map<String, Integer> populated1 = new HashMap<>();
    MapUtil.<String, Integer, Integer>populateMap(populated1, java.util.Arrays.asList(1, 2), value -> "k" + value, value -> value + 10);
    assertEquals(Integer.valueOf(11), populated1.get("k1"));
  }
}
