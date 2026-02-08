package top.csaf.http;

import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.SHttpTask;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.csaf.coll.MapUtil;
import top.csaf.constant.CommonPattern;
import top.csaf.http.constant.HeaderConst;
import top.csaf.http.constant.ReqMethodConst;
import top.csaf.http.convert.AutoJsonMsgConvertor;
import top.csaf.lang.StrUtil;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * HTTP 工具类
 */
@Slf4j
public class HttpUtil extends cn.zhxu.okhttps.HttpUtils {

  /**
   * 获取请求参数长度
   *
   * @param bodyParams 请求参数
   * @return 请求参数长度
   */
  public static int getContentLength(final Map<String, Object> bodyParams) {
    if (MapUtil.isEmpty(bodyParams)) {
      return 0;
    }
    String s = new AutoJsonMsgConvertor().serialize(bodyParams, false);
    s = CommonPattern.LEFT_CURLY_BRACES.matcher(s).replaceAll("%7B");
    s = CommonPattern.DOUBLE_QUOTATION_MARK.matcher(s).replaceAll("%22");
    s = CommonPattern.COLON.matcher(s).replaceAll("%3A");
    s = CommonPattern.LEFT_SQUARE_BRACKET.matcher(s).replaceAll("%5B");
    s = CommonPattern.RIGHT_SQUARE_BRACKET.matcher(s).replaceAll("%5D");
    s = CommonPattern.COMMA.matcher(s).replaceAll("%2C");
    s = CommonPattern.RIGHT_CURLY_BRACES.matcher(s).replaceAll("%7D");
    return s.length();
  }

  /**
   * 同步请求
   *
   * @param requestMethod 请求方法
   * @param url           请求地址
   * @param contentType   内容类型
   * @param params        参数
   * @param headers       消息头
   * @param resultClass   响应体需要转换的类型
   * @return 响应体
   */
  @SuppressWarnings("unchecked")
  protected static Object sync(@NonNull final String requestMethod, final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class resultClass) {
    if (StrUtil.isBlank(url)) {
      throw new IllegalArgumentException("Url: should not be blank");
    }
    // 使用 OkHttps 的 sync 方法构建 Task
    SHttpTask task = cn.zhxu.okhttps.HttpUtils.sync(url);

    // 添加内容类型
    if (StrUtil.isNotBlank(contentType)) {
      task.bodyType(contentType);
    }

    // 添加参数
    if (MapUtil.isNotEmpty(params)) {
      if (ReqMethodConst.GET.equals(requestMethod) || ReqMethodConst.DELETE.equals(requestMethod)) {
        task.addUrlPara(params);
      } else if (ReqMethodConst.POST.equals(requestMethod) || ReqMethodConst.PATCH.equals(requestMethod) || ReqMethodConst.PUT.equals(requestMethod)) {
        task.addBodyPara(params);
      }
    }

    // 添加 Header
    if (MapUtil.isNotEmpty(headers)) {
      task.addHeader(headers);
    }
    // 替换 OkHttp 默认的 Accept，并设置默认 UA
    if (MapUtil.isEmpty(headers) || headers.get(HeaderConst.USER_AGENT) == null) {
      task.addHeader(HeaderConst.USER_AGENT, HeaderConst.USER_AGENT_X);
    }

    // 执行请求
    HttpResult result = task.request(requestMethod);

    // 如果不需要转换类型，直接返回 HttpResult
    if (resultClass == null) {
      return result;
    }

    HttpResult.Body body = result.getBody();

    // 1. 如果需要 HttpResult.Body 类型
    if (HttpResult.Body.class.equals(resultClass)) {
      return body;
    }
    // 2. 如果需要 String 类型
    else if (String.class.equals(resultClass)) {
      return body.toString();
    }

    // 3. 核心修改：使用反射处理特定 JSON 库类型或通用 Bean
    // 获取响应字符串，交给 bodyToResult 进行反射解析
    return bodyToResult(body.toString(), resultClass);
  }

  /**
   * 将响应字符串转换为指定类型（反射实现，零硬依赖）
   *
   * @param jsonString  JSON 字符串
   * @param resultClass 目标类型
   * @return 转换后的对象
   */
  @SuppressWarnings("unchecked")
  private static <T> T bodyToResult(String jsonString, Class<T> resultClass) {
    if (jsonString == null) {
      return null;
    }
    String className = resultClass.getName();

    try {
      // === Jackson 处理 (ObjectNode, JsonNode, ArrayNode) ===
      if (className.startsWith("com.fasterxml.jackson.databind.")) {
        Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        Object mapper = mapperClass.getConstructor().newInstance();
        Method readTree = mapperClass.getMethod("readTree", String.class);
        return (T) readTree.invoke(mapper, jsonString);
      }

      // === Fastjson 处理 (v1 & v2) ===
      if (className.startsWith("com.alibaba.fastjson")) {
        // 自动探测 v2 或 v1
        Class<?> jsonClass;
        try {
          jsonClass = Class.forName("com.alibaba.fastjson2.JSON");
        } catch (ClassNotFoundException e) {
          jsonClass = Class.forName("com.alibaba.fastjson.JSON");
        }

        if (className.endsWith("JSONObject")) {
          Method parseObject = jsonClass.getMethod("parseObject", String.class);
          return (T) parseObject.invoke(null, jsonString);
        } else if (className.endsWith("JSONArray")) {
          Method parseArray = jsonClass.getMethod("parseArray", String.class);
          return (T) parseArray.invoke(null, jsonString);
        } else {
          // JSON.class 或其他，默认尝试 parseObject
          Method parseObject = jsonClass.getMethod("parseObject", String.class);
          return (T) parseObject.invoke(null, jsonString);
        }
      }

      // === Gson 处理 ===
      if (className.startsWith("com.google.gson.")) {
        Class<?> parserClass = Class.forName("com.google.gson.JsonParser");
        Object jsonElement;
        try {
          // Gson 2.8.6+ 使用静态方法 parseString
          Method parseString = parserClass.getMethod("parseString", String.class);
          jsonElement = parseString.invoke(null, jsonString);
        } catch (NoSuchMethodException e) {
          // 旧版本 Gson 使用 new JsonParser().parse()
          Object parser = parserClass.getConstructor().newInstance();
          Method parse = parserClass.getMethod("parse", String.class);
          jsonElement = parse.invoke(parser, jsonString);
        }

        if (className.endsWith("JsonObject")) {
          Method getAsJsonObject = jsonElement.getClass().getMethod("getAsJsonObject");
          return (T) getAsJsonObject.invoke(jsonElement);
        } else if (className.endsWith("JsonArray")) {
          Method getAsJsonArray = jsonElement.getClass().getMethod("getAsJsonArray");
          return (T) getAsJsonArray.invoke(jsonElement);
        }
        return (T) jsonElement;
      }

    } catch (Exception e) {
      log.error("Failed to parse JSON for class: {}", className, e);
      return null;
    }

    // === 通用 Bean 处理 ===
    // 如果不是上述特定的 JSON 库类型，则使用 AutoJsonMsgConvertor 进行通用转换
    // 它会自动选择当前环境中最优的 JSON 库
    return new AutoJsonMsgConvertor().toBean(resultClass, jsonString);
  }

  /**
   * 同步 GET
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T getByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.GET, url, contentType, params, headers, resultClass);
  }

  /**
   * 同步 GET
   *
   * @param url         请求地址
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T getByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.GET, url, null, params, headers, resultClass);
  }

  /**
   * 同步 GET
   *
   * @param url         请求地址
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T getByHeader(final String url, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.GET, url, null, null, headers, resultClass);
  }

  /**
   * 同步 GET
   *
   * @param url     请求地址
   * @param params  参数
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult getByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.GET, url, null, params, headers, null);
  }

  /**
   * 同步 GET
   *
   * @param url     请求地址
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult getByHeader(final String url, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.GET, url, null, null, headers, null);
  }

  /**
   * 同步 GET
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T get(final String url, final String contentType, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.GET, url, contentType, params, null, resultClass);
  }

  /**
   * 同步 GET
   *
   * @param url         请求地址
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T get(final String url, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.GET, url, null, params, null, resultClass);
  }

  /**
   * 同步 GET
   *
   * @param url         请求地址
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T get(final String url, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.GET, url, null, null, null, resultClass);
  }

  /**
   * 同步 GET
   *
   * @param url    请求地址
   * @param params 参数
   * @return 响应体
   */
  public static HttpResult get(final String url, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.GET, url, null, params, null, null);
  }

  /**
   * 同步 GET
   *
   * @param url 请求地址
   * @return 响应体
   */
  public static HttpResult get(final String url) {
    return (HttpResult) sync(ReqMethodConst.GET, url, null, null, null, null);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T postByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.POST, url, contentType, params, headers, resultClass);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T postByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.POST, url, null, params, headers, resultClass);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T postByHeader(final String url, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.POST, url, null, null, headers, resultClass);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @return 响应体
   */
  public static HttpResult postByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.POST, url, contentType, params, headers, null);
  }

  /**
   * 同步 POST
   *
   * @param url     请求地址
   * @param params  参数
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult postByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.POST, url, null, params, headers, null);
  }

  /**
   * 同步 POST
   *
   * @param url     请求地址
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult postByHeader(final String url, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.POST, url, null, null, headers, null);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T post(final String url, final String contentType, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.POST, url, contentType, params, null, resultClass);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T post(final String url, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.POST, url, null, params, null, resultClass);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T post(final String url, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.POST, url, null, null, null, resultClass);
  }

  /**
   * 同步 POST
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @return 响应体
   */
  public static HttpResult post(final String url, final String contentType, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.POST, url, contentType, params, null, null);
  }

  /**
   * 同步 POST
   *
   * @param url    请求地址
   * @param params 参数
   * @return 响应体
   */
  public static HttpResult post(final String url, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.POST, url, null, params, null, null);
  }

  /**
   * 同步 POST
   *
   * @param url 请求地址
   * @return 响应体
   */
  public static HttpResult post(final String url) {
    return (HttpResult) sync(ReqMethodConst.POST, url, null, null, null, null);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T putByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PUT, url, contentType, params, headers, resultClass);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T putByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PUT, url, null, params, headers, resultClass);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T putByHeader(final String url, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PUT, url, null, null, headers, resultClass);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @return 响应体
   */
  public static HttpResult putByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.PUT, url, contentType, params, headers, null);
  }

  /**
   * 同步 PUT
   *
   * @param url     请求地址
   * @param params  参数
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult putByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.PUT, url, null, params, headers, null);
  }

  /**
   * 同步 PUT
   *
   * @param url     请求地址
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult putByHeader(final String url, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.PUT, url, null, null, headers, null);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T put(final String url, final String contentType, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PUT, url, contentType, params, null, resultClass);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T put(final String url, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PUT, url, null, params, null, resultClass);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T put(final String url, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PUT, url, null, null, null, resultClass);
  }

  /**
   * 同步 PUT
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @return 响应体
   */
  public static HttpResult put(final String url, final String contentType, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.PUT, url, contentType, params, null, null);
  }

  /**
   * 同步 PUT
   *
   * @param url    请求地址
   * @param params 参数
   * @return 响应体
   */
  public static HttpResult put(final String url, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.PUT, url, null, params, null, null);
  }

  /**
   * 同步 PUT
   *
   * @param url 请求地址
   * @return 响应体
   */
  public static HttpResult put(final String url) {
    return (HttpResult) sync(ReqMethodConst.PUT, url, null, null, null, null);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T patchByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PATCH, url, contentType, params, headers, resultClass);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T patchByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PATCH, url, null, params, headers, resultClass);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T patchByHeader(final String url, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PATCH, url, null, null, headers, resultClass);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @return 响应体
   */
  public static HttpResult patchByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.PATCH, url, contentType, params, headers, null);
  }


  /**
   * 同步 PATCH
   *
   * @param url     请求地址
   * @param params  参数
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult patchByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.PATCH, url, null, params, headers, null);
  }

  /**
   * 同步 PATCH
   *
   * @param url     请求地址
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult patchByHeader(final String url, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.PATCH, url, null, null, headers, null);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T patch(final String url, final String contentType, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PATCH, url, contentType, params, null, resultClass);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T patch(final String url, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PATCH, url, null, params, null, resultClass);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T patch(final String url, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.PATCH, url, null, null, null, resultClass);
  }

  /**
   * 同步 PATCH
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @return 响应体
   */
  public static HttpResult patch(final String url, final String contentType, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.PATCH, url, contentType, params, null, null);
  }

  /**
   * 同步 PATCH
   *
   * @param url    请求地址
   * @param params 参数
   * @return 响应体
   */
  public static HttpResult patch(final String url, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.PATCH, url, null, params, null, null);
  }

  /**
   * 同步 PATCH
   *
   * @param url 请求地址
   * @return 响应体
   */
  public static HttpResult patch(final String url) {
    return (HttpResult) sync(ReqMethodConst.PATCH, url, null, null, null, null);
  }

  /**
   * 同步 DELETE
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T deleteByHeader(final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.DELETE, url, contentType, params, headers, resultClass);
  }

  /**
   * 同步 DELETE
   *
   * @param url         请求地址
   * @param params      参数
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T deleteByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.DELETE, url, null, params, headers, resultClass);
  }

  /**
   * 同步 DELETE
   *
   * @param url         请求地址
   * @param headers     消息头
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T deleteByHeader(final String url, final Map<String, String> headers, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.DELETE, url, null, null, headers, resultClass);
  }


  /**
   * 同步 DELETE
   *
   * @param url     请求地址
   * @param params  参数
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult deleteByHeader(final String url, final Map<String, Object> params, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.DELETE, url, null, params, headers, null);
  }

  /**
   * 同步 DELETE
   *
   * @param url     请求地址
   * @param headers 消息头
   * @return 响应体
   */
  public static HttpResult deleteByHeader(final String url, final Map<String, String> headers) {
    return (HttpResult) sync(ReqMethodConst.DELETE, url, null, null, headers, null);
  }

  /**
   * 同步 DELETE
   *
   * @param url         请求地址
   * @param contentType 内容类型
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T delete(final String url, final String contentType, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.DELETE, url, contentType, params, null, resultClass);
  }

  /**
   * 同步 DELETE
   *
   * @param url         请求地址
   * @param params      参数
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T delete(final String url, final Map<String, Object> params, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.DELETE, url, null, params, null, resultClass);
  }

  /**
   * 同步 DELETE
   *
   * @param url         请求地址
   * @param resultClass 响应体需要转换的类型
   * @param <T>         返回类型
   * @return 响应体
   */
  public static <T> T delete(final String url, final Class<T> resultClass) {
    return (T) sync(ReqMethodConst.DELETE, url, null, null, null, resultClass);
  }

  /**
   * 同步 DELETE
   *
   * @param url    请求地址
   * @param params 参数
   * @return 响应体
   */
  public static HttpResult delete(final String url, final Map<String, Object> params) {
    return (HttpResult) sync(ReqMethodConst.DELETE, url, null, params, null, null);
  }

  /**
   * 同步 DELETE
   *
   * @param url 请求地址
   * @return 响应体
   */
  public static HttpResult delete(final String url) {
    return (HttpResult) sync(ReqMethodConst.DELETE, url, null, null, null, null);
  }
}
