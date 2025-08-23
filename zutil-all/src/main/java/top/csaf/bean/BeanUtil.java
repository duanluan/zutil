package top.csaf.bean;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import top.csaf.coll.CollUtil;
import top.csaf.json.JsonUtil;
import top.csaf.lang.StrUtil;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Bean 工具类
 */
@Slf4j
public class BeanUtil extends org.springframework.beans.BeanUtils {

  /**
   * 将 Bean（implements java.io.Serializable）深层转换为 Map 对象，支持任意深度的对象属性转换，可循环字段会被转换为 Iterator
   *
   * @param object 需要转换的 Java Bean 对象
   * @return 转换后的 Map 对象
   */
  public static Map<String, Object> toMap(@NonNull Object object) {
    Map<String, Object> result = new HashMap<>();
    Map map;
    try {
      if (object instanceof Map) {
        map = (Map) object;
      } else {
        map = PropertyUtils.describe(object);
      }
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    for (Object o : map.entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      if (value == null) {
        result.put(key, value);
        continue;
      }
      // 基础类型
      if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character) {
        result.put(key, value);
        continue;
      }
      Class<?> valueClass = value.getClass();
      // 其他类型递归调用
      if (value instanceof Map) {
        Map<String, Object> newValue = new HashMap<>();
        for (Map.Entry<Object, Object> entry1 : ((Map<Object, Object>) value).entrySet()) {
          String key1 = entry1.getKey().toString();
          Object value1 = entry1.getValue();
          // 如果还有深层
          if (value1 != null && (value1 instanceof Map || (value1 instanceof Serializable && !(value1 instanceof CharSequence)) || value1 instanceof Iterable || value1 instanceof Iterator || value1.getClass().isArray())) {
            newValue.put(key1, toMap(value1));
          } else {
            newValue.put(key1, value1);
          }
        }
        result.put(key, newValue);
      } else if (value instanceof Iterable) {
        // TODO 根据 Iterable 的具体类型创建新的值
        List newValue = new ArrayList<>();
        for (Object value1 : (Iterable) value) {
          // 如果还有深层
          if (value1 != null && (value1 instanceof Map || (value1 instanceof Serializable && !(value1 instanceof CharSequence)) || value1 instanceof Iterable || value1 instanceof Iterator || value1.getClass().isArray())) {
            newValue.add(toMap(value1));
          } else {
            newValue.add(value1);
          }
        }
        result.put(key, newValue.iterator());
      } else if (value instanceof Iterator) {
        // TODO 根据 Iterator 的具体类型创建新的值
        List newValue = new ArrayList<>();
        Iterator iterator = (Iterator) value;
        while (iterator.hasNext()) {
          Object value1 = iterator.next();
          // 如果还有深层
          if (value1 != null && (value1 instanceof Map || (value1 instanceof Serializable && !(value1 instanceof CharSequence)) || value1 instanceof Iterable || value1 instanceof Iterator || value1.getClass().isArray())) {
            newValue.add(toMap(value1));
          } else {
            newValue.add(value1);
          }
        }
        result.put(key, newValue.iterator());
      } else if (value != null && valueClass.isArray()) {
        int arrayLen = Array.getLength(value);
        Object newValue = Array.newInstance(Map.class, arrayLen);
        for (int i = 0; i < arrayLen; i++) {
          Object value1 = Array.get(value, i);
          // 如果还有深层
          if (value1 != null && (value1 instanceof Map || (value1 instanceof Serializable && !(value1 instanceof CharSequence)) || value1 instanceof Iterable || value1 instanceof Iterator || value1.getClass().isArray())) {
            Array.set(newValue, i, toMap(value1));
          } else {
            Array.set(newValue, i, value1);
          }
        }
        result.put(key, newValue);
      } else {
        result.put(key, toMap(value));
      }
    }
    return result;
  }

  /**
   * Bean（implements java.io.Serializable） List 深层转 Map List
   *
   * @param sourceList Bean List
   * @param <T>        Bean 类型
   * @return Map List
   */
  public static <T> List<Map<String, Object>> toMap(@NonNull List<T> sourceList) {
    List<Map<String, Object>> result = new ArrayList<>();
    if (CollUtil.isEmpty(sourceList)) {
      return result;
    }
    for (T t : sourceList) {
      result.add(toMap(t));
    }
    return result;
  }

  /**
   * 获取属性名
   *
   * @param <T> 属性所属类
   * @param fn  属性 Getter
   * @return 属性名
   */
  public static <T> String getPropertyName(@NonNull PropFunc<T, ?> fn) {
    try {
      Method method = fn.getClass().getDeclaredMethod("writeReplace");
      method.setAccessible(true);
      Object serializedLambda = method.invoke(fn);
      Method implMethodName = serializedLambda.getClass().getDeclaredMethod("getImplMethodName");
      implMethodName.setAccessible(true);
      String methodName = (String) implMethodName.invoke(serializedLambda);
      return Introspector.decapitalize(methodName.substring(3));
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 获取列名
   *
   * @param <T> 类
   * @param fn  属性 Getter
   * @return 列名
   */
  public static <T> String getColumnName(@NonNull PropFunc<T, ?> fn) {
    return StrUtil.toUnderscore(getPropertyName(fn));
  }

  /**
   * 获取属性类
   *
   * @param fn  属性 Getter
   * @param <T> 属性所属类
   * @return 属性类
   */
  public static <T> Class<?> getPropertyClass(@NonNull PropFunc<T, ?> fn) {
    try {
      Method method = fn.getClass().getDeclaredMethod("writeReplace");
      method.setAccessible(true);
      Object serializedLambda = method.invoke(fn);
      Method implMethodName = serializedLambda.getClass().getDeclaredMethod("getImplMethodName");
      implMethodName.setAccessible(true);
      String methodName = (String) implMethodName.invoke(serializedLambda);
      String className = serializedLambda.getClass().getDeclaredMethod("getImplClass").invoke(serializedLambda).toString().replace("/", ".");
      Class<T> clazz = (Class<T>) Class.forName(className);
      return clazz.getDeclaredField(Introspector.decapitalize(methodName.substring(3))).getType();
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 根据属性名获取属性值
   *
   * @param bean      对象
   * @param fieldName 属性名
   * @return 属性值
   */
  public static Object getProperty(@NonNull Object bean, @NonNull String fieldName) {
    if (StrUtil.isBlank(fieldName)) {
      return null;
    }
    try {
      PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(bean.getClass(), fieldName);
      if (propertyDescriptor != null) {
        Method readMethod = propertyDescriptor.getReadMethod();
        if (readMethod != null) {
          return readMethod.invoke(bean);
        }
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  /**
   * 根据属性 Getter 获取属性值
   *
   * @param bean    对象
   * @param fieldFn 属性 Getter
   * @param <T>     属性所属类
   * @return 属性值
   */
  public static <T> Object getProperty(@NonNull Object bean, @NonNull PropFunc<T, ?> fieldFn) {
    return getProperty(bean, getPropertyName(fieldFn));
  }

  /**
   * 根据属性名获取属性值字符串
   *
   * @param bean      对象
   * @param fieldName 属性名
   * @return 属性值字符串
   */
  public static String getPropertyStr(@NonNull Object bean, @NonNull String fieldName) {
    Object object = getProperty(bean, fieldName);
    if (object != null) {
      return object.toString();
    }
    return null;
  }

  /**
   * 根据属性 Getter 获取属性值字符串
   *
   * @param bean    对象
   * @param fieldFn 属性 Getter
   * @param <T>     属性所属类
   * @return 属性值字符串
   */
  public static <T> String getPropertyStr(@NonNull Object bean, @NonNull PropFunc<T, ?> fieldFn) {
    return getPropertyStr(bean, getPropertyName(fieldFn));
  }

  /**
   * 判断对象是否包含某个属性
   *
   * @param bean      对象
   * @param fieldName 属性名
   * @return true 表示存在该属性，false 表示不存在
   */
  public static boolean hasProperty(@NonNull Object bean, @NonNull String fieldName) {
    if (StrUtil.isBlank(fieldName)) {
      return false;
    }
    PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(bean.getClass(), fieldName);
    return propertyDescriptor != null;
  }

  /**
   * 根据属性名设置属性值
   *
   * @param bean      对象
   * @param fieldName 属性名
   * @param value     属性值
   * @return 是否设置成功
   */
  public static boolean setProperty(@NonNull Object bean, @NonNull String fieldName, Object value) {
    try {
      PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(bean.getClass(), fieldName);
      if (propertyDescriptor != null) {
        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (writeMethod != null) {
          writeMethod.invoke(bean, value);
          return true;
        }
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.error(e.getMessage(), e);
    }
    return false;
  }

  /**
   * 根据属性 Getter 设置属性值
   *
   * @param bean    对象
   * @param fieldFn 属性 Getter
   * @param value   属性值
   * @param <T>     属性所属类
   * @return 是否设置成功
   */
  public static <T> boolean setProperty(@NonNull Object bean, @NonNull PropFunc<T, ?> fieldFn, Object value) {
    return setProperty(bean, getPropertyName(fieldFn), value);
  }

  /**
   * 复制属性到新类型对象中
   *
   * @param source      对象
   * @param targetClass 目标类
   * @param <T>         指定类型
   * @return 指定类型的新对象
   */
  public static <T> T copyProperties(@NonNull Object source, @NonNull Class<T> targetClass) {
    // 判断目标类是否存在无参构造函数
    boolean hasNoArgsConstructor = false;
    Constructor<?>[] constructors = targetClass.getConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.getParameterCount() == 0) {
        hasNoArgsConstructor = true;
        break;
      }
    }
    if (!hasNoArgsConstructor) {
      throw new IllegalArgumentException("targetClass: " + targetClass.getName() + " must have no args constructor");
    }

    T target = null;
    try {
      target = targetClass.newInstance();
      org.springframework.beans.BeanUtils.copyProperties(source, target);
    } catch (InstantiationException | IllegalAccessException e) {
      log.error(e.getMessage(), e);
    }
    return target;
  }

  /**
   * 复制属性到新类型对象列表中
   *
   * @param sourceList  对象列表
   * @param targetClass 目标类
   * @param <T>         目标类型
   * @return 指定类型的对象列表
   */
  public static <T> List<T> copyProperties(@NonNull List<?> sourceList, @NonNull Class<T> targetClass) {
    // 判断目标类是否存在无参构造函数
    boolean hasNoArgsConstructor = false;
    Constructor<?>[] constructors = targetClass.getConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.getParameterCount() == 0) {
        hasNoArgsConstructor = true;
        break;
      }
    }
    if (!hasNoArgsConstructor) {
      throw new IllegalArgumentException("targetClass: " + targetClass.getName() + " must have no args constructor");
    }

    List<T> targetList = new ArrayList<>();
    for (Object source : sourceList) {
      T target = null;
      try {
        target = targetClass.newInstance();
        org.springframework.beans.BeanUtils.copyProperties(source, target);
      } catch (InstantiationException | IllegalAccessException e) {
        log.error(e.getMessage(), e);
      }
      targetList.add(target);
    }
    return targetList;
  }

  /**
   * 深克隆
   *
   * @param source 源对象
   * @param <T>    源对象类型
   * @return 深克隆后的对象
   */
  public static <T> T deepClone(T source) {
    // return (T) JSON.parseObject(JSON.toJSONString(source), source.getClass());
    return (T) JsonUtil.parseObject(JsonUtil.toJsonNoFeature(source), source.getClass());
  }

  /**
   * 深克隆
   *
   * @param source 源集合
   * @param <T>    源集合元素类型
   * @return 深克隆后的集合
   */
  public static <T> List<T> deepClone(List<T> source) {
    List<T> result = new ArrayList<>();
    for (T t : source) {
      result.add(deepClone(t));
    }
    return result;
  }
}
