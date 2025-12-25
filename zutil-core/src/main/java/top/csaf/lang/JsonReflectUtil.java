package top.csaf.lang;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * JSON 反射工具类
 * <p>
 * 用于兼容处理不同 JSON 库（Gson, Jackson, Fastjson, Fastjson2）的对象，避免引入强依赖。
 */
@Slf4j
public class JsonReflectUtil {

  /**
   * 尝试解包 JSON 元素对象，提取其真实值
   * <p>
   * 例如：将 Gson 的 JsonPrimitive 转换为 String 或 BigDecimal
   *
   * @param obj 任意对象
   * @return 如果是已知的 JSON 包装类，返回提取后的值；否则返回原对象
   */
  public static Object getValue(Object obj) {
    if (obj == null) {
      return null;
    }
    String className = obj.getClass().getName();

    // 1. 处理 Gson: com.google.gson.JsonPrimitive
    if ("com.google.gson.JsonPrimitive".equals(className)) {
      return getGsonValue(obj);
    }

    // 2. 处理 Jackson: com.fasterxml.jackson.databind.node.ValueNode
    if (className.startsWith("com.fasterxml.jackson.databind.node.")) {
      return getJacksonValue(obj);
    }

    // 3. 处理 Fastjson (v1) & Fastjson2
    // Fastjson v1: com.alibaba.fastjson.JSONObject / JSONArray
    // Fastjson v2: com.alibaba.fastjson2.JSONObject / JSONArray
    // 通常这些对象实现了 Map 或 List 接口，但在某些比较场景下（如 isAllEquals），
    // 可能需要特殊处理其内部包装的值（虽然 Fastjson 通常直接返回原生对象）。
    // 这里主要针对可能存在的包装类型或特殊行为进行扩展预留。
    // 如果 Fastjson 的对象直接就是 Map/List，CollUtil 的其他逻辑会自动处理。

    // 如果确实需要提取 Fastjson 的值（例如某些特定的包装类），可以在这里添加逻辑。
    // 目前 Fastjson 的设计比较符合 Java 原生集合习惯，通常不需要像 Gson/Jackson 那样解包。
    // 但为了保险起见，如果未来发现有特殊类，可以放这里。

    return obj;
  }

  /**
   * 反射处理 Gson
   */
  private static Object getGsonValue(Object obj) {
    try {
      Method isNumberMethod = obj.getClass().getMethod("isNumber");
      boolean isNumber = (boolean) isNumberMethod.invoke(obj);
      if (isNumber) {
        Method getAsBigDecimalMethod = obj.getClass().getMethod("getAsBigDecimal");
        return getAsBigDecimalMethod.invoke(obj);
      } else {
        Method getAsStringMethod = obj.getClass().getMethod("getAsString");
        return getAsStringMethod.invoke(obj);
      }
    } catch (Exception e) {
      log.warn("Reflect handle Gson value failed", e);
      return obj;
    }
  }

  /**
   * 反射处理 Jackson
   */
  private static Object getJacksonValue(Object obj) {
    try {
      Method isValueNodeMethod = obj.getClass().getMethod("isValueNode");
      if ((boolean) isValueNodeMethod.invoke(obj)) {
        Method isNumberMethod = obj.getClass().getMethod("isNumber");
        if ((boolean) isNumberMethod.invoke(obj)) {
          Method numberValueMethod = obj.getClass().getMethod("numberValue");
          return numberValueMethod.invoke(obj);
        }
        Method isTextualMethod = obj.getClass().getMethod("isTextual");
        if ((boolean) isTextualMethod.invoke(obj)) {
          Method textValueMethod = obj.getClass().getMethod("textValue");
          return textValueMethod.invoke(obj);
        }
        Method asTextMethod = obj.getClass().getMethod("asText");
        return asTextMethod.invoke(obj);
      }
    } catch (Exception e) {
      log.warn("Reflect handle Jackson value failed", e);
    }
    return obj;
  }
}
