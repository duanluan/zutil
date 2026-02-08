package top.csaf.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.charset.StandardCharsets;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UrlUtil 工具类测试。
 */
@DisplayName("UrlUtil 工具类测试")
class UrlUtilTest {

  /**
   * 验证默认 UTF-8 编码/解码及空格转换规则。
   */
  @Test
  void testUrlEncodeDecodeDefault() {
    String input = "a b/?&=";
    String encoded = UrlUtil.urlEncode(input);
    assertTrue(encoded.contains("+"));
    assertEquals(input, UrlUtil.urlDecode(encoded));
    assertEquals("a b", UrlUtil.urlDecode("a+b"));
  }

  /**
   * 验证指定字符集的编码/解码行为。
   */
  @Test
  void testUrlEncodeDecodeCustomCharset() {
    String input = "a b";
    String encoded = UrlUtil.urlEncode(input, StandardCharsets.ISO_8859_1);
    assertEquals("a+b", encoded);
    assertEquals(input, UrlUtil.urlDecode(encoded, StandardCharsets.ISO_8859_1));
  }

  /**
   * 验证不支持的字符集会抛出运行时异常。
   */
  @Test
  void testUrlEncodeDecodeUnsupportedCharset() {
    Charset unsupported = new Charset("x-unsupported", new String[0]) {
      @Override
      public boolean contains(Charset cs) {
        return false;
      }

      @Override
      public CharsetDecoder newDecoder() {
        return java.nio.charset.StandardCharsets.UTF_8.newDecoder();
      }

      @Override
      public CharsetEncoder newEncoder() {
        return java.nio.charset.StandardCharsets.UTF_8.newEncoder();
      }
    };

    assertThrows(RuntimeException.class, () -> UrlUtil.urlEncode("a", unsupported));
    assertThrows(RuntimeException.class, () -> UrlUtil.urlDecode("a", unsupported));
  }

  /**
   * 验证空参数与默认前缀的处理逻辑。
   */
  @Test
  void testToUrlParamsEmptyAndDefaultPrefix() {
    assertEquals("?", UrlUtil.toUrlParams((CharSequence) null, null));
    assertEquals("&", UrlUtil.toUrlParams("&", new HashMap<>()));
    assertEquals("?", UrlUtil.toUrlParams(new HashMap<>()));
  }

  /**
   * 验证单参数与多参数拼接结果。
   */
  @Test
  void testToUrlParamsSingleAndMulti() {
    Map<String, Object> single = new LinkedHashMap<>();
    single.put("a", 1);
    assertEquals("?a=1", UrlUtil.toUrlParams((CharSequence) null, single));

    Map<String, Object> multiple = new LinkedHashMap<>();
    multiple.put("a", 1);
    multiple.put("b", 2);
    assertEquals("?a=1&b=2", UrlUtil.toUrlParams("?", multiple));
  }

  /**
   * 验证编码拼接、集合/数组展开与空值处理。
   */
  @Test
  void testToUrlParamsEncoded() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("name", "a b");
    params.put("id", Arrays.asList(1, 2));
    params.put("code", new int[]{3, 4});
    params.put("empty", null);
    String result = UrlUtil.toUrlParams("?", params, StandardCharsets.UTF_8);
    assertEquals("?name=a+b&id=1&id=2&code=3&code=4&empty=", result);
  }

  /**
   * 验证空 Iterable 与空数组会输出空值占位。
   */
  @Test
  void testToUrlParamsEmptyIterableAndArray() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("list", new ArrayList<>());
    params.put("arr", new int[0]);
    String result = UrlUtil.toUrlParams("?", params, StandardCharsets.UTF_8);
    assertEquals("?list=&arr=", result);
  }

  /**
   * 验证默认前缀 + 指定字符集的重载方法。
   */
  @Test
  void testToUrlParamsDefaultPrefixWithCharset() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("kw", "a b");
    assertEquals("?kw=a+b", UrlUtil.toUrlParams(params, StandardCharsets.UTF_8));
  }

  /**
   * 验证仅包含空 key 时返回前缀本身。
   */
  @Test
  void testToUrlParamsNullKeyOnly() {
    Map<String, Object> params = new HashMap<>();
    params.put(null, "v");
    assertEquals("?", UrlUtil.toUrlParams("?", params));
  }

  /**
   * 验证查询串解析：空值、无值以及包含 "=" 的场景。
   */
  @Test
  void testToMapParamsParsesValues() {
    String url = "http://localhost?name=zhangsan&empty=&flag&eq=a=b";
    Map<String, String> map = UrlUtil.toMapParams("?", url);
    assertEquals("zhangsan", map.get("name"));
    assertEquals("", map.get("empty"));
    assertEquals("", map.get("flag"));
    assertEquals("a=b", map.get("eq"));

    Map<String, String> mapDefault = UrlUtil.toMapParams(url);
    assertEquals("zhangsan", mapDefault.get("name"));

    Map<String, String> mapNullPrefix = UrlUtil.toMapParams(null, url);
    assertEquals("zhangsan", mapNullPrefix.get("name"));
  }

  /**
   * 验证默认前缀 + 指定字符集的解析重载。
   */
  @Test
  void testToMapParamsDefaultPrefixWithCharset() {
    String url = "http://localhost?name=zhang+san";
    Map<String, String> map = UrlUtil.toMapParams(url, StandardCharsets.UTF_8);
    assertEquals("zhang san", map.get("name"));
  }

  /**
   * 验证空前缀解析查询串。
   */
  @Test
  void testToMapParamsEmptyPrefix() {
    String query = "?a=1&b=2";
    Map<String, String> map = UrlUtil.toMapParams("", query);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

  /**
   * 验证空前缀 + 空查询串会抛出异常。
   */
  @Test
  void testToMapParamsEmptyPrefixBlankQuery() {
    assertThrows(IllegalArgumentException.class, () -> UrlUtil.toMapParams("", "?"));
    assertThrows(IllegalArgumentException.class, () -> UrlUtil.toMapParams("", "&"));
  }

  /**
   * 验证查询串仅包含空白时会抛出异常。
   */
  @Test
  void testToMapParamsBlankQueryAfterPrefix() {
    assertThrows(IllegalArgumentException.class, () -> UrlUtil.toMapParams("?", "http://localhost?   "));
  }

  /**
   * 验证解码逻辑与片段（#）截断。
   */
  @Test
  void testToMapParamsDecodeAndFragment() {
    String url = "http://localhost?name=zhang+san&city=shang%20hai#frag";
    Map<String, String> map = UrlUtil.toMapParams("?", url, StandardCharsets.UTF_8);
    assertEquals("zhang san", map.get("name"));
    assertEquals("shang hai", map.get("city"));
  }

  /**
   * 验证连续分隔符会忽略空参数。
   */
  @Test
  void testToMapParamsSkipEmptyParam() {
    String url = "http://localhost?a=1&&b=2";
    Map<String, String> map = UrlUtil.toMapParams("?", url);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

  /**
   * 验证多值参数的解析结果。
   */
  @Test
  void testToMultiMapParams() {
    String url = "http://localhost?tag=a&tag=b&empty=";
    Map<String, List<String>> map = UrlUtil.toMultiMapParams("?", url);
    assertEquals(Arrays.asList("a", "b"), map.get("tag"));
    assertEquals(Arrays.asList(""), map.get("empty"));
  }

  /**
   * 验证多值解析的字符集重载。
   */
  @Test
  void testToMultiMapParamsWithCharset() {
    String url = "http://localhost?tag=a%20b&tag=c+d";
    Map<String, List<String>> mapWithPrefix = UrlUtil.toMultiMapParams("?", url, StandardCharsets.UTF_8);
    assertEquals(Arrays.asList("a b", "c d"), mapWithPrefix.get("tag"));

    Map<String, List<String>> mapDefaultPrefix = UrlUtil.toMultiMapParams(url, StandardCharsets.UTF_8);
    assertEquals(Arrays.asList("a b", "c d"), mapDefaultPrefix.get("tag"));
  }

  /**
   * 验证多值解析的默认前缀重载（不解码）。
   */
  @Test
  void testToMultiMapParamsDefaultPrefix() {
    String url = "http://localhost?tag=a%20b&tag=c+d";
    Map<String, List<String>> map = UrlUtil.toMultiMapParams(url);
    assertEquals(Arrays.asList("a%20b", "c+d"), map.get("tag"));
  }

  /**
   * 验证参数追加到已有 URL 的各种组合。
   */
  @Test
  void testAppendParams() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("q", "a b");
    String baseUrl = "http://localhost/api";
    assertEquals("http://localhost/api?q=a+b", UrlUtil.appendParams(baseUrl, params));
    assertEquals("http://localhost/api?x=1&q=a+b", UrlUtil.appendParams(baseUrl + "?x=1", params));
    assertEquals("http://localhost/api?q=a+b#frag", UrlUtil.appendParams(baseUrl + "#frag", params));
  }

  /**
   * 验证指定字符集追加与分隔符边界处理。
   */
  @Test
  void testAppendParamsWithCharsetAndSeparators() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("q", "a b");
    String baseUrl = "http://localhost/api";
    assertEquals("http://localhost/api?q=a+b", UrlUtil.appendParams(baseUrl + "?", params, StandardCharsets.UTF_8));
    assertEquals("http://localhost/api?x=1&q=a+b", UrlUtil.appendParams(baseUrl + "?x=1", params, StandardCharsets.UTF_8));
    assertEquals("http://localhost/api?x=1&q=a+b", UrlUtil.appendParams(baseUrl + "?x=1&", params, StandardCharsets.UTF_8));

    Map<String, Object> nullKeyParams = new HashMap<>();
    nullKeyParams.put(null, "v");
    assertEquals("http://localhost/api#frag", UrlUtil.appendParams(baseUrl + "#frag", nullKeyParams, StandardCharsets.UTF_8));
  }

  /**
   * 验证参数为空时直接返回原 URL。
   */
  @Test
  void testAppendParamsEmpty() {
    String baseUrl = "http://localhost/api";
    assertEquals(baseUrl, UrlUtil.appendParams(baseUrl, new HashMap<>()));
    assertEquals(baseUrl, UrlUtil.appendParams(baseUrl, new HashMap<>(), StandardCharsets.UTF_8));
  }

  /**
   * 验证非法 URL 的异常抛出。
   */
  @Test
  void testToMapParamsInvalid() {
    assertThrows(IllegalArgumentException.class, () -> UrlUtil.toMapParams("?", ""));
    assertThrows(IllegalArgumentException.class, () -> UrlUtil.toMapParams("?", "http://localhost"));
    assertThrows(IllegalArgumentException.class, () -> UrlUtil.toMapParams("?", "http://localhost?"));
  }

  /**
   * 通过反射覆盖私有方法的异常分支。
   */
  @Test
  void testPrivateHelpersBranches() throws Exception {
    Method toQueryString = UrlUtil.class.getDeclaredMethod("toQueryString", Map.class, Charset.class, boolean.class);
    toQueryString.setAccessible(true);
    assertEquals("", toQueryString.invoke(null, new HashMap<>(), StandardCharsets.UTF_8, true));

    Method parseQuery = UrlUtil.class.getDeclaredMethod("parseQuery", String.class, Charset.class, boolean.class, java.util.function.BiConsumer.class);
    parseQuery.setAccessible(true);
    parseQuery.invoke(null, "   ", StandardCharsets.UTF_8, false, (java.util.function.BiConsumer<String, String>) (k, v) -> {
    });

    Method stripLeadingQuery = UrlUtil.class.getDeclaredMethod("stripLeadingQuery", String.class);
    stripLeadingQuery.setAccessible(true);
    assertEquals("a=1&b=2", stripLeadingQuery.invoke(null, "a=1&b=2"));
  }

  /**
   * 验证空参数时的异常行为。
   */
  @Test
  void testNullInputs() {
    assertThrows(NullPointerException.class, () -> UrlUtil.urlEncode((CharSequence) null));
    assertThrows(NullPointerException.class, () -> UrlUtil.urlDecode((CharSequence) null));
    assertThrows(NullPointerException.class, () -> UrlUtil.urlEncode("a", null));
    assertThrows(NullPointerException.class, () -> UrlUtil.urlDecode("a", null));
  }
}
