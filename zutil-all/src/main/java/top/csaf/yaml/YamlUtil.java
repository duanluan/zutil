package top.csaf.yaml;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * YAML 工具类
 *
 * @author ZhongJianhao
 */
@Slf4j
public class YamlUtil {

  /**
   * 根据 key 从 YAML Map 中获取 value
   *
   * @param yamlMap YamlUtils.load 方法返回的 Map
   * @param key     key
   * @return value
   */
  public static Object get(@NonNull final Map<String, Object> yamlMap, @NonNull final String key) {
    // 将 key 拆分成一个个子 key
    String[] keys = key.split("\\.");
    // 从 YAML Map 开始搜索
    Map<String, Object> searchMap = yamlMap;
    for (int i = 0; i < keys.length; i++) {
      // 获取当前 key 对应的 value
      Object value = searchMap.get(keys[i]);
      // 如果 value 为空，直接返回 null
      if (value == null) {
        return null;
        // 如果 value 是 Map 类型，说明还需要往下搜索
      } else if (value instanceof Map) {
        searchMap = (Map<String, Object>) value;
      }
      // 如果已经搜索到最后一个 key，直接返回对应的 value
      if (i == keys.length - 1) {
        return value;
      }
    }
    return null;
  }

  /**
   * 根据 key 从 YAML 文件中获取 value
   *
   * @param io       Reader
   * @param key      key
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return value
   */
  public static Object get(@NonNull final Reader io, @NonNull final String key, final boolean isEscape) {
    return get(load(io, isEscape), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value，会替换 ${xxx} 为对应的值
   *
   * @param io  Reader
   * @param key key
   * @return value
   */
  public static Object get(@NonNull final Reader io, @NonNull final String key) {
    return get(load(io, true), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value
   *
   * @param filePath 文件路径
   * @param key      key
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return value
   */
  public static Object get(@NonNull final String filePath, @NonNull final String key, final boolean isEscape) {
    return get(load(filePath, isEscape), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value，会替换 ${xxx} 为对应的值
   *
   * @param filePath 文件路径
   * @param key      key
   * @return value
   */
  public static Object get(@NonNull final String filePath, @NonNull final String key) {
    return get(load(filePath, true), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value
   *
   * @param file     文件
   * @param key      key
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return value
   */
  public static Object get(@NonNull final File file, @NonNull final String key, final boolean isEscape) {
    return get(load(file, isEscape), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value，会替换 ${xxx} 为对应的值
   *
   * @param file 文件
   * @param key  key
   * @return value
   */
  public static Object get(@NonNull final File file, @NonNull final String key) {
    return get(load(file, true), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value
   *
   * @param input    InputStream
   * @param key      key
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return value
   */
  public static Object get(@NonNull final InputStream input, @NonNull final String key, final boolean isEscape) {
    return get(load(input, isEscape), key);
  }

  /**
   * 根据 key 从 YAML 文件中获取 value，会替换 ${xxx} 为对应的值
   *
   * @param input InputStream
   * @param key   key
   * @return value
   */
  public static Object get(@NonNull final InputStream input, @NonNull final String key) {
    return get(load(input, true), key);
  }

  /**
   * 将值中的 ${xxx} 无限层级替换为对应的值
   *
   * @param allMap                       YAML 文件转换的 Map
   * @param childMap                     子集 Map
   * @param isRecursive                  是否为递归，递归返回子集 Map，非递归返回最终 Map
   * @param escapeNotFoundReplacement    值中的 ${xxx} 需要转义但是找不到时的替换值
   * @param escapeNotFoundThrowException 值中的 ${xxx} 需要转义但是找不到时是否抛出异常
   * @return 替换后的 Map
   */
  private static Map<String, Object> escape(@NonNull final Map<String, Object> allMap, Map<String, Object> childMap, boolean isRecursive, Object escapeNotFoundReplacement, Boolean escapeNotFoundThrowException) {
    if (childMap == null) {
      childMap = allMap;
    }

    for (Map.Entry<String, Object> entry : childMap.entrySet()) {
      Object value = entry.getValue();
      // 如果是 Map，说明有子集，就递归
      if (value instanceof Map) {
        entry.setValue(escape(allMap, (Map<String, Object>) value, true, escapeNotFoundReplacement, escapeNotFoundThrowException));
      }
      // 如果是 String，说明是值，就替换
      else if (value instanceof String) {
        String value1 = (String) value;

        int startIndex = 0;
        int leftIndex;
        int rightIndex;
        // 循环替换，直到没有 ${} 为止
        while ((leftIndex = value1.indexOf("${", startIndex)) != -1 && (rightIndex = value1.indexOf("}", leftIndex + 2)) != -1) {
          // 获取 ${} 中的 key
          String needReplaceKey = value1.substring(leftIndex + 2, rightIndex);
          // 获取对应的 value
          Object value2 = get(allMap, needReplaceKey);
          // 没有找到对应的 value
          if (value2 == null) {
            // 如果配置了抛出异常，就抛出异常
            if (Boolean.TRUE.equals(escapeNotFoundThrowException)) {
              throw new IllegalArgumentException("need replace key not found: " + needReplaceKey);
            } else if (escapeNotFoundReplacement == null) {
              startIndex = rightIndex + 1;
              continue;
            } else {
              value2 = escapeNotFoundReplacement;
            }
          }
          // 替换
          value1 = value1.replace("${" + needReplaceKey + "}", value2.toString());
          // 因为替换后的值中可能还有 ${}，长度也可能会变长，所以就不加 startIndex 了
        }
        entry.setValue(value1);
      }
    }
    return isRecursive ? childMap : allMap;
  }

  /**
   * 加载 YAML 文件为 Map
   *
   * @param io       Reader
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final Reader io, final boolean isEscape) {
    Map<String, Object> map = new Yaml().loadAs(io, Map.class);
    if (isEscape) {
      return escape(map, null, isEscape, YamlFeat.getEscapeNotFoundReplacement(), YamlFeat.getEscapeNotFoundThrowException());
    }
    return map;
  }

  /**
   * 加载 YAML 文件为 Map，会替换 ${xxx} 为对应的值
   *
   * @param io Reader
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final Reader io) {
    return load(io, true);
  }

  /**
   * 加载 YAML 文件为 Map
   *
   * @param filePath 文件路径
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final String filePath, final boolean isEscape) {
    Path path = Paths.get(filePath);
    // 校验是否为普通文件，防止传入目录路径导致 "Is a directory" 异常
    if (!Files.isRegularFile(path)) {
      return Collections.emptyMap();
    }
    try {
      Map<String, Object> map = new Yaml().loadAs(Files.newInputStream(path), Map.class);
      if (isEscape) {
        return escape(map, null, isEscape, YamlFeat.getEscapeNotFoundReplacement(), YamlFeat.getEscapeNotFoundThrowException());
      }
      return map;
    } catch (IOException e) {
      log.error("load yaml file error", e);
      return Collections.emptyMap();
    }
  }

  /**
   * 加载 YAML 文件为 Map，会替换 ${xxx} 为对应的值
   *
   * @param filePath 文件路径
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final String filePath) {
    return load(filePath, true);
  }

  /**
   * 加载 YAML 文件为 Map
   *
   * @param file     文件
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final File file, final boolean isEscape) {
    // 校验是否为普通文件，防止传入目录对象导致 "Is a directory" 异常
    if (!file.isFile()) {
      return Collections.emptyMap();
    }
    try {
      Map<String, Object> map = new Yaml().loadAs(Files.newInputStream(file.toPath()), Map.class);
      if (isEscape) {
        return escape(map, null, isEscape, YamlFeat.getEscapeNotFoundReplacement(), YamlFeat.getEscapeNotFoundThrowException());
      }
      return map;
    } catch (IOException e) {
      log.error("load yaml file error", e);
      return Collections.emptyMap();
    }
  }

  /**
   * 加载 YAML 文件为 Map，会替换 ${xxx} 为对应的值
   *
   * @param file 文件
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final File file) {
    return load(file, true);
  }

  /**
   * 加载 YAML 文件为 Map
   *
   * @param input    InputStream
   * @param isEscape 是否替换 ${xxx} 为对应的值
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final InputStream input, final boolean isEscape) {
    Map<String, Object> map = new Yaml().loadAs(input, Map.class);
    if (isEscape) {
      return escape(map, null, isEscape, YamlFeat.getEscapeNotFoundReplacement(), YamlFeat.getEscapeNotFoundThrowException());
    }
    return map;
  }

  /**
   * 加载 YAML 文件为 Map，会替换 ${xxx} 为对应的值
   *
   * @param input InputStream
   * @return Map
   */
  public static Map<String, Object> load(@NonNull final InputStream input) {
    return load(input, true);
  }
}
