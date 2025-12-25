package top.csaf.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.NonNull;
import top.csaf.coll.CollUtil;

import java.util.List;

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
}
