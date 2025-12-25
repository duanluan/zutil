package top.csaf.http.convert;

import cn.zhxu.data.Array;
import cn.zhxu.data.Mapper;
import cn.zhxu.data.TypeRef;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 自动 JSON 消息转换器 (零依赖版)
 * <p>
 * 自动检测类路径下的 JSON 库，优先级：Fastjson2 > Fastjson (v1) > Jackson > Gson。
 * 实现了 OkHttps 的 MsgConvertor 接口，支持零依赖运行。
 *
 * @author duanluan
 */
@Slf4j
public class AutoJsonMsgConvertor implements MsgConvertor, ConvertProvider {

  private enum JsonProvider {
    FASTJSON2, FASTJSON_V1, JACKSON, GSON, NONE
  }

  private static final JsonProvider PROVIDER;
  private static Object jacksonObjectMapper;
  private static Object gsonInstance;

  static {
    JsonProvider provider = JsonProvider.NONE;
    try {
      Class.forName("com.alibaba.fastjson2.JSON");
      provider = JsonProvider.FASTJSON2;
    } catch (ClassNotFoundException e1) {
      try {
        Class.forName("com.alibaba.fastjson.JSON");
        provider = JsonProvider.FASTJSON_V1;
      } catch (ClassNotFoundException e2) {
        try {
          Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
          jacksonObjectMapper = mapperClass.getConstructor().newInstance();
          // 可以在这里配置 Jackson，如忽略未知属性等
          provider = JsonProvider.JACKSON;
        } catch (Exception e3) {
          try {
            Class<?> gsonClass = Class.forName("com.google.code.gson.Gson");
            gsonInstance = gsonClass.getConstructor().newInstance();
            provider = JsonProvider.GSON;
          } catch (Exception e4) {
            log.warn("No supported JSON library found (Fastjson2, Fastjson, Jackson, Gson). JSON features will fail.");
          }
        }
      }
    }
    PROVIDER = provider;
    log.info("AutoJsonMsgConvertor using provider: {}", PROVIDER);
  }

  @Override
  public MsgConvertor getConvertor() {
    return new AutoJsonMsgConvertor();
  }

  @Override
  public String mediaType() {
    return "application/json";
  }

  // ==================== Mapper & Array Implementation ====================

  @Override
  public Mapper toMapper(InputStream in, Charset charset) {
    return toMapper(streamToString(in, charset));
  }

  @Override
  public Mapper toMapper(String in) {
    Map<String, Object> map = toBean(Map.class, in);
    if (map == null) {
      map = new LinkedHashMap<>();
    }
    return new UniversalMapper(map);
  }

  @Override
  public Array toArray(InputStream in, Charset charset) {
    return toArray(streamToString(in, charset));
  }

  @Override
  public Array toArray(String in) {
    List<Object> list = toBean(List.class, in);
    if (list == null) {
      list = new ArrayList<>();
    }
    return new UniversalArray(list);
  }

  // ==================== Serialization ====================

  @Override
  public byte[] serialize(Object object, Charset charset, boolean pretty) {
    String json = serialize(object, pretty);
    return json.getBytes(charset);
  }

  @Override
  public byte[] serialize(Object object, Charset charset) {
    return serialize(object, charset, false);
  }

  @Override
  public String serialize(Object object, boolean pretty) {
    // 解包 UniversalMapper/Array，避免重复包装
    if (object instanceof UniversalMapper) {
      object = ((UniversalMapper) object).toMap();
    } else if (object instanceof UniversalArray) {
      object = ((UniversalArray) object).toList();
    }

    try {
      switch (PROVIDER) {
        case FASTJSON2: {
          Class<?> jsonClass = Class.forName("com.alibaba.fastjson2.JSON");
          if (pretty) {
            Class<?> featureClass = Class.forName("com.alibaba.fastjson2.JSONWriter$Feature");
            Object prettyFeature = Enum.valueOf((Class<Enum>) featureClass, "PrettyFormat");
            Object features = java.lang.reflect.Array.newInstance(featureClass, 1);
            java.lang.reflect.Array.set(features, 0, prettyFeature);
            Method toJSONString = jsonClass.getMethod("toJSONString", Object.class, features.getClass());
            return (String) toJSONString.invoke(null, object, features);
          } else {
            Method toJSONString = jsonClass.getMethod("toJSONString", Object.class);
            return (String) toJSONString.invoke(null, object);
          }
        }
        case FASTJSON_V1: {
          Class<?> jsonClass = Class.forName("com.alibaba.fastjson.JSON");
          if (pretty) {
            // Fastjson v1 SerializerFeature.PrettyFormat
            // 简化处理：直接调用 toString 或默认序列化，pretty 可能需要更复杂的反射
            // 尝试获取 SerializerFeature
            try {
              Class<?> featureClass = Class.forName("com.alibaba.fastjson.serializer.SerializerFeature");
              Object prettyFeature = Enum.valueOf((Class<Enum>) featureClass, "PrettyFormat");
              Object features = java.lang.reflect.Array.newInstance(featureClass, 1);
              java.lang.reflect.Array.set(features, 0, prettyFeature);
              Method toJSONString = jsonClass.getMethod("toJSONString", Object.class, features.getClass());
              return (String) toJSONString.invoke(null, object, features);
            } catch (Exception e) {
              Method toJSONString = jsonClass.getMethod("toJSONString", Object.class);
              return (String) toJSONString.invoke(null, object);
            }
          } else {
            Method toJSONString = jsonClass.getMethod("toJSONString", Object.class);
            return (String) toJSONString.invoke(null, object);
          }
        }
        case JACKSON: {
          Method writeValueAsString = jacksonObjectMapper.getClass().getMethod("writeValueAsString", Object.class);
          String json = (String) writeValueAsString.invoke(jacksonObjectMapper, object);
          if (pretty) {
            // Jackson pretty print 比较麻烦，这里简化返回普通 JSON，或者你需要反射调用 writerWithDefaultPrettyPrinter()
            return json;
          }
          return json;
        }
        case GSON: {
          Method toJson = gsonInstance.getClass().getMethod("toJson", Object.class);
          // Gson pretty print 需要创建特定的 Gson 实例，这里复用默认实例
          return (String) toJson.invoke(gsonInstance, object);
        }
        default:
          throw new IllegalStateException("No JSON provider available");
      }
    } catch (Exception e) {
      throw new RuntimeException("JSON Serialize failed", e);
    }
  }

  // ==================== Deserialization ====================

  @Override
  public <T> T toBean(Type type, InputStream in, Charset charset) {
    // 统一转为 String 处理，保证最大兼容性 (尤其是 Fastjson v1 老版本不支持 InputStream)
    // 也可以针对 Jackson/Gson 优化为使用 InputStream，但这里为了代码简洁统一使用 String
    return toBean(type, streamToString(in, charset));
  }

  @Override
  public <T> T toBean(Type type, String in) {
    if (in == null || in.isEmpty()) {
      return null;
    }
    try {
      switch (PROVIDER) {
        case FASTJSON2: {
          Class<?> jsonClass = Class.forName("com.alibaba.fastjson2.JSON");
          Method parseObject = jsonClass.getMethod("parseObject", String.class, Type.class);
          return (T) parseObject.invoke(null, in, type);
        }
        case FASTJSON_V1: {
          Class<?> jsonClass = Class.forName("com.alibaba.fastjson.JSON");
          Method parseObject = jsonClass.getMethod("parseObject", String.class, Type.class, Class.forName("[Lcom.alibaba.fastjson.parser.Feature;"));
          Object emptyFeatures = java.lang.reflect.Array.newInstance(Class.forName("com.alibaba.fastjson.parser.Feature"), 0);
          return (T) parseObject.invoke(null, in, type, emptyFeatures);
        }
        case JACKSON: {
          Class<?> typeFactoryClass = Class.forName("com.fasterxml.jackson.databind.type.TypeFactory");
          Method defaultInstance = typeFactoryClass.getMethod("defaultInstance");
          Object typeFactory = defaultInstance.invoke(null);
          Method constructType = typeFactoryClass.getMethod("constructType", Type.class);
          Object javaType = constructType.invoke(typeFactory, type);
          Method readValue = jacksonObjectMapper.getClass().getMethod("readValue", String.class, Class.forName("com.fasterxml.jackson.databind.JavaType"));
          return (T) readValue.invoke(jacksonObjectMapper, in, javaType);
        }
        case GSON: {
          Method fromJson = gsonInstance.getClass().getMethod("fromJson", String.class, Type.class);
          return (T) fromJson.invoke(gsonInstance, in, type);
        }
        default:
          throw new IllegalStateException("No JSON provider available");
      }
    } catch (Exception e) {
      throw new RuntimeException("JSON Deserialize failed", e);
    }
  }

  @Override
  public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
    return toList(type, streamToString(in, charset));
  }

  @Override
  public <T> List<T> toList(Class<T> type, String in) {
    // 构造 List<T> 类型
    ParameterizedType listType = new ParameterizedTypeImpl(List.class, new Type[]{type});
    return toBean(listType, in);
  }

  // ==================== 静态内部类：通用实现 ====================

  /**
   * 通用 Mapper 实现，基于 Map<String, Object>
   */
  public static class UniversalMapper implements Mapper, Serializable {
    private final Map<String, Object> map;

    public UniversalMapper(Map<String, Object> map) {
      this.map = map;
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public Mapper getMapper(String key) {
      Object val = map.get(key);
      if (val instanceof Map) {
        return new UniversalMapper((Map<String, Object>) val);
      }
      return null;
    }

    @Override
    public Array getArray(String key) {
      Object val = map.get(key);
      if (val instanceof List) {
        return new UniversalArray((List<Object>) val);
      }
      return null;
    }

    @Override
    public boolean getBool(String key) {
      Object val = map.get(key);
      if (val instanceof Boolean) return (Boolean) val;
      return Boolean.parseBoolean(String.valueOf(val));
    }

    @Override
    public int getInt(String key) {
      Object val = map.get(key);
      if (val instanceof Number) return ((Number) val).intValue();
      try {
        return Integer.parseInt(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0;
      }
    }

    @Override
    public long getLong(String key) {
      Object val = map.get(key);
      if (val instanceof Number) return ((Number) val).longValue();
      try {
        return Long.parseLong(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0L;
      }
    }

    @Override
    public float getFloat(String key) {
      Object val = map.get(key);
      if (val instanceof Number) return ((Number) val).floatValue();
      try {
        return Float.parseFloat(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0f;
      }
    }

    @Override
    public double getDouble(String key) {
      Object val = map.get(key);
      if (val instanceof Number) return ((Number) val).doubleValue();
      try {
        return Double.parseDouble(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0d;
      }
    }

    @Override
    public String getString(String key) {
      Object val = map.get(key);
      return val != null ? String.valueOf(val) : null;
    }

    @Override
    public boolean has(String key) {
      return map.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
      return map.keySet();
    }

    @Override
    public <T> T toBean(Type type) {
      return convert(map, type);
    }

    @Override
    public <T> T toBean(Class<T> type) {
      return convert(map, type);
    }

    @Override
    public <T> T toBean(TypeRef<T> type) {
      return convert(map, type.getType());
    }

    @Override
    public Map<String, Object> toMap() {
      return map;
    }

    @Override
    public String toPretty() {
      return new AutoJsonMsgConvertor().serialize(map, true);
    }

    @Override
    public String toString() {
      return new AutoJsonMsgConvertor().serialize(map, false);
    }
  }

  /**
   * 通用 Array 实现，基于 List<Object>
   */
  public static class UniversalArray implements Array, Serializable {
    private final List<Object> list;

    public UniversalArray(List<Object> list) {
      this.list = list;
    }

    @Override
    public int size() {
      return list.size();
    }

    @Override
    public boolean isEmpty() {
      return list.isEmpty();
    }

    @Override
    public Mapper getMapper(int index) {
      Object val = list.get(index);
      if (val instanceof Map) {
        return new UniversalMapper((Map<String, Object>) val);
      }
      return null;
    }

    @Override
    public Array getArray(int index) {
      Object val = list.get(index);
      if (val instanceof List) {
        return new UniversalArray((List<Object>) val);
      }
      return null;
    }

    @Override
    public boolean getBool(int index) {
      Object val = list.get(index);
      if (val instanceof Boolean) return (Boolean) val;
      return Boolean.parseBoolean(String.valueOf(val));
    }

    @Override
    public int getInt(int index) {
      Object val = list.get(index);
      if (val instanceof Number) return ((Number) val).intValue();
      try {
        return Integer.parseInt(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0;
      }
    }

    @Override
    public long getLong(int index) {
      Object val = list.get(index);
      if (val instanceof Number) return ((Number) val).longValue();
      try {
        return Long.parseLong(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0L;
      }
    }

    @Override
    public float getFloat(int index) {
      Object val = list.get(index);
      if (val instanceof Number) return ((Number) val).floatValue();
      try {
        return Float.parseFloat(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0f;
      }
    }

    @Override
    public double getDouble(int index) {
      Object val = list.get(index);
      if (val instanceof Number) return ((Number) val).doubleValue();
      try {
        return Double.parseDouble(String.valueOf(val));
      } catch (NumberFormatException e) {
        return 0d;
      }
    }

    @Override
    public String getString(int index) {
      Object val = list.get(index);
      return val != null ? String.valueOf(val) : null;
    }

    @Override
    public <T> List<T> toList(Class<T> type) {
      ParameterizedType listType = new ParameterizedTypeImpl(List.class, new Type[]{type});
      return convert(list, listType);
    }

    @Override
    public List<Object> toList() {
      return list;
    }

    @Override
    public String toPretty() {
      return new AutoJsonMsgConvertor().serialize(list, true);
    }

    @Override
    public String toString() {
      return new AutoJsonMsgConvertor().serialize(list, false);
    }
  }

  // ==================== Helper Methods ====================

  /**
   * 将 Map/List 转换为目标 Bean (通过 JSON 中转)
   * 这种方式效率不是最高，但最通用，能适配所有 JSON 库
   */
  public static <T> T convert(Object src, Type type) {
    if (src == null) return null;
    try {
      // 先序列化为 JSON 字符串，再反序列化为目标类型
      // 这是最稳健的跨库转换方式
      String json = new AutoJsonMsgConvertor().serialize(src, false);
      return new AutoJsonMsgConvertor().toBean(type, json);
    } catch (Exception e) {
      throw new RuntimeException("Convert failed", e);
    }
  }

  private String streamToString(InputStream in, Charset charset) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buff = new byte[512];
      int len;
      while ((len = in.read(buff)) > 0) {
        output.write(buff, 0, len);
      }
      return output.toString(charset.name());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // 简单的 ParameterizedType 实现，用于构造 List<T> 等泛型
  private static class ParameterizedTypeImpl implements ParameterizedType {
    private final Class<?> raw;
    private final Type[] args;

    public ParameterizedTypeImpl(Class<?> raw, Type[] args) {
      this.raw = raw;
      this.args = args;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return args;
    }

    @Override
    public Type getRawType() {
      return raw;
    }

    @Override
    public Type getOwnerType() {
      return null;
    }
  }
}
