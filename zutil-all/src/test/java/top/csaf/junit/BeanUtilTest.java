package top.csaf.junit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.bean.BeanUtil;
import top.csaf.bean.PropFunc;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bean 工具类测试")
public class BeanUtilTest {

  @NoArgsConstructor
  @Data
  public static class TestSuperBean implements Serializable {
    private String superName;
  }

  @EqualsAndHashCode(callSuper = true)
  @NoArgsConstructor
  @Data
  public static class TestBean extends TestSuperBean implements Serializable {
    private String name;
    private Object deepObject;

    public TestBean(String name) {
      this.name = name;
    }
  }

  @DisplayName("toMap：Bean 转 Map、Bean List 转 Map List")
  @Test
  void toMap() {
    /** {@link BeanUtil#toMap(Object)} */
    assertThrows(NullPointerException.class, () -> BeanUtil.toMap((Object) null));

    Map<String, Object> map1 = new HashMap<>();
    map1.put("name", "6");
    map1.put("testBean7", new TestBean("7"));
    TestBean testBean5 = new TestBean();
    testBean5.setName("5");
    testBean5.setDeepObject(map1);
    TestBean testBean4 = new TestBean();
    testBean4.setName("4");
    testBean4.setDeepObject(new TestBean[]{testBean5});
    TestBean testBean3 = new TestBean();
    testBean3.setName("3");
    testBean3.setDeepObject(Collections.singletonList(testBean4).iterator());
    TestBean testBean2 = new TestBean();
    testBean2.setName("2");
    testBean2.setDeepObject(Collections.singletonList(testBean3));
    Map<String, Object> map = new HashMap<>();
    map.put("name", "1");
    map.put("testBean2", testBean2);
    map.put("map1", map1);

    Map testBean22 = (Map) BeanUtil.toMap(map).get("testBean2");
    Map<String, Object> testBean33 = ((Iterator<Map<String, Object>>) testBean22.get("deepObject")).next();
    Map<String, Object> testBean44 = ((Iterator<Map<String, Object>>) testBean33.get("deepObject")).next();
    Map<String, Object> testBean55 = ((Map<String, Object>[]) testBean44.get("deepObject"))[0];
    Map<String, Object> testBean66 = (Map<String, Object>) ((Map<String, Object>) testBean55.get("deepObject")).get("testBean7");
    assertEquals("7", testBean66.get("name"));

    /** {@link BeanUtil#toMap(List)} */
    assertThrows(NullPointerException.class, () -> BeanUtil.toMap((List) null));
    assertEquals(new ArrayList(), BeanUtil.toMap(new ArrayList()));

    // 上面测试时已经把 testBean3.deepObject 给 next 掉了，所以这里重新赋值
    testBean3.setDeepObject(Collections.singletonList(testBean4).iterator());
    testBean22 = (Map) BeanUtil.toMap(Collections.singletonList(map)).get(0).get("testBean2");
    testBean33 = ((Iterator<Map<String, Object>>) testBean22.get("deepObject")).next();
    testBean44 = ((Iterator<Map<String, Object>>) testBean33.get("deepObject")).next();
    testBean55 = ((Map<String, Object>[]) testBean44.get("deepObject"))[0];
    testBean66 = (Map<String, Object>) ((Map<String, Object>) testBean55.get("deepObject")).get("testBean7");
    assertEquals("7", testBean66.get("name"));
  }

  @DisplayName("根据属性名获取属性值")
  @Test
  void getPropertyName() {
    assertThrows(NullPointerException.class, () -> BeanUtil.getPropertyName(null));
    assertEquals("name", BeanUtil.getPropertyName(TestBean::getName));
  }

  @DisplayName("获取列名")
  @Test
  void getColumnName() {
    assertThrows(NullPointerException.class, () -> BeanUtil.getColumnName(null));
    assertEquals("deep_object", BeanUtil.getColumnName(TestBean::getDeepObject));
  }

  @DisplayName("根据属性名获取属性类型")
  @Test
  void getPropertyClass() {
    assertThrows(NullPointerException.class, () -> BeanUtil.getPropertyClass(null));
    assertEquals(String.class, BeanUtil.getPropertyClass(TestBean::getName));
  }

  @DisplayName("根据属性名获取属性值")
  @Test
  void getProperty() {
    TestBean testBean = new TestBean();
    testBean.setName("1");

    /** {@link BeanUtil#getProperty(Object, String) */
    assertThrows(NullPointerException.class, () -> BeanUtil.getProperty(null, "name"));
    assertThrows(NullPointerException.class, () -> BeanUtil.getProperty(testBean, (String) null));
    assertEquals(null, BeanUtil.getProperty(testBean, ""));
    assertEquals("1", BeanUtil.getProperty(testBean, "name"));
    assertNull(BeanUtil.getProperty(testBean, "name1"));

    /** {@link BeanUtil#getProperty(Object, PropFunc) */
    assertThrows(NullPointerException.class, () -> BeanUtil.getProperty(null, TestBean::getName));
    assertThrows(NullPointerException.class, () -> BeanUtil.getProperty(testBean, (PropFunc) null));
    assertEquals("1", BeanUtil.getProperty(testBean, TestBean::getName));

    /** {@link BeanUtil#getPropertyStr(Object, String) */
    assertThrows(NullPointerException.class, () -> BeanUtil.getPropertyStr(null, "name"));
    assertThrows(NullPointerException.class, () -> BeanUtil.getPropertyStr(testBean, (String) null));
    assertEquals(null, BeanUtil.getPropertyStr(testBean, ""));
    testBean.setDeepObject(2);
    assertEquals("2", BeanUtil.getPropertyStr(testBean, "deepObject"));

    /** {@link BeanUtil#getPropertyStr(Object, PropFunc) */
    assertThrows(NullPointerException.class, () -> BeanUtil.getPropertyStr(null, TestBean::getName));
    assertThrows(NullPointerException.class, () -> BeanUtil.getPropertyStr(testBean, (PropFunc) null));
    assertEquals("1", BeanUtil.getPropertyStr(testBean, TestBean::getName));
  }

  @DisplayName("判断对象是否包含某个属性")
  @Test
  void hasProperty() {
    TestBean testBean = new TestBean();

    assertTrue(BeanUtil.hasProperty(testBean, "name"));
    assertFalse(BeanUtil.hasProperty(testBean, "name1"));
  }

  @DisplayName("根据属性名设置属性值")
  @Test
  void setProperty() {
    TestBean testBean = new TestBean();
    testBean.setName("1");

    /** {@link BeanUtil#setProperty(Object, String, Object)} */
    assertThrows(NullPointerException.class, () -> BeanUtil.setProperty(null, "name", "2"));
    assertThrows(NullPointerException.class, () -> BeanUtil.setProperty(testBean, (String) null, "2"));
    assertTrue(BeanUtil.setProperty(testBean, "name", "2"));
    assertEquals("2", testBean.getName());
    assertFalse(BeanUtil.setProperty(testBean, "name1", "2"));

    /** {@link BeanUtil#setProperty(Object, PropFunc, Object)} */
    assertThrows(NullPointerException.class, () -> BeanUtil.setProperty(null, TestBean::getName, "3"));
    assertThrows(NullPointerException.class, () -> BeanUtil.setProperty(testBean, (PropFunc) null, "3"));
    assertTrue(BeanUtil.setProperty(testBean, TestBean::getName, "3"));
  }

  @Data
  class TestBeanNotHaveNoArgsConstructor {
    private String name;

    TestBeanNotHaveNoArgsConstructor(String name) {
      this.name = name;
    }
  }

  @DisplayName("复制属性到新类型对象中")
  @Test
  void copyProperties() {
    TestSuperBean testBean = new TestSuperBean();
    testBean.setSuperName("1");

    /** {@link BeanUtil#copyProperties(Object, Class)} */
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties(null, TestBean.class));
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties(testBean, null));
    assertThrows(IllegalArgumentException.class, () -> BeanUtil.copyProperties(testBean, TestBeanNotHaveNoArgsConstructor.class));
    TestBean testBean1 = BeanUtil.copyProperties(testBean, TestBean.class);
    assertEquals("1", testBean1.getSuperName());

    /** {@link BeanUtil#copyProperties(List, Class)} */
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties(null, TestBean.class));
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties(Collections.singletonList(testBean), null));
    assertThrows(IllegalArgumentException.class, () -> BeanUtil.copyProperties(Collections.singletonList(testBean), TestBeanNotHaveNoArgsConstructor.class));
    List<TestBean> testBean2 = BeanUtil.copyProperties(Collections.singletonList(testBean), TestBean.class);
    assertEquals("1", testBean2.get(0).getSuperName());
  }
}
