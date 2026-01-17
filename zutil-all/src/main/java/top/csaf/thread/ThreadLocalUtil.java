package top.csaf.thread;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal 工具类，使用 TransmittableThreadLocal
 */
public class ThreadLocalUtil {

  private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<Map<String, Object>>() {

    /**
     * 初始化值
     */
    @Override
    protected Map<String, Object> initialValue() {
      return new HashMap<>();
    }

    /**
     * 仅在使用 TtlRunnable/TtlCallable 包装任务进行跨线程池传递时调用。
     * 返回一个新的 Map，实现父子线程的数据隔离。
     */
    @Override
    public Map<String, Object> copy(Map<String, Object> parentValue) {
      return parentValue != null ? new HashMap<>(parentValue) : new HashMap<>();
    }

    /**
     * 仅在直接 new Thread() 创建子线程时调用（Standard InheritableThreadLocal behavior）。
     * 同样返回一个新的 Map。
     */
    @Override
    protected Map<String, Object> childValue(Map<String, Object> parentValue) {
      return parentValue != null ? new HashMap<>(parentValue) : new HashMap<>();
    }
  };

  /**
   * 设置变量
   */
  public static void set(String key, Object value) {
    THREAD_LOCAL.get().put(key, value);
  }

  /**
   * 获取变量
   */
  public static Object get(String key) {
    return THREAD_LOCAL.get().get(key);
  }

  /**
   * 删除指定 Key 的变量
   */
  public static void remove(String key) {
    THREAD_LOCAL.get().remove(key);
  }

  /**
   * 清除当前线程的所有变量 (移除 ThreadLocal 本身)
   * 建议在请求结束或任务结束后调用，防止内存泄漏
   */
  public static void clear() {
    THREAD_LOCAL.remove();
  }

  /**
   * 获取当前线程的所有变量副本
   */
  public static Map<String, Object> getAll() {
    return new HashMap<>(THREAD_LOCAL.get());
  }
}
