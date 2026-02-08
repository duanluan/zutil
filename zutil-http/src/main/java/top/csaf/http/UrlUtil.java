package top.csaf.http;

import lombok.NonNull;
import top.csaf.charset.StandardCharsets;
import top.csaf.coll.MapUtil;
import top.csaf.lang.StrUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * URL 工具类，提供编码/解码、查询参数拼装与解析等功能。
 */
public class UrlUtil {

  // 默认查询前缀
  private static final String DEFAULT_PREFIX = "?";
  // 参数分隔符
  private static final String PARAM_SEPARATOR = "&";
  // 键值分隔符
  private static final String KEY_VALUE_SEPARATOR = "=";
  // URL 片段分隔符
  private static final char FRAGMENT_SEPARATOR = '#';

  /**
   * 使用 UTF-8 对内容进行 URL 编码。
   *
   * @param value 待编码内容
   * @return 编码后的字符串
   */
  public static String urlEncode(@NonNull final CharSequence value) {
    return urlEncode(value, StandardCharsets.UTF_8);
  }

  /**
   * 使用指定字符集对内容进行 URL 编码。
   *
   * @param value   待编码内容
   * @param charset 字符集
   * @return 编码后的字符串
   */
  public static String urlEncode(@NonNull final CharSequence value, @NonNull final Charset charset) {
    try {
      return URLEncoder.encode(value.toString(), charset.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 使用 UTF-8 对内容进行 URL 解码。
   *
   * @param value 待解码内容
   * @return 解码后的字符串
   */
  public static String urlDecode(@NonNull final CharSequence value) {
    return urlDecode(value, StandardCharsets.UTF_8);
  }

  /**
   * 使用指定字符集对内容进行 URL 解码。
   *
   * @param value   待解码内容
   * @param charset 字符集
   * @return 解码后的字符串
   */
  public static String urlDecode(@NonNull final CharSequence value, @NonNull final Charset charset) {
    try {
      return URLDecoder.decode(value.toString(), charset.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 将参数拼接为 URL 查询字符串（不做 URL 编码）。
   * <p>prefix 为空时使用默认前缀 "?".</p>
   *
   * @param prefix 前缀，通常为 "?"
   * @param params 参数 Map
   * @return 查询字符串
   */
  public static String toUrlParams(CharSequence prefix, final Map<String, Object> params) {
    return toUrlParams(prefix, params, null, false);
  }

  /**
   * 使用指定字符集对 key/value 编码，并拼接为查询字符串。
   * <p>prefix 为空时使用默认前缀 "?".</p>
   *
   * @param prefix  前缀，通常为 "?"
   * @param params  参数 Map
   * @param charset 字符集
   * @return 查询字符串
   */
  public static String toUrlParams(CharSequence prefix, final Map<String, Object> params, @NonNull final Charset charset) {
    return toUrlParams(prefix, params, charset, true);
  }

  /**
   * 使用默认前缀 "?" 拼接查询字符串（不做 URL 编码）。
   *
   * @param params 参数 Map
   * @return 查询字符串
   */
  public static String toUrlParams(final Map<String, Object> params) {
    return toUrlParams(DEFAULT_PREFIX, params);
  }

  /**
   * 使用默认前缀 "?" 且按指定字符集编码后拼接查询字符串。
   *
   * @param params  参数 Map
   * @param charset 字符集
   * @return 查询字符串
   */
  public static String toUrlParams(final Map<String, Object> params, @NonNull final Charset charset) {
    return toUrlParams(DEFAULT_PREFIX, params, charset);
  }

  /**
   * 将参数追加到现有 URL，使用 UTF-8 进行编码。
   * <p>会自动处理已有查询串与片段（#...）。</p>
   *
   * @param url    基础 URL
   * @param params 参数 Map
   * @return 追加后的 URL
   */
  public static String appendParams(@NonNull final CharSequence url, final Map<String, Object> params) {
    return appendParams(url, params, StandardCharsets.UTF_8);
  }

  /**
   * 将参数追加到现有 URL，使用指定字符集进行编码。
   * <p>会自动处理已有查询串与片段（#...）。</p>
   *
   * @param url     基础 URL
   * @param params  参数 Map
   * @param charset 字符集
   * @return 追加后的 URL
   */
  public static String appendParams(@NonNull final CharSequence url, final Map<String, Object> params, @NonNull final Charset charset) {
    if (MapUtil.isEmpty(params)) {
      return url.toString();
    }
    String urlStr = url.toString();
    String fragment = "";
    int fragmentIndex = urlStr.indexOf(FRAGMENT_SEPARATOR);
    if (fragmentIndex >= 0) {
      fragment = urlStr.substring(fragmentIndex);
      urlStr = urlStr.substring(0, fragmentIndex);
    }

    String query = toQueryString(params, charset, true);
    if (StrUtil.isBlank(query)) {
      return urlStr + fragment;
    }
    String separator;
    if (urlStr.contains(DEFAULT_PREFIX)) {
      if (urlStr.endsWith(DEFAULT_PREFIX) || urlStr.endsWith(PARAM_SEPARATOR)) {
        separator = "";
      } else {
        separator = PARAM_SEPARATOR;
      }
    } else {
      separator = DEFAULT_PREFIX;
    }
    return urlStr + separator + query + fragment;
  }

  /**
   * 将 URL 查询串解析为 Map（不做 URL 解码）。
   * <p>仅按第一个 "=" 分隔，重复 key 以最后一次为准。</p>
   *
   * @param prefix 前缀，通常为 "?"
   * @param url    URL
   * @return 参数 Map
   */
  public static Map<String, String> toMapParams(CharSequence prefix, CharSequence url) {
    return toMapParams(prefix, url, null, false);
  }

  /**
   * 使用指定字符集对 key/value 解码后解析为 Map。
   * <p>仅按第一个 "=" 分隔，重复 key 以最后一次为准。</p>
   *
   * @param prefix  前缀，通常为 "?"
   * @param url     URL
   * @param charset 字符集
   * @return 参数 Map
   */
  public static Map<String, String> toMapParams(CharSequence prefix, CharSequence url, @NonNull final Charset charset) {
    return toMapParams(prefix, url, charset, true);
  }

  /**
   * 使用默认前缀 "?" 解析 URL 查询串为 Map（不做 URL 解码）。
   * <p>仅按第一个 "=" 分隔，重复 key 以最后一次为准。</p>
   *
   * @param url URL
   * @return 参数 Map
   */
  public static Map<String, String> toMapParams(final CharSequence url) {
    return toMapParams(DEFAULT_PREFIX, url);
  }

  /**
   * 使用默认前缀 "?" 且按指定字符集解码后解析为 Map。
   * <p>仅按第一个 "=" 分隔，重复 key 以最后一次为准。</p>
   *
   * @param url     URL
   * @param charset 字符集
   * @return 参数 Map
   */
  public static Map<String, String> toMapParams(final CharSequence url, @NonNull final Charset charset) {
    return toMapParams(DEFAULT_PREFIX, url, charset);
  }

  /**
   * 将 URL 查询串解析为多值 Map（不做 URL 解码）。
   * <p>同名 key 的值会全部保留。</p>
   *
   * @param prefix 前缀，通常为 "?"
   * @param url    URL
   * @return 参数多值 Map
   */
  public static Map<String, List<String>> toMultiMapParams(CharSequence prefix, CharSequence url) {
    return toMultiMapParams(prefix, url, null, false);
  }

  /**
   * 使用指定字符集对 key/value 解码后解析为多值 Map。
   * <p>同名 key 的值会全部保留。</p>
   *
   * @param prefix  前缀，通常为 "?"
   * @param url     URL
   * @param charset 字符集
   * @return 参数多值 Map
   */
  public static Map<String, List<String>> toMultiMapParams(CharSequence prefix, CharSequence url, @NonNull final Charset charset) {
    return toMultiMapParams(prefix, url, charset, true);
  }

  /**
   * 使用默认前缀 "?" 解析 URL 查询串为多值 Map（不做 URL 解码）。
   * <p>同名 key 的值会全部保留。</p>
   *
   * @param url URL
   * @return 参数多值 Map
   */
  public static Map<String, List<String>> toMultiMapParams(final CharSequence url) {
    return toMultiMapParams(DEFAULT_PREFIX, url);
  }

  /**
   * 使用默认前缀 "?" 且按指定字符集解码后解析为多值 Map。
   * <p>同名 key 的值会全部保留。</p>
   *
   * @param url     URL
   * @param charset 字符集
   * @return 参数多值 Map
   */
  public static Map<String, List<String>> toMultiMapParams(final CharSequence url, @NonNull final Charset charset) {
    return toMultiMapParams(DEFAULT_PREFIX, url, charset);
  }

  /**
   * 内部统一的参数拼接入口。
   */
  private static String toUrlParams(CharSequence prefix, final Map<String, Object> params, final Charset charset, final boolean encode) {
    if (prefix == null) {
      prefix = DEFAULT_PREFIX;
    }
    if (MapUtil.isEmpty(params)) {
      return prefix.toString();
    }
    String query = toQueryString(params, charset, encode);
    if (StrUtil.isBlank(query)) {
      return prefix.toString();
    }
    return prefix + query;
  }

  /**
   * 将参数转换为查询串（不包含前缀）。
   */
  private static String toQueryString(final Map<String, Object> params, final Charset charset, final boolean encode) {
    if (MapUtil.isEmpty(params)) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      String key = entry.getKey();
      if (key == null) {
        continue;
      }
      appendParam(result, key, entry.getValue(), charset, encode);
    }
    return result.toString();
  }

  /**
   * 处理单个 key 的值，支持集合/数组展开。
   */
  private static void appendParam(StringBuilder result, String key, Object value, Charset charset, boolean encode) {
    if (value == null) {
      appendPair(result, key, "", charset, encode);
      return;
    }
    if (value instanceof Iterable) {
      boolean appended = false;
      for (Object item : (Iterable<?>) value) {
        appendParam(result, key, item, charset, encode);
        appended = true;
      }
      if (!appended) {
        appendPair(result, key, "", charset, encode);
      }
      return;
    }
    Class<?> valueClass = value.getClass();
    if (valueClass.isArray()) {
      int length = Array.getLength(value);
      if (length == 0) {
        appendPair(result, key, "", charset, encode);
        return;
      }
      for (int i = 0; i < length; i++) {
        appendParam(result, key, Array.get(value, i), charset, encode);
      }
      return;
    }
    appendPair(result, key, String.valueOf(value), charset, encode);
  }

  /**
   * 追加单个 key=value 到结果中。
   */
  private static void appendPair(StringBuilder result, String key, String value, Charset charset, boolean encode) {
    String useKey = encode ? urlEncode(key, charset) : key;
    String useValue = encode ? urlEncode(value, charset) : value;
    if (result.length() > 0) {
      result.append(PARAM_SEPARATOR);
    }
    result.append(useKey).append(KEY_VALUE_SEPARATOR).append(useValue);
  }

  /**
   * 解析查询串为 Map（可能解码）。
   */
  private static Map<String, String> toMapParams(CharSequence prefix, CharSequence url, Charset charset, boolean decode) {
    String query = extractQuery(prefix, url);
    Map<String, String> map = new HashMap<>();
    parseQuery(query, charset, decode, map::put);
    return map;
  }

  /**
   * 解析查询串为多值 Map（可能解码）。
   */
  private static Map<String, List<String>> toMultiMapParams(CharSequence prefix, CharSequence url, Charset charset, boolean decode) {
    String query = extractQuery(prefix, url);
    Map<String, List<String>> map = new HashMap<>();
    parseQuery(query, charset, decode, (key, value) -> map.computeIfAbsent(key, k -> new ArrayList<>()).add(value));
    return map;
  }

  /**
   * 从 URL 中提取查询串（会去掉片段 #...）。
   */
  private static String extractQuery(CharSequence prefix, CharSequence url) {
    if (StrUtil.isBlank(url)) {
      throw new IllegalArgumentException("Url: must not be blank");
    }
    String prefixStr = prefix == null ? DEFAULT_PREFIX : prefix.toString();
    String urlStr = url.toString();
    int fragmentIndex = urlStr.indexOf(FRAGMENT_SEPARATOR);
    if (fragmentIndex >= 0) {
      urlStr = urlStr.substring(0, fragmentIndex);
    }
    if (prefixStr.isEmpty()) {
      String query = stripLeadingQuery(urlStr);
      if (StrUtil.isBlank(query)) {
        throw new IllegalArgumentException("Url: must contain '" + prefixStr + "' or no parameters after the '" + prefixStr + "'");
      }
      return query;
    }

    int prefixIndex = urlStr.indexOf(prefixStr);
    if (prefixIndex < 0) {
      throw new IllegalArgumentException("Url: must contain '" + prefixStr + "' or no parameters after the '" + prefixStr + "'");
    }
    int queryStart = prefixIndex + prefixStr.length();
    if (queryStart >= urlStr.length()) {
      throw new IllegalArgumentException("Url: must contain '" + prefixStr + "' or no parameters after the '" + prefixStr + "'");
    }
    String query = urlStr.substring(queryStart);
    if (StrUtil.isBlank(query)) {
      throw new IllegalArgumentException("Url: must contain '" + prefixStr + "' or no parameters after the '" + prefixStr + "'");
    }
    return query;
  }

  /**
   * 去掉查询串开头的 "?" 或 "&"。
   */
  private static String stripLeadingQuery(String query) {
    if (query.startsWith(DEFAULT_PREFIX) || query.startsWith(PARAM_SEPARATOR)) {
      return query.substring(1);
    }
    return query;
  }

  /**
   * 解析查询串并通过回调消费键值对。
   */
  private static void parseQuery(String query, Charset charset, boolean decode, BiConsumer<String, String> consumer) {
    if (StrUtil.isBlank(query)) {
      return;
    }
    Charset useCharset = decode ? (charset == null ? StandardCharsets.UTF_8 : charset) : null;
    String[] params = query.split(PARAM_SEPARATOR);
    for (String param : params) {
      if (param.isEmpty()) {
        continue;
      }
      int index = param.indexOf(KEY_VALUE_SEPARATOR);
      String key = index >= 0 ? param.substring(0, index) : param;
      String value = index >= 0 ? param.substring(index + 1) : "";
      if (decode) {
        key = urlDecode(key, useCharset);
        value = urlDecode(value, useCharset);
      }
      consumer.accept(key, value);
    }
  }
}
