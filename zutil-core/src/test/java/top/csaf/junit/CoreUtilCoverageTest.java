package top.csaf.junit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonPrimitive;
import org.apache.commons.collections4.functors.DefaultEquator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.charset.StandardCharsets;
import top.csaf.coll.CollUtil;
import top.csaf.coll.MapUtil;
import top.csaf.lang.ArrayUtil;
import top.csaf.lang.ClassUtil;
import top.csaf.lang.JsonReflectUtil;
import top.csaf.lang.NumberUtil;
import top.csaf.lang.ObjUtil;
import top.csaf.lang.RandomStrUtil;
import top.csaf.lang.RandomUtil;
import top.csaf.lang.StrUtil;
import top.csaf.lang.SysUtil;
import top.csaf.util.ReflectionTestUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("核心工具类补充测试")
class CoreUtilCoverageTest {

  @DisplayName("NumberUtil：转换与类型判断")
  @Test
  void numberUtilConvertAndCheck() {
    assertNull(NumberUtil.createBigDecimal((Object) null));
    assertNull(NumberUtil.createBigInteger((Object) null));
    assertNull(NumberUtil.createDouble((Object) null));
    assertNull(NumberUtil.createFloat((Object) null));
    assertNull(NumberUtil.createInteger((Object) null));
    assertNull(NumberUtil.createLong((Object) null));
    assertNull(NumberUtil.createNumber((Object) null));

    assertEquals(new BigDecimal("12.30"), NumberUtil.createBigDecimal((Object) "12.30"));
    assertEquals(new BigInteger("123"), NumberUtil.createBigInteger((Object) 123));
    assertEquals(12.5D, NumberUtil.createDouble((Object) "12.5"));
    assertEquals(12.5F, NumberUtil.createFloat((Object) "12.5"));
    assertEquals(12, NumberUtil.createInteger((Object) "12"));
    assertEquals(12L, NumberUtil.createLong((Object) "12"));
    assertEquals(12, NumberUtil.createNumber((Object) "12").intValue());

    assertTrue(NumberUtil.isBigDecimal("12.3"));
    assertTrue(NumberUtil.isBigInteger("123"));
    assertTrue(NumberUtil.isDouble("12.3"));
    assertTrue(NumberUtil.isFloat("12.3"));
    assertTrue(NumberUtil.isInteger("123"));
    assertTrue(NumberUtil.isLong("123"));
    assertTrue(NumberUtil.isNumber("123.4"));
    assertFalse(NumberUtil.isBigDecimal("x"));
    assertFalse(NumberUtil.isBigInteger("1.2"));
    assertFalse(NumberUtil.isDouble("x"));
    assertFalse(NumberUtil.isFloat("x"));
    assertFalse(NumberUtil.isInteger("1.2"));
    assertFalse(NumberUtil.isLong("1.2"));
    assertFalse(NumberUtil.isNumber("x"));
    assertFalse(NumberUtil.isNumber((Object) null));
    assertFalse(NumberUtil.isBigDecimal((Object) null));
    assertFalse(NumberUtil.isBigInteger((Object) null));
    assertFalse(NumberUtil.isDouble((Object) null));
    assertFalse(NumberUtil.isFloat((Object) null));
    assertFalse(NumberUtil.isInteger((Object) null));
    assertFalse(NumberUtil.isLong((Object) null));

    assertFalse(NumberUtil.ltZero(null));
    assertFalse(NumberUtil.leZero(null));
    assertFalse(NumberUtil.gtZero(null));
    assertFalse(NumberUtil.geZero(null));
    assertFalse(NumberUtil.eqZero(null));
    assertTrue(NumberUtil.ltZero(-1));
    assertFalse(NumberUtil.ltZero(0));
    assertTrue(NumberUtil.leZero(0));
    assertFalse(NumberUtil.leZero(1));
    assertTrue(NumberUtil.gtZero(1));
    assertFalse(NumberUtil.gtZero(0));
    assertTrue(NumberUtil.geZero(0));
    assertFalse(NumberUtil.geZero(-1));
    assertTrue(NumberUtil.eqZero(0));
    assertFalse(NumberUtil.eqZero(1));
  }

  @DisplayName("StrUtil：空白、格式化与命名转换")
  @Test
  void strUtilBranches() {
    assertTrue(StrUtil.isEmpty((Object) null));
    assertTrue(StrUtil.isEmpty((Object) ""));
    assertTrue(StrUtil.isEmpty(""));
    assertFalse(StrUtil.isEmpty((Object) "x"));
    assertFalse(StrUtil.isEmpty(1));
    assertTrue(StrUtil.isNotEmpty((Object) "x"));
    assertFalse(StrUtil.isNotEmpty((Object) ""));
    assertFalse(StrUtil.isNotEmpty(""));
    assertTrue(StrUtil.isAnyEmpty((Object[]) null));
    assertTrue(StrUtil.isAnyEmpty(new Object[]{"a", ""}));
    assertTrue(StrUtil.isAnyEmpty("a", ""));
    assertFalse(StrUtil.isAnyEmpty(new Object[]{"a", 1}));
    assertFalse(StrUtil.isAnyEmpty("a", 1));
    assertFalse(StrUtil.isAnyEmpty(new Object[]{"a", "b"}));

    assertTrue(StrUtil.isBlank((Object) null));
    assertTrue(StrUtil.isBlank((Object) " "));
    assertTrue(StrUtil.isBlank(" "));
    assertFalse(StrUtil.isBlank((Object) "x"));
    assertFalse(StrUtil.isBlank(1));
    assertTrue(StrUtil.isNotBlank((Object) "x"));
    assertFalse(StrUtil.isNotBlank((Object) " "));
    assertFalse(StrUtil.isNotBlank(" "));
    assertTrue(StrUtil.isAnyBlank((Object[]) null));
    assertTrue(StrUtil.isAnyBlank(new Object[]{"a", " "}));
    assertTrue(StrUtil.isAnyBlank("a", " "));
    assertFalse(StrUtil.isAnyBlank(new Object[]{"a", 1}));
    assertFalse(StrUtil.isAnyBlank("a", 1));
    assertFalse(StrUtil.isAnyBlank(new Object[]{"a", "b"}));

    assertThrows(NullPointerException.class, () -> StrUtil.indexOf(null, "a"));
    assertThrows(NullPointerException.class, () -> StrUtil.indexOf("a", null));
    assertEquals(-1, StrUtil.indexOf("abcd", "bd"));
    assertEquals(-1, StrUtil.indexOf("abcd", "dx"));
    assertEquals(1, StrUtil.indexOf("abcd", "bc"));
    assertEquals(1, StrUtil.indexOf("abcd", "bcd"));
    assertEquals("UserName", StrUtil.removeStartIgnoreCase("preUserName", "pre"));
    assertEquals("UserName", StrUtil.removeEndIgnoreCase("UserNameDTO", "dto"));
    assertEquals("UserName", StrUtil.removeStartIgnoreCase("UserName", "pre"));
    assertEquals("UserName", StrUtil.removeEndIgnoreCase("UserName", "dto"));
    assertThrows(NullPointerException.class, () -> StrUtil.removeStartIgnoreCase(null, "a"));
    assertThrows(NullPointerException.class, () -> StrUtil.removeStartIgnoreCase("a", null));
    assertThrows(NullPointerException.class, () -> StrUtil.removeEndIgnoreCase(null, "a"));
    assertThrows(NullPointerException.class, () -> StrUtil.removeEndIgnoreCase("a", null));
    assertThrows(IllegalArgumentException.class, () -> StrUtil.removeStartIgnoreCase("", "a"));
    assertThrows(IllegalArgumentException.class, () -> StrUtil.removeStartIgnoreCase("a", ""));
    assertThrows(IllegalArgumentException.class, () -> StrUtil.removeEndIgnoreCase("", "a"));
    assertThrows(IllegalArgumentException.class, () -> StrUtil.removeEndIgnoreCase("a", ""));

    assertEquals("a-1-b-2", StrUtil.format("a-{}-b-{}", "1", "2"));
    assertThrows(NullPointerException.class, () -> StrUtil.format(null, "1"));
    assertThrows(NullPointerException.class, () -> StrUtil.format("{}", (String[]) null));
    assertThrows(IllegalArgumentException.class, () -> StrUtil.format("", "1"));
    assertThrows(IllegalArgumentException.class, () -> StrUtil.format("{}"));
    assertThrows(NullPointerException.class, () -> StrUtil.toUnderscore(null));
    assertEquals("user_name", StrUtil.toUnderscore("userName"));
    assertEquals("u_r_l_value", StrUtil.toUnderscore("URLValue"));
    assertEquals("user name", StrUtil.toUnderscore("User Name"));
    assertEquals("", StrUtil.toUnderscore(""));
    assertThrows(NullPointerException.class, () -> StrUtil.toInitialUpperCase(null));
    assertEquals("A", StrUtil.toInitialUpperCase("a"));
    assertEquals("User", StrUtil.toInitialUpperCase("user"));
    assertEquals("", StrUtil.toInitialUpperCase(""));
    assertThrows(NullPointerException.class, () -> StrUtil.nCopies(null, 1));
    assertEquals("ababab", StrUtil.nCopies("ab", 3));
    assertEquals("ab", StrUtil.nCopies("ab", 0));
    assertEquals("", StrUtil.nCopies("", 3));
  }

  @DisplayName("CollUtil/MapUtil/ObjUtil：构造器与边界分支")
  @Test
  void collectionObjectUtilityBranches() {
    assertNotNull(new CollUtil());
    assertNotNull(new MapUtil());
    assertNotNull(new ClassUtil());
    assertNotNull(new NumberUtil());
    assertNotNull(new JsonReflectUtil());
    assertNotNull(new StrUtil());
    assertNotNull(new ArrayUtil());
    assertNotNull(new ObjUtil());

    assertEquals(Collections.singletonList(2), CollUtil.removeAll(Arrays.asList(1, 2), Collections.singletonList(1), DefaultEquator.defaultEquator()));
    assertEquals(Collections.singletonList(1), CollUtil.retainAll(Arrays.asList(1, 2), Collections.singletonList(1), DefaultEquator.defaultEquator()));
    ArrayList<Integer> numbers = new ArrayList<>(Collections.singletonList(1));
    assertEquals(Collections.singletonList(1), new ArrayList<>(CollUtil.transformingCollection(numbers, value -> value)));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.predicatedCollection(new ArrayList<>(), o -> false).add("x"));
    assertEquals(Collections.singletonList(1), CollUtil.subtract(Arrays.asList(1, 2), Collections.singletonList(2), value -> true));
    assertTrue(CollUtil.isEqualCollection(Collections.singletonList(1), Collections.singletonList(1), DefaultEquator.defaultEquator()));
    assertEquals(1, CollUtil.extractSingleton(Collections.singletonList(1)));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.isAllEmpty((Object) "x"));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.isAnyEmpty((Object) "x"));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.isAllEmpty((Object) new int[]{1}));
    assertThrows(IllegalArgumentException.class, () -> CollUtil.isAnyEmpty((Object) new int[]{1}));
    assertTrue(CollUtil.isAllEmpty(new Object[]{null}));
    assertFalse(CollUtil.isAllEmpty(new Object[]{"x"}));
    assertTrue(CollUtil.isAnyEmpty(new Object[]{null}));
    assertFalse(CollUtil.isAnyEmpty(new Object[]{"x"}));
    assertFalse(CollUtil.isAllEquals(true, null, new Object[]{1}, new Object[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new int[]{1}, new int[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new long[]{1}, new long[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new double[]{1}, new double[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new float[]{1}, new float[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new char[]{'1'}, new char[]{'2'}));
    assertFalse(CollUtil.isAllEquals(true, null, new byte[]{1}, new byte[]{2}));
    assertFalse(CollUtil.isAllEquals(true, null, new boolean[]{true}, new boolean[]{false}));
    assertFalse(CollUtil.isAllEquals(true, null, new short[]{1}, new short[]{2}));
    assertTrue(CollUtil.isAllEquals(false, null, new Vector<>(Collections.singletonList(1)).elements(), new Vector<>(Collections.singletonList(1)).elements()));
    assertFalse(CollUtil.isAllEquals(true, null, new Vector<>(Collections.singletonList(1)).elements(), new Vector<>(Collections.singletonList(2)).elements()));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new Object[]{1}, new Object[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new int[]{1}, new int[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new long[]{1}, new long[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new double[]{1}, new double[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new float[]{1}, new float[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new char[]{'1'}, new char[]{'2'}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new byte[]{1}, new byte[]{2}));
    assertFalse(CollUtil.isAllEqualsSameIndex(true, null, new short[]{1}, new short[]{2}));

    assertTrue(ObjUtil.isAllEquals(true, false, 1.0D, new BigDecimal("1.00"), BigInteger.ONE));
    assertFalse(ObjUtil.isAllEquals(true, true, null, 1));
    assertFalse(ObjUtil.isAllEquals(false, false, 1.0D, 1));
    assertThrows(IllegalArgumentException.class, () -> ObjUtil.isAllEquals(true, false, 1));
  }

  @DisplayName("JsonReflectUtil：异常分支保持原对象")
  @Test
  void jsonReflectUtilFallbackBranches() throws Throwable {
    Object badValue = new Object();
    assertSame(badValue, ReflectionTestUtil.invokeMethod(JsonReflectUtil.class, "getGsonValue", new Class[]{Object.class}, badValue));
    assertSame(badValue, ReflectionTestUtil.invokeMethod(JsonReflectUtil.class, "getJacksonValue", new Class[]{Object.class}, badValue));
  }

  @DisplayName("ArrayUtil：移动、去重和边界分支")
  @Test
  void arrayUtilAdditionalBranches() throws Exception {
    assertThrows(NullPointerException.class, () -> ArrayUtil.moveForward((int[]) null, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> ArrayUtil.moveForward(new int[]{}, 0, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtil.moveForward(new int[]{1}, -1, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtil.moveForward(new int[]{1}, 0, 0));
    assertEquals(-1, ArrayUtil.indexOf(new char[]{}, "a"));
    assertArrayEquals(new long[]{1, 4, 0, 0}, ArrayUtil.moveForward(new long[]{1, 2, 3, 4}, 1, 2));
    assertArrayEquals(new double[]{1, 4, 0, 0}, ArrayUtil.moveForward(new double[]{1, 2, 3, 4}, 1, 2));
    assertArrayEquals(new float[]{1, 4, 0, 0}, ArrayUtil.moveForward(new float[]{1, 2, 3, 4}, 1, 2));
    assertArrayEquals(new byte[]{1, 4, 0, 0}, ArrayUtil.moveForward(new byte[]{1, 2, 3, 4}, 1, 2));
    assertArrayEquals(new char[]{'1', '4', '\0', '\0'}, ArrayUtil.moveForward(new char[]{'1', '2', '3', '4'}, 1, 2));
    assertArrayEquals(new boolean[]{true, false, false, false}, ArrayUtil.moveForward(new boolean[]{true, false, true, false}, 1, 2));
    assertArrayEquals(new short[]{1, 4, 0, 0}, ArrayUtil.moveForward(new short[]{1, 2, 3, 4}, 1, 2));
    assertArrayEquals(new Integer[]{1, 4, null, null}, ArrayUtil.moveForward(new Integer[]{1, 2, 3, 4}, 1, 2));
    assertArrayEquals("a".getBytes(), ArrayUtil.toBytes(new char[]{'a'}));
    assertArrayEquals("a".getBytes(java.nio.charset.StandardCharsets.UTF_8), ArrayUtil.toBytes(new char[]{'a'}, java.nio.charset.StandardCharsets.UTF_8));
    assertArrayEquals("a".getBytes("UTF-8"), ArrayUtil.toBytes(new char[]{'a'}, "UTF-8"));
    assertThrows(RuntimeException.class, () -> ArrayUtil.toBytes(new char[]{'a'}, "BAD-CHARSET"));
    assertEquals("a", ArrayUtil.toString("a".getBytes()));
    assertEquals("a", ArrayUtil.toString("a".getBytes(java.nio.charset.StandardCharsets.UTF_8), java.nio.charset.StandardCharsets.UTF_8));
    assertEquals("a", ArrayUtil.toString("a".getBytes("UTF-8"), "UTF-8"));
    assertThrows(RuntimeException.class, () -> ArrayUtil.toString(new byte[]{1}, "BAD-CHARSET"));
    assertArrayEquals(new char[]{'a'}, ArrayUtil.toChars("a".getBytes()));
    assertArrayEquals(new char[]{'a'}, ArrayUtil.toChars("a".getBytes(java.nio.charset.StandardCharsets.UTF_8), java.nio.charset.StandardCharsets.UTF_8));
    assertArrayEquals(new char[]{'a'}, ArrayUtil.toChars("a".getBytes("UTF-8"), "UTF-8"));

    assertArrayEquals(new int[]{1, 3, 2}, ArrayUtil.removeAllElements(new int[]{1, 2, 3, 2}, 2, 4));
    assertArrayEquals(new Integer[]{1, 3, 2}, ArrayUtil.removeAllElements(new Integer[]{1, 2, 3, 2}, 2, 4));
    assertArrayEquals(new int[]{1, 2, 3}, ArrayUtil.deduplicate(new int[]{1, 2, 2, 3}));
    assertArrayEquals(new Integer[]{1, 2}, ArrayUtil.deduplicate(new Integer[]{1, 2, 1}));
    assertArrayEquals(new long[]{1, 2, 3}, ArrayUtil.deduplicate(new long[]{1, 2, 2, 3}));
    assertArrayEquals(new double[]{1, 2, 3}, ArrayUtil.deduplicate(new double[]{1, 2, 2, 3}));
    assertArrayEquals(new float[]{1, 2, 3}, ArrayUtil.deduplicate(new float[]{1, 2, 2, 3}));
    assertArrayEquals(new char[]{'1', '2', '3'}, ArrayUtil.deduplicate(new char[]{'1', '2', '2', '3'}));
    assertArrayEquals(new byte[]{1, 2, 3}, ArrayUtil.deduplicate(new byte[]{1, 2, 2, 3}));
    assertArrayEquals(new boolean[]{true, false}, ArrayUtil.deduplicate(new boolean[]{true, false, false}));
    assertArrayEquals(new short[]{1, 2, 3}, ArrayUtil.deduplicate(new short[]{1, 2, 2, 3}));
    assertArrayEquals(new String[]{"a", "b"}, ArrayUtil.deduplicate(new String[]{"a", "b", "a"}));

    assertArrayEquals(new String[]{"b", "a"}, ArrayUtil.deduplicatePreceding(new String[]{"a", "b", "a"}));
    assertArrayEquals(new Integer[]{2, 1}, ArrayUtil.deduplicatePreceding(new Integer[]{1, 2, 1}));
    assertArrayEquals(new String[]{"c", "b", "a"}, ArrayUtil.deduplicateReverse(new String[]{"b", "a", "b", "c"}));
    assertArrayEquals(new String[]{"a", "b"}, ArrayUtil.deduplicateHashSort(new String[]{"a", "b", "a"}));
    assertArrayEquals(new int[]{3, 2, 1}, ArrayUtil.deduplicateReverse(new int[]{1, 2, 2, 3}));
    assertArrayEquals(new long[]{3, 2, 1}, ArrayUtil.deduplicateReverse(new long[]{1, 2, 2, 3}));
    assertArrayEquals(new double[]{3, 2, 1}, ArrayUtil.deduplicateReverse(new double[]{1, 2, 2, 3}));
    assertArrayEquals(new float[]{3, 2, 1}, ArrayUtil.deduplicateReverse(new float[]{1, 2, 2, 3}));
    assertArrayEquals(new char[]{'3', '2', '1'}, ArrayUtil.deduplicateReverse(new char[]{'1', '2', '2', '3'}));
    assertArrayEquals(new byte[]{3, 2, 1}, ArrayUtil.deduplicateReverse(new byte[]{1, 2, 2, 3}));
    assertArrayEquals(new boolean[]{true, false}, ArrayUtil.deduplicateReverse(new boolean[]{true, false, false}));
    assertArrayEquals(new short[]{3, 2, 1}, ArrayUtil.deduplicateReverse(new short[]{1, 2, 2, 3}));
    Set<Object> hashResult = new HashSet<>(Arrays.asList(ArrayUtil.deduplicateHashSort(new String[]{"b", "a", "b"})));
    assertEquals(new HashSet<>(Arrays.asList("a", "b")), hashResult);
  }

  @DisplayName("JsonReflectUtil：Gson 与 Jackson 值解包")
  @Test
  void jsonReflectUtilValueUnwrap() throws Exception {
    assertNull(JsonReflectUtil.getValue(null));
    assertEquals(new BigDecimal("1"), JsonReflectUtil.getValue(new JsonPrimitive(1)));
    assertEquals("a", JsonReflectUtil.getValue(new JsonPrimitive("a")));

    ObjectMapper mapper = new ObjectMapper();
    JsonNode numberNode = mapper.readTree("1");
    JsonNode textNode = mapper.readTree("\"a\"");
    JsonNode boolNode = mapper.readTree("true");
    assertEquals(1, JsonReflectUtil.getValue(numberNode));
    assertEquals("a", JsonReflectUtil.getValue(textNode));
    assertEquals("true", JsonReflectUtil.getValue(boolNode));
    assertEquals("plain", JsonReflectUtil.getValue("plain"));
  }

  @DisplayName("ClassUtil 与常量工具")
  @Test
  void classAndConstantUtilBranches() throws Exception {
    assertFalse(ClassUtil.isPrimitiveType((Class<?>[]) null));
    assertFalse(ClassUtil.isPrimitiveType((Object[]) null));
    assertThrows(IllegalArgumentException.class, ClassUtil::isPrimitiveType);
    assertThrows(IllegalArgumentException.class, () -> ClassUtil.isPrimitiveType(new Object[]{}));
    assertTrue(ClassUtil.isPrimitiveType(Integer.class, Long.class, Double.class, Float.class, Character.class, Byte.class, Boolean.class, Short.class));
    assertFalse(ClassUtil.isPrimitiveType(Integer.class, String.class));
    assertTrue(ClassUtil.isPrimitiveType(1, 1L, 1D, 1F, '1', (byte) 1, true, (short) 1));
    assertFalse(ClassUtil.isPrimitiveType(1, "1"));

    assertEquals(java.nio.charset.StandardCharsets.UTF_8, StandardCharsets.UTF_8);
    assertEquals(Charset.forName("GB2312"), StandardCharsets.GB2312);
    Constructor<StandardCharsets> constructor = StandardCharsets.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
    assertTrue(exception.getCause() instanceof AssertionError);

    assertNotNull(SysUtil.getTempDir());
    assertNotNull(new SysUtil());
    assertNotNull(new RandomUtil());
    assertNotNull(new RandomStrUtil());
    assertNotEquals(RandomUtil.nextInt(1, 3), RandomUtil.nextInt(3, 5));
    assertEquals(4, RandomStrUtil.randomAlphanumeric(4).length());
  }

  @DisplayName("核心工具类剩余覆盖分支")
  @Test
  void coreRemainingCoverageBranches() throws Throwable {
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "moveForward", new Class[]{Object.class, int.class, int.class}, null, 0, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> ArrayUtil.moveForward(new int[]{1}, 1, 1));
    assertThrows(NullPointerException.class, () -> ReflectionTestUtil.invokeMethod(ArrayUtil.class, "deduplicate", new Class[]{Object.class}, new Object[]{null}));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicate((String[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicatePreceding((String[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateReverse((String[]) null));
    assertThrows(NullPointerException.class, () -> ArrayUtil.deduplicateHashSort((String[]) null));

    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new ArrayList<>(Collections.singletonList(1))));
    assertFalse(CollUtil.isAllEquals(false, null, Collections.singletonList(null), new Object[]{1}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new int[]{1}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new long[]{1}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new double[]{1}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new float[]{1}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new char[]{'1'}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new byte[]{1}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new boolean[]{true}));
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, new short[]{1}));
    Vector<Object> values = new Vector<>();
    values.add(1);
    assertFalse(CollUtil.isAllEquals(false, null, new Object[]{null}, values.elements()));
    assertTrue(CollUtil.isAllEqualsSameIndex(true, null, new boolean[]{true}, new boolean[]{true}));

    assertTrue(ObjUtil.isAllEquals(false, false, "a", "a", "a", "a"));
    assertTrue(ObjUtil.isAllEquals(true, false, "a", "a", "a", "a"));
    assertFalse(ObjUtil.isAllEquals(false, false, "a", "a", "b", "b"));
    assertFalse(ObjUtil.isAllEquals(true, false, 1.0D, 1.0D, 2.0D, 2.0D));

    ObjectMapper mapper = new ObjectMapper();
    JsonNode objectNode = mapper.readTree("{\"a\":1}");
    assertSame(objectNode, JsonReflectUtil.getValue(objectNode));

    class BrokenJacksonNode {
      public boolean isValueNode() {
        return true;
      }

      public boolean isNumber() {
        throw new IllegalStateException("boom");
      }
    }
    Object brokenJacksonNode = new BrokenJacksonNode();
    assertSame(brokenJacksonNode, ReflectionTestUtil.invokeMethod(JsonReflectUtil.class, "getJacksonValue", new Class[]{Object.class}, brokenJacksonNode));
  }
}
