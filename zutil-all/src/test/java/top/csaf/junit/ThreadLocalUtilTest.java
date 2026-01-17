package top.csaf.junit;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.thread.ThreadLocalUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ThreadLocalUtil 单元测试
 */
@DisplayName("ThreadLocalUtil 线程本地变量工具测试")
class ThreadLocalUtilTest {

  @AfterEach
  void tearDown() {
    // 每个测试结束后清理，防止污染
    ThreadLocalUtil.clear();
  }

  /**
   * 测试基础的 CRUD 操作
   * 覆盖：set, get, remove, getAll
   */
  @Test
  @DisplayName("基础功能测试：set/get/remove/getAll")
  void testBasicOperations() {
    // Set & Get
    ThreadLocalUtil.set("key1", "value1");
    assertEquals("value1", ThreadLocalUtil.get("key1"));
    assertNull(ThreadLocalUtil.get("key2"));

    // getAll
    Map<String, Object> all = ThreadLocalUtil.getAll();
    assertEquals(1, all.size());
    assertEquals("value1", all.get("key1"));

    // 修改副本不影响原值 (验证 getAll 返回的是拷贝)
    all.put("key1", "changed");
    assertEquals("value1", ThreadLocalUtil.get("key1"));

    // Remove
    ThreadLocalUtil.remove("key1");
    assertNull(ThreadLocalUtil.get("key1"));
  }

  /**
   * 测试 clear 方法
   * 覆盖：clear, initialValue (重新获取时触发)
   */
  @Test
  @DisplayName("清理功能测试：clear")
  void testClear() {
    ThreadLocalUtil.set("key1", "value1");
    ThreadLocalUtil.clear();
    assertNull(ThreadLocalUtil.get("key1"));
    assertTrue(ThreadLocalUtil.getAll().isEmpty());
  }

  /**
   * 测试普通线程继承性 (childValue)
   * 验证：子线程继承父线程数据，且具备隔离性（深拷贝）
   */
  @Test
  @DisplayName("线程隔离测试：子线程继承与深拷贝 (new Thread)")
  void testThreadInheritance() throws InterruptedException {
    ThreadLocalUtil.set("parentKey", "parentVal");

    AtomicReference<Object> childValRef = new AtomicReference<>();
    AtomicReference<Object> childGetParentKeyRef = new AtomicReference<>();

    Thread childThread = new Thread(() -> {
      // 1. 验证能取到父线程的值
      childGetParentKeyRef.set(ThreadLocalUtil.get("parentKey"));

      // 2. 子线程修改值
      ThreadLocalUtil.set("parentKey", "childModified");
      ThreadLocalUtil.set("childKey", "childVal");
      childValRef.set(ThreadLocalUtil.get("childKey"));
    });

    childThread.start();
    childThread.join();

    // 断言子线程行为
    assertEquals("parentVal", childGetParentKeyRef.get(), "子线程应能继承父线程变量");
    assertEquals("childVal", childValRef.get());

    // 断言父线程不受影响 (隔离性)
    assertEquals("parentVal", ThreadLocalUtil.get("parentKey"), "父线程变量不应被子线程修改");
    assertNull(ThreadLocalUtil.get("childKey"), "父线程不应看到子线程的新增变量");
  }

  /**
   * 测试线程池继承性 (copy)
   * 验证：TTL 包装任务能继承数据，且具备隔离性（深拷贝）
   */
  @Test
  @DisplayName("线程池隔离测试：TTL 跨线程传递与深拷贝")
  void testThreadPoolInheritance() throws ExecutionException, InterruptedException {
    ThreadLocalUtil.set("poolKey", "mainVal");

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    // 使用 TtlRunnable 包装，触发 copy 方法
    Runnable task = TtlRunnable.get(() -> {
      // 1. 验证继承
      assertEquals("mainVal", ThreadLocalUtil.get("poolKey"));

      // 2. 验证隔离：修改值
      ThreadLocalUtil.set("poolKey", "poolModified");
      assertEquals("poolModified", ThreadLocalUtil.get("poolKey"));
    });

    Future<?> future = executorService.submit(task);
    future.get(); // 等待执行完成

    // 验证主线程不受影响
    assertEquals("mainVal", ThreadLocalUtil.get("poolKey"), "主线程变量不应被线程池任务修改");

    executorService.shutdown();
  }

  /**
   * 通过反射测试内部受保护方法的边界情况
   * 覆盖：initialValue, copy, childValue 中的 null 分支
   */
  @Test
  @DisplayName("覆盖率补充测试：内部受保护方法边界条件")
  void testInternalProtectedMethods() throws Exception {
    // 1. 获取私有静态字段 THREAD_LOCAL
    Field field = ThreadLocalUtil.class.getDeclaredField("THREAD_LOCAL");
    field.setAccessible(true);
    TransmittableThreadLocal<Map<String, Object>> ttlInstance =
      (TransmittableThreadLocal<Map<String, Object>>) field.get(null);

    // 2. 获取并测试 initialValue
    // 修复：直接从实例的类（匿名内部类）获取方法，去掉报错的 TransmittableThreadLocal.class.getDeclaredMethod
    Method initialValue = ttlInstance.getClass().getDeclaredMethod("initialValue");
    initialValue.setAccessible(true);
    Object initResult = initialValue.invoke(ttlInstance);
    assertNotNull(initResult);
    assertTrue(((Map<?, ?>) initResult).isEmpty());

    // 3. 测试 copy 方法 (覆盖 parentValue == null 的分支)
    // 注意：copy 是 public 方法，但为了统一处理或者如果它是 protected，用 getDeclaredMethod 也是对的
    // 这里因为是匿名内部类重写了 copy，所以从 ttlInstance.getClass() 获取是正确的
    Method copyMethod = ttlInstance.getClass().getDeclaredMethod("copy", Map.class);
    copyMethod.setAccessible(true);

    // Case A: parentValue != null
    // Java 8 兼容写法: Collections.singletonMap 代替 Map.of
    Map<String, Object> parentMap = (Map<String, Object>) copyMethod.invoke(ttlInstance, Collections.singletonMap("k", "v"));
    assertEquals("v", parentMap.get("k"));

    // Case B: parentValue == null (触发防御性分支)
    Map<String, Object> nullResult = (Map<String, Object>) copyMethod.invoke(ttlInstance, (Object) null);
    assertNotNull(nullResult);
    assertTrue(nullResult.isEmpty());

    // 4. 测试 childValue 方法 (覆盖 parentValue == null 的分支)
    Method childValueMethod = ttlInstance.getClass().getDeclaredMethod("childValue", Map.class);
    childValueMethod.setAccessible(true);

    // Case A: parentValue != null
    Map<String, Object> childMap = (Map<String, Object>) childValueMethod.invoke(ttlInstance, Collections.singletonMap("k", "v"));
    assertEquals("v", childMap.get("k"));

    // Case B: parentValue == null (触发防御性分支)
    Map<String, Object> childNullResult = (Map<String, Object>) childValueMethod.invoke(ttlInstance, (Object) null);
    assertNotNull(childNullResult);
    assertTrue(childNullResult.isEmpty());
  }
}
