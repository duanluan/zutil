package top.csaf.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.NonNull;
import top.csaf.charset.StandardCharsets;
import top.csaf.coll.CollUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 * <p>
 * {@link JSONWriter.Feature}、{@link JSONReader.Feature}：<a href="https://alibaba.github.io/fastjson2/features_cn.html">序列化和反序列化行为</a>
 */
public class JsonUtil {

  /**
   * 对象转 JSON 字符串，输出值为 null 的字段
   *
   * @param object 对象
   * @return JSON 字符串
   */
  public static String toJson(@NonNull final Object object) {
    return toJson(object, JSONWriter.Feature.WriteMapNullValue);
  }

  /**
   * 对象转 JSON 字符串
   *
   * @param object   对象
   * @param features 序列化行为，为空时默认输出值为 null 的字段
   * @return JSON 字符串
   */
  public static String toJson(@NonNull final Object object, final JSONWriter.Feature... features) {
    if (CollUtil.sizeIsEmpty(features)) {
      return toJson(object);
    }
    // 设置输出行为
    return JSON.toJSONString(object, features);
  }

  /**
   * 对象转 JSON 字符串，不含序列化特性
   *
   * @param object 对象
   * @return JSON 字符串
   */
  public static String toJsonNoFeature(@NonNull final Object object) {
    return JSON.toJSONString(object);
  }

  /**
   * JSON 字符串转对象
   *
   * @param json     JSON 字符串
   * @param clazz    对象类型
   * @param features 反序列化行为
   * @param <T>      对象类型
   * @return 对象
   */
  public static <T> T parseObject(@NonNull final String json, @NonNull final Class<T> clazz, final JSONReader.Feature... features) {
    if (CollUtil.sizeIsNotEmpty(features)) {
      return JSON.parseObject(json, clazz, features);
    }
    return JSON.parseObject(json, clazz);
  }

  /**
   * JSON 字符串转集合
   *
   * @param json     JSON 字符串
   * @param clazz    集合元素类型
   * @param features 反序列化行为
   * @param <T>      集合元素类型
   * @return 集合
   */
  public static <T> List<T> parseArray(@NonNull final String json, @NonNull final Class<T> clazz, final JSONReader.Feature... features) {
    if (CollUtil.sizeIsNotEmpty(features)) {
      return JSON.parseArray(json, clazz, features);
    }
    return JSON.parseArray(json, clazz);
  }

  /**
   * 格式化 JSON 字符串 (Pretty Print)
   *
   * @param json JSON 字符串
   * @return 格式化后的 JSON 字符串
   */
  public static String format(String json) {
    if (json == null || json.isEmpty()) {
      return "";
    }
    Object object = JSON.parse(json, JSONReader.Feature.AllowUnQuotedFieldNames);
    return JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.WriteMapNullValue);
  }

  /**
   * 压缩 JSON 字符串 (Minify)
   *
   * @param json JSON 字符串
   * @return 压缩后的 JSON 字符串
   */
  public static String minify(String json) {
    if (json == null || json.isEmpty()) {
      return "";
    }
    Object object = JSON.parse(json, JSONReader.Feature.AllowUnQuotedFieldNames);
    return JSON.toJSONString(object, JSONWriter.Feature.WriteMapNullValue);
  }

  /**
   * 验证 JSON 是否有效
   *
   * @param json JSON 字符串
   * @return 是否有效
   */
  public static boolean isValid(String json) {
    return JSON.isValid(json);
  }

  /**
   * URL 参数转 JSON
   * 例如: a=1&b=2 -> {"a":"1", "b":"2"}
   *
   * @param params URL 参数字符串
   * @return JSON 字符串
   */
  public static String paramsToJson(String params) {
    if (params == null || params.isEmpty()) {
      return "{}";
    }
    Map<String, String> map = new HashMap<>();
    String[] pairs = params.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      try {
        if (idx > 0) {
          String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
          String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
          map.put(key, value);
        } else if (idx < 0 && !pair.isEmpty()) {
          // 处理没有值的 key
          map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8.name()), "");
        }
      } catch (UnsupportedEncodingException e) {
        // 忽略异常，继续处理
      }
    }
    return toJson(map);
  }

  /**
   * 将任意对象转为 Fastjson2 的 JSONObject 或 JSONArray，用于树形结构遍历
   *
   * @param json JSON 字符串
   * @return JSONObject, JSONArray 或其他基本类型
   */
  public static Object parse(String json) {
    return JSON.parse(json, JSONReader.Feature.AllowUnQuotedFieldNames);
  }
}
