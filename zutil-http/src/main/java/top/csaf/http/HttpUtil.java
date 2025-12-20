package top.csaf.http;

import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.SHttpTask;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.csaf.coll.MapUtil;
import top.csaf.constant.CommonPattern;
import top.csaf.http.constant.HeaderConst;
import top.csaf.http.constant.ReqMethodConst;
import top.csaf.json.JsonUtil;
import top.csaf.lang.StrUtil;
import top.csaf.regex.RegExUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * HTTP 工具类
 */
@Slf4j
public class HttpUtil extends cn.zhxu.okhttps.HttpUtils {

  /**
   * 键值对参数转换为 URL 参数
   *
   * @param prefix 前缀，一般是“?”
   * @param params 参数
   * @return URL 参数
   */
  public static String toUrlParams(CharSequence prefix, final Map<String, Object> params) {
    if (prefix == null) {
      prefix = "?";
    }
    if (MapUtil.isEmpty(params)) {
      return prefix.toString();
    }
    Set<Map.Entry<String, Object>> entrySet = params.entrySet();
    StringBuilder result = new StringBuilder();
    Map.Entry<String, Object> firstEntry = null;
    for (Map.Entry<String, Object> entry : entrySet) {
      firstEntry = entry;
      result.append(entry.getKey()).append("=").append(entry.getValue());
      break;
    }
    entrySet.remove(firstEntry);
    for (Map.Entry<String, Object> entry : entrySet) {
      result.append("&").append(entry.getKey()).append("=").append(entry.getValue());
    }
    return prefix.toString() + result;
  }

  /**
   * 键值对参数转换为 URL 参数，含前缀“?”
   *
   * @param params 参数
   * @return URL 参数
   */
  public static String toUrlParams(final Map<String, Object> params) {
    return toUrlParams("?", params);
  }

  /**
   * URL 参数转换为 Map 参数
   *
   * @param prefix 前缀，一般是“?”
   * @param url    URL
   * @return Map 参数
   */
  public static Map<String, String> toMapParams(CharSequence prefix, CharSequence url) {
    if (prefix == null) {
      prefix = "?";
    }
    if (StrUtil.isBlank(url)) {
      throw new IllegalArgumentException("Url: must not be blank");
    }
    String[] split = url.toString().split(RegExUtil.replaceAllSpecialChar(prefix));
    if (split.length < 2) {
      throw new IllegalArgumentException("Url: must contain '" + prefix + "' or no parameters after the '" + prefix + "'");
    }
    String[] params = split[1].split("&");
    Map<String, String> map = new HashMap<>(params.length);
    for (String param : params) {
      String[] split1 = param.split("=");
      if (split1.length == 2) {
        map.put(split1[0], split1[1]);
      } else {
        map.put(split1[0], "");
      }
    }
    return map;
  }

  /**
   * URL 参数转换为 Map 参数，默认前缀“?”
   *
   * @param url URL
   * @return Map 参数
   */
  public static Map<String, String> toMapParams(final CharSequence url) {
    return toMapParams("?", url);
  }

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
    String s = JsonUtil.toJson(bodyParams);
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
  protected static Object sync(@NonNull final String requestMethod, final String url, final String contentType, final Map<String, Object> params, final Map<String, String> headers, final Class resultClass) {
    if (StrUtil.isBlank(url)) {
      throw new IllegalArgumentException("Url: should not be blank");
    }
    SHttpTask task = sync(url);
    // 添加内容类型
    if (StrUtil.isNotBlank(contentType)) {
      task.bodyType(contentType);
    }
    // 添加参数
    if (MapUtil.isNotEmpty(params)) {
      if (ReqMethodConst.GET.equals(requestMethod) || ReqMethodConst.DELETE.equals(requestMethod)) {
        task = task.addUrlPara(params);
      } else if (ReqMethodConst.POST.equals(requestMethod) || ReqMethodConst.PATCH.equals(requestMethod) || ReqMethodConst.PUT.equals(requestMethod)) {
        task.addBodyPara(params);
      }
    }
    // 添加 Header
    if (MapUtil.isNotEmpty(headers)) {
      task.addHeader(headers);
    }
    // 替换 OkHttp 默认的 Accept
    if (MapUtil.isEmpty(headers) || headers.get(HeaderConst.USER_AGENT) == null) {
      task.addHeader(HeaderConst.USER_AGENT, HeaderConst.USER_AGENT_X);
    }
    // 请求
    HttpResult result = task.request(requestMethod);
    if (resultClass == null) {
      return result;
    }
    HttpResult.Body body = result.getBody();

    // 根据不同返回类型返回结果
    if (HttpResult.Body.class.equals(resultClass)) {
      return body;
    }
    // String
    else if (String.class.equals(resultClass)) {
      return body.toString();
    }
    // jackson
    else if (ObjectNode.class.equals(resultClass) || JsonNode.class.equals(resultClass) || ArrayNode.class.equals(resultClass)) {
      try {
        return new ObjectMapper().readTree(body.toString());
      } catch (JsonProcessingException e) {
        log.error(e.getMessage(), e);
      }
    }
    // fastjson
    else if (JSON.class.equals(resultClass)) {
      return JSON.parseObject(body.toString());
    } else if (JSONObject.class.equals(resultClass)) {
      return JSON.parseObject(body.toString());
    } else if (JSONArray.class.equals(resultClass)) {
      return JSON.parseArray(body.toString());
    }
    // gson
    else if (JsonElement.class.equals(resultClass)) {
      return JsonParser.parseString(body.toString());
    } else if (JsonObject.class.equals(resultClass)) {
      JsonElement jsonElement = JsonParser.parseString(body.toString());
      return jsonElement.getAsJsonObject();
    } else if (JsonArray.class.equals(resultClass)) {
      JsonElement jsonElement = JsonParser.parseString(body.toString());
      return jsonElement.getAsJsonArray();
    }
    return body.toBean(resultClass);
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
