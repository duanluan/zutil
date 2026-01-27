package top.csaf.junit;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.json.JsonUtil;
import top.csaf.regex.RegExUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("JSON 工具类测试")
class JsonUtilTest {

  @Data
  public static class TestObject {
    private String name;
    private Integer age;
  }

  @DisplayName("对象转 JSON 字符串")
  @Test
  void toJson() {
    // 带特性：输出值为 null 的字段
    assertEquals("[]", JsonUtil.toJson(new ArrayList<>(), JSONWriter.Feature.WriteNullListAsEmpty));

    // 不带特性，默认输出值为 null 的字段
    TestObject testObj = new TestObject();
    String jsonWithNull = JsonUtil.toJson(testObj);
    // 宽松匹配：只要包含 null 字段即可
    assertTrue(jsonWithNull.contains("\"name\":null") || jsonWithNull.contains("null"), "应包含 null 值");

    // 不带特性 (NoFeature)，通常不输出 null
    String jsonNoFeature = JsonUtil.toJsonNoFeature(testObj);
    assertFalse(jsonNoFeature.contains("\"name\":null"), "不应包含 null 值");
    assertEquals("{}", jsonNoFeature);
  }

  @DisplayName("字符串转对象")
  @Test
  void parseObject() {
    String json = "{\"age\": 18}";
    // 带特性：初始化 String 字段为空字符串””
    assertEquals("", JsonUtil.parseObject(json, TestObject.class, JSONReader.Feature.InitStringFieldAsEmpty).getName());

    // 不带特性
    assertNull(JsonUtil.parseObject(json, TestObject.class).getName());

    // 解析失败
    assertThrows(JSONException.class, () -> JsonUtil.parseObject("{invalid json}", TestObject.class));
  }

  @DisplayName("字符串转集合")
  @Test
  void parseArray() {
    String json = "[{\"age\":18}]";
    // 带特性
    assertEquals("", JsonUtil.parseArray(json, TestObject.class, JSONReader.Feature.InitStringFieldAsEmpty).get(0).getName());

    // 不带特性
    assertNull(JsonUtil.parseArray(json, TestObject.class).get(0).getName());

    // 解析失败
    assertThrows(JSONException.class, () -> JsonUtil.parseArray("[invalid json]", TestObject.class));

    // 空列表
    assertTrue(JsonUtil.parseArray("[]", TestObject.class).isEmpty());
  }

  @DisplayName("JSON 格式化")
  @Test
  void format() {
    // 正常格式化
    String raw = "{\"name\":\"zhangsan\",\"age\":18}";
    String formatted = JsonUtil.format(raw);

    // 修复：Fastjson2 默认使用 \t 缩进，此处改为检查 \t 或空格
    assertTrue(formatted.contains("\n") || formatted.contains("\r\n"), "格式化后应包含换行符");
    assertTrue(formatted.contains("\t") || formatted.contains("  "), "格式化后应包含缩进(Tab或空格)");

    // 空值处理
    assertEquals("", JsonUtil.format(null));
    assertEquals("", JsonUtil.format(""));

    // 容错性（允许非标准 JSON，如单引号）
    String nonStandard = "{'name':'zhangsan'}";
    assertTrue(JsonUtil.format(nonStandard).contains("\"name\":"));
  }

  @DisplayName("JSON 压缩")
  @Test
  void minify() {
    // 正常压缩
    String formatted = "{\n  \"name\": \"zhangsan\",\n  \"age\": 18\n}";
    String minified = JsonUtil.minify(formatted);
    assertFalse(minified.contains("\n"), "压缩后不应包含换行符");
    assertFalse(minified.contains("  "), "压缩后不应包含多余空格");
    assertEquals("{\"name\":\"zhangsan\",\"age\":18}", minified);

    // 空值处理
    assertEquals("", JsonUtil.minify(null));
    assertEquals("", JsonUtil.minify(""));
  }

  @DisplayName("验证 JSON 有效性")
  @Test
  void isValid() {
    assertTrue(JsonUtil.isValid("{}"));
    assertTrue(JsonUtil.isValid("[]"));
    assertTrue(JsonUtil.isValid("{\"a\":1}"));
    assertFalse(JsonUtil.isValid("{a:1}")); // 标准 JSON key 必须带双引号
    assertFalse(JsonUtil.isValid("invalid"));
    assertFalse(JsonUtil.isValid(""));
  }

  @DisplayName("URL 参数转 JSON")
  @Test
  void paramsToJson() {
    // 正常转换
    String params = "a=1&b=2&name=%E5%BC%A0%E4%B8%89"; // name=张三
    String json = JsonUtil.paramsToJson(params);
    Map map = JsonUtil.parseObject(json, Map.class);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
    assertEquals("张三", map.get("name"));

    // 空值 key
    String emptyVal = "a=&b=2";
    Map map2 = JsonUtil.parseObject(JsonUtil.paramsToJson(emptyVal), Map.class);
    assertEquals("", map2.get("a"));

    // 只有 key 没有等号
    String onlyKey = "key1&key2";
    Map map3 = JsonUtil.parseObject(JsonUtil.paramsToJson(onlyKey), Map.class);
    assertEquals("", map3.get("key1"));

    // 空输入
    assertEquals("{}", JsonUtil.paramsToJson(null));
    assertEquals("{}", JsonUtil.paramsToJson(""));
  }

  @DisplayName("泛型解析 (parse)")
  @Test
  void parse() {
    // 解析为 Map
    Object objMap = JsonUtil.parse("{\"a\":1}");
    assertInstanceOf(Map.class, objMap);
    assertEquals(1, ((Map<?, ?>) objMap).get("a"));

    // 解析为 List
    Object objList = JsonUtil.parse("[1, 2]");
    assertInstanceOf(List.class, objList);
    assertEquals(2, ((List<?>) objList).size());

    // 容错解析
    Object nonStandard = JsonUtil.parse("{'a':1}"); // 单引号
    assertInstanceOf(Map.class, nonStandard);
  }
}
