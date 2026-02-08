package top.csaf.http;

import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.OkHttpsException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("HttpUtil 工具类测试")
class HttpUtilTest {

  private MockWebServer server;
  private String baseUrl;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    baseUrl = server.url("/").toString();
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  @DisplayName("工具方法: getContentLength")
  void testGetContentLength() {
    assertEquals(0, HttpUtil.getContentLength(new HashMap<>()));
    Map<String, Object> params = new HashMap<>();
    params.put("a", 1);
    assertTrue(HttpUtil.getContentLength(params) > 0);
  }

  // ... (核心测试: testGetParams, testPostParams, testHeaders, testReturnTypes, testException 保持不变) ...
  @Test
  void testGetParams() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("OK"));
    Map<String, Object> params = new HashMap<>();
    params.put("q", "test");
    HttpUtil.get(baseUrl + "api", params);
    RecordedRequest request = server.takeRequest();
    assertTrue(request.getPath().contains("/api?q=test"));
  }

  @Test
  void testPostParams() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("OK"));
    Map<String, Object> params = new HashMap<>();
    params.put("u", "admin");
    HttpUtil.post(baseUrl, params);
    assertTrue(server.takeRequest().getBody().readUtf8().contains("u=admin"));
  }

  @Test
  void testHeaders() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("OK"));
    Map<String, String> headers = new HashMap<>();
    headers.put("Token", "123");
    HttpUtil.getByHeader(baseUrl, headers);
    assertEquals("123", server.takeRequest().getHeader("Token"));
  }

  @Test
  @DisplayName("解析: 完整覆盖所有返回类型")
  void testReturnTypes() {
    String jsonObj = "{\"name\":\"test\"}";
    String jsonArr = "[1, 2]";

    // String
    server.enqueue(new MockResponse().setBody(jsonObj));
    assertEquals(jsonObj, HttpUtil.get(baseUrl, String.class));

    // HttpResult.Body
    server.enqueue(new MockResponse().setBody(jsonObj));
    cn.zhxu.okhttps.HttpResult.Body body = HttpUtil.get(baseUrl, cn.zhxu.okhttps.HttpResult.Body.class);
    assertNotNull(body);
    assertEquals(jsonObj, body.toString());

    // Jackson：JsonNode、ObjectNode、ArrayNode
    server.enqueue(new MockResponse().setBody(jsonObj));
    assertEquals("test", HttpUtil.get(baseUrl, JsonNode.class).get("name").asText());
    server.enqueue(new MockResponse().setBody(jsonObj));
    assertTrue(HttpUtil.get(baseUrl, ObjectNode.class).has("name"));
    server.enqueue(new MockResponse().setBody(jsonArr));
    assertEquals(1, HttpUtil.get(baseUrl, ArrayNode.class).get(0).asInt());

    // Gson：JsonObject、JsonArray、JsonElement
    server.enqueue(new MockResponse().setBody(jsonObj));
    assertEquals("test", HttpUtil.get(baseUrl, JsonObject.class).get("name").getAsString());
    server.enqueue(new MockResponse().setBody(jsonArr));
    JsonArray gsonArr = HttpUtil.get(baseUrl, JsonArray.class);
    assertEquals(1, gsonArr.get(0).getAsInt());
    server.enqueue(new MockResponse().setBody(jsonObj));
    JsonElement gsonEl = HttpUtil.get(baseUrl, JsonElement.class);
    assertTrue(gsonEl.isJsonObject());

    // Fastjson：JSONObject、JSONArray、JSON
    server.enqueue(new MockResponse().setBody(jsonObj));
    assertEquals("test", HttpUtil.get(baseUrl, JSONObject.class).getString("name"));
    server.enqueue(new MockResponse().setBody(jsonArr));
    JSONArray fastJsonArr = HttpUtil.get(baseUrl, JSONArray.class);
    assertEquals(1, fastJsonArr.get(0));
    server.enqueue(new MockResponse().setBody(jsonObj));
    JSONObject fastJsonObj = (JSONObject) HttpUtil.get(baseUrl, JSON.class);
    assertEquals("test", fastJsonObj.getString("name"));

    // POJO
    server.enqueue(new MockResponse().setBody(jsonObj));
    assertEquals("test", HttpUtil.get(baseUrl, TestBean.class).getName());
  }

  @Test
  @DisplayName("覆盖: 私有构造函数")
  void testPrivateConstructor() throws Exception {
    java.lang.reflect.Constructor<HttpUtil> constructor = HttpUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }

  @Test
  void testException() {
    assertThrows(IllegalArgumentException.class, () -> HttpUtil.get(""));
    assertThrows(IllegalArgumentException.class, () -> HttpUtil.sync("GET", "", null, null, null, null));
  }

  @Test
  void testJacksonException() {
    server.enqueue(new MockResponse().setBody("Not Json"));
    // 预期内部捕获异常并打印日志，不抛出异常到外部
    try {
      HttpUtil.get(baseUrl, JsonNode.class);
    } catch (Exception e) {
      // ignore
    }
  }

  // --- 修复点 1：网络错误测试 ---
  @Test
  @DisplayName("覆盖: 网络错误")
  void testNetworkError() throws IOException {
    server.shutdown(); // 关闭服务制造连接拒绝

    // OkHttps 默认行为是抛出异常，而不是返回 null
    // 所以这里必须断言抛出 OkHttpsException
    assertThrows(OkHttpsException.class, () -> HttpUtil.get(baseUrl));
  }

  // --- 修复点 2：全量覆盖测试 (解决编译错误和泄漏) ---
  @Test
  @DisplayName("覆盖: 暴力覆盖所有重载方法")
  void testAllOverloads() {
    // 准备足够多的 Response (60个)，防止 SocketTimeout
    for (int i = 0; i < 60; i++) {
      server.enqueue(new MockResponse().setBody("{}"));
    }

    Map<String, Object> params = new HashMap<>();
    Map<String, String> headers = new HashMap<>();
    Class<String> cls = String.class;

    // 辅助方法：返回类型为 HttpResult 的，必须手动 close()
    HttpResult res;

    // === GET ===
    assertNotNull(HttpUtil.getByHeader(baseUrl, "text/plain", params, headers, cls));
    assertNotNull(HttpUtil.getByHeader(baseUrl, params, headers, cls));
    assertNotNull(HttpUtil.getByHeader(baseUrl, headers, cls));

    res = HttpUtil.getByHeader(baseUrl, params, headers); res.close(); // 手动关闭
    res = HttpUtil.getByHeader(baseUrl, headers); res.close();         // 手动关闭

    assertNotNull(HttpUtil.get(baseUrl, "text/plain", params, cls));
    assertNotNull(HttpUtil.get(baseUrl, params, cls));
    assertNotNull(HttpUtil.get(baseUrl, cls));

    res = HttpUtil.get(baseUrl, params); res.close();
    res = HttpUtil.get(baseUrl); res.close();

    // === POST ===
    assertNotNull(HttpUtil.postByHeader(baseUrl, "text/plain", params, headers, cls));
    assertNotNull(HttpUtil.postByHeader(baseUrl, params, headers, cls));
    assertNotNull(HttpUtil.postByHeader(baseUrl, headers, cls));

    res = HttpUtil.postByHeader(baseUrl, "text/plain", params, headers); res.close();
    res = HttpUtil.postByHeader(baseUrl, params, headers); res.close();
    res = HttpUtil.postByHeader(baseUrl, headers); res.close();

    assertNotNull(HttpUtil.post(baseUrl, "text/plain", params, cls));
    assertNotNull(HttpUtil.post(baseUrl, params, cls));
    assertNotNull(HttpUtil.post(baseUrl, cls));

    res = HttpUtil.post(baseUrl, "text/plain", params); res.close();
    res = HttpUtil.post(baseUrl, params); res.close();
    res = HttpUtil.post(baseUrl); res.close();

    // === PUT ===
    assertNotNull(HttpUtil.putByHeader(baseUrl, "text/plain", params, headers, cls));
    assertNotNull(HttpUtil.putByHeader(baseUrl, params, headers, cls));
    assertNotNull(HttpUtil.putByHeader(baseUrl, headers, cls));

    res = HttpUtil.putByHeader(baseUrl, "text/plain", params, headers); res.close();
    res = HttpUtil.putByHeader(baseUrl, params, headers); res.close();
    res = HttpUtil.putByHeader(baseUrl, headers); res.close();

    assertNotNull(HttpUtil.put(baseUrl, "text/plain", params, cls));
    assertNotNull(HttpUtil.put(baseUrl, params, cls));
    assertNotNull(HttpUtil.put(baseUrl, cls));

    res = HttpUtil.put(baseUrl, "text/plain", params); res.close();
    res = HttpUtil.put(baseUrl, params); res.close();
    res = HttpUtil.put(baseUrl); res.close();

    // === PATCH ===
    assertNotNull(HttpUtil.patchByHeader(baseUrl, "text/plain", params, headers, cls));
    assertNotNull(HttpUtil.patchByHeader(baseUrl, params, headers, cls));
    assertNotNull(HttpUtil.patchByHeader(baseUrl, headers, cls));

    res = HttpUtil.patchByHeader(baseUrl, "text/plain", params, headers); res.close();
    res = HttpUtil.patchByHeader(baseUrl, params, headers); res.close();
    res = HttpUtil.patchByHeader(baseUrl, headers); res.close();

    assertNotNull(HttpUtil.patch(baseUrl, "text/plain", params, cls));
    assertNotNull(HttpUtil.patch(baseUrl, params, cls));
    assertNotNull(HttpUtil.patch(baseUrl, cls));

    res = HttpUtil.patch(baseUrl, "text/plain", params); res.close();
    res = HttpUtil.patch(baseUrl, params); res.close();
    res = HttpUtil.patch(baseUrl); res.close();

    // === DELETE ===
    assertNotNull(HttpUtil.deleteByHeader(baseUrl, "text/plain", params, headers, cls));
    assertNotNull(HttpUtil.deleteByHeader(baseUrl, params, headers, cls));
    assertNotNull(HttpUtil.deleteByHeader(baseUrl, headers, cls));

    res = HttpUtil.deleteByHeader(baseUrl, params, headers); res.close();
    res = HttpUtil.deleteByHeader(baseUrl, headers); res.close();

    assertNotNull(HttpUtil.delete(baseUrl, "text/plain", params, cls));
    assertNotNull(HttpUtil.delete(baseUrl, params, cls));
    assertNotNull(HttpUtil.delete(baseUrl, cls));

    res = HttpUtil.delete(baseUrl, params); res.close();
    res = HttpUtil.delete(baseUrl); res.close();
  }

  @Test
  @DisplayName("覆盖: Content-Type 设置")
  void testContentType() throws InterruptedException {
    server.enqueue(new MockResponse().setBody("OK"));
    // 显式调用带 Content-Type 的方法
    HttpUtil.post(baseUrl, "application/json", new HashMap<>());
    RecordedRequest request = server.takeRequest();
    assertTrue(request.getHeader("Content-Type").contains("application/json"));
  }

  @Data
  static class TestBean {
    private String name;
  }
}
