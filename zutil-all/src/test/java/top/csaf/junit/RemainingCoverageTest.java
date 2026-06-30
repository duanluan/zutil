package top.csaf.junit;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import top.csaf.bean.BeanUtil;
import top.csaf.bean.ConvertUtil;
import top.csaf.bean.PropFunc;
import top.csaf.crypto.AesUtil;
import top.csaf.crypto.BlockCipher;
import top.csaf.crypto.BlockCipherUtil;
import top.csaf.crypto.DesUtil;
import top.csaf.crypto.DigestUtil;
import top.csaf.crypto.Md5Util;
import top.csaf.crypto.Sm4Util;
import top.csaf.crypto.enums.BlockCipherType;
import top.csaf.crypto.enums.EncodingType;
import top.csaf.crypto.enums.Mode;
import top.csaf.crypto.enums.Padding;
import top.csaf.date.DateUtil;
import top.csaf.idcard.IdCardUtil;
import top.csaf.io.FileUtil;
import top.csaf.tree.TreeConfig;
import top.csaf.tree.TreeNode;
import top.csaf.tree.TreeUtil;
import top.csaf.text.UnicodeUtil;
import top.csaf.thread.RetryUtil;
import top.csaf.thread.ThreadLocalUtil;
import top.csaf.yaml.YamlFeat;
import top.csaf.yaml.YamlFeatConfig;
import top.csaf.yaml.YamlUtil;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("剩余覆盖率补充测试")
class RemainingCoverageTest {

  private static final String YML_FILE_PATH = FileUtil.getProjectPath() + "/src/test/java/top/csaf/assets/yaml/test.yml";
  private static final File YML_FILE = new File(YML_FILE_PATH);
  private static final String XML_FILE_PATH = new File("zutil-all/src/test/java/top/csaf/assets/xml/test.xml").exists()
    ? new File("zutil-all/src/test/java/top/csaf/assets/xml/test.xml").getAbsolutePath()
    : new File("src/test/java/top/csaf/assets/xml/test.xml").getAbsolutePath();

  @Test
  void yamlBranchesAndNullGuards() throws Exception {
    new YamlFeat();
    new YamlUtil();

    YamlFeat.setEscapeNotFoundReplacementAlways(null);
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(null);
    assertEquals("direct", YamlFeat.getEscapeNotFoundReplacement("direct"));
    YamlFeat.setEscapeNotFoundReplacementAlways("always");
    assertEquals("always", YamlFeat.getEscapeNotFoundReplacement(null));
    YamlFeat.setEscapeNotFoundReplacementAlways(null);
    YamlFeat.setEscapeNotFoundReplacement("thread");
    assertEquals("thread", YamlFeat.getEscapeNotFoundReplacement(null));
    assertNull(YamlFeat.getEscapeNotFoundReplacement(null));
    assertEquals("direct", YamlFeat.getEscapeNotFoundReplacementLazy("direct"));
    YamlFeat.setEscapeNotFoundReplacementAlways("always");
    assertEquals("always", YamlFeat.getEscapeNotFoundReplacementLazy(null));
    YamlFeat.setEscapeNotFoundReplacementAlways(null);
    YamlFeat.setEscapeNotFoundReplacement("thread");
    assertEquals("thread", YamlFeat.getEscapeNotFoundReplacementLazy(null));
    assertNull(YamlFeat.getEscapeNotFoundReplacementLazy(null));

    assertTrue(YamlFeat.getEscapeNotFoundThrowException(true));
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(true);
    assertTrue(YamlFeat.getEscapeNotFoundThrowException(null));
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(null);
    YamlFeat.setEscapeNotFoundThrowException(true);
    assertTrue(YamlFeat.getEscapeNotFoundThrowException(null));
    assertFalse(YamlFeat.getEscapeNotFoundThrowException(null));
    assertFalse(YamlFeat.getEscapeNotFoundThrowExceptionLazy(false));
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(true);
    assertTrue(YamlFeat.getEscapeNotFoundThrowExceptionLazy(null));
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(null);
    YamlFeat.setEscapeNotFoundThrowException(true);
    assertTrue(YamlFeat.getEscapeNotFoundThrowExceptionLazy(null));
    assertNull(YamlFeat.getEscapeNotFoundThrowExceptionLazy(null));

    YamlFeatConfig.setEscapeNotFoundReplacement(null).apply();
    String yaml = "a: 1\nb:\n  b1: ${a}\nc: ${missing}\nd: ${missing";
    Map<String, Object> map = YamlUtil.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), true);
    assertEquals("1", ((Map<?, ?>) map.get("b")).get("b1"));
    assertEquals("${missing}", map.get("c"));
    assertEquals("${missing", map.get("d"));
    Map<String, Object> replacementMap = YamlUtil.load(new StringReader("a: ${missing}\n"), false);
    Method escape = YamlUtil.class.getDeclaredMethod("escape", Map.class, Map.class, boolean.class, Object.class, Boolean.class);
    escape.setAccessible(true);
    assertEquals("fallback", ((Map<?, ?>) escape.invoke(null, replacementMap, null, false, "fallback", false)).get("a"));
    assertThrows(Exception.class, () -> escape.invoke(null, YamlUtil.load(new StringReader("a: ${missing}\n"), false), null, false, null, true));
    assertEquals("b", YamlUtil.load(new StringReader("a: b\n"), false).get("a"));
    assertEquals("b", YamlUtil.get(new StringReader("a: b\n"), "a", false));
    assertNull(YamlUtil.get(Collections.singletonMap("a", "b"), "a.b.c"));
    assertNull(YamlUtil.get(Collections.singletonMap("a", "b"), "."));
    assertNull(YamlUtil.get(Collections.singletonMap("a", Collections.singletonMap("b", "c")), "a.b.c"));
    assertThrows(Exception.class, () -> escape.invoke(null, null, null, false, null, false));
    File unreadableYaml = Files.createTempFile("zutil-yaml", ".yml").toFile();
    Files.write(unreadableYaml.toPath(), Collections.singletonList("a: b"));
    unreadableYaml.deleteOnExit();
    boolean readable = unreadableYaml.setReadable(false, false);
    try {
      if (readable) {
        assertEquals(Collections.emptyMap(), YamlUtil.load(unreadableYaml, true));
        assertEquals(Collections.emptyMap(), YamlUtil.load(unreadableYaml.getAbsolutePath(), true));
      }
    } finally {
      unreadableYaml.setReadable(true, false);
    }
    assertEquals(Collections.emptyMap(), YamlUtil.load(new File(YML_FILE.getParentFile(), "not-exists.yml"), true));
    assertEquals(Collections.emptyMap(), YamlUtil.load(new File(YML_FILE.getParentFile(), "not-exists.yml").getAbsolutePath(), true));

    YamlFeat.setEscapeNotFoundReplacement("thread");
    assertEquals("thread", YamlFeat.getEscapeNotFoundReplacement());
    YamlFeat.setEscapeNotFoundThrowException(true);
    assertTrue(YamlFeat.getEscapeNotFoundThrowException());

    for (Method method : YamlUtil.class.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        assertYamlUtilNullGuards(method);
      }
    }

    YamlFeat.setEscapeNotFoundReplacementAlways(null);
    YamlFeat.setEscapeNotFoundThrowExceptionAlways(null);
  }

  @Test
  void xmlBranchesAndNullGuards() throws Exception {
    new top.csaf.xml.XmlUtil();
    String xml = "<root><name> A </name><age>1</age></root>";
    Document doc = DocumentHelper.parseText(xml);
    assertEquals("\" A \"", top.csaf.xml.XmlUtil.toJson(DocumentHelper.parseText("<name> A </name>").getRootElement(), false, JSONWriter.Feature.WriteMapNullValue));

    Method commonRead = top.csaf.xml.XmlUtil.class.getDeclaredMethod("commonRead", SAXReader.class, Object.class, Object.class);
    commonRead.setAccessible(true);
    assertThrows(Exception.class, () -> commonRead.invoke(null, null, 1, null));
    assertNotNull(commonRead.invoke(null, new SAXReader(), new StringReader(xml), null));
    assertNotNull(commonRead.invoke(null, null, new StringReader(xml), "reader-system-id"));
    assertNotNull(commonRead.invoke(null, null, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), "stream-system-id"));
    assertNull(top.csaf.xml.XmlUtil.parse("<root>"));
    Method toJsonRecursion = top.csaf.xml.XmlUtil.class.getDeclaredMethod("toJsonRecursion", Element.class, boolean.class);
    toJsonRecursion.setAccessible(true);
    assertThrows(Exception.class, () -> toJsonRecursion.invoke(null, null, true));

    File dir = Files.createTempDirectory("zutil-xml-dir").toFile();
    dir.deleteOnExit();
    top.csaf.xml.XmlUtil.toFile(doc, OutputFormat.createPrettyPrint(), dir);
    top.csaf.xml.XmlUtil.toFile(doc, OutputFormat.createPrettyPrint(), dir.getAbsolutePath());
    top.csaf.xml.XmlUtil.toFile(doc, dir);
    top.csaf.xml.XmlUtil.toFile(doc, dir.getAbsolutePath());

    for (Method method : top.csaf.xml.XmlUtil.class.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        assertXmlNullGuards(method);
      }
    }
  }

  @Test
  void treeBranches() {
    new TreeUtil();
    assertThrows(IllegalArgumentException.class, () -> TreeUtil.build(Collections.emptyList(), null));
    assertThrows(IllegalArgumentException.class, () -> TreeUtil.build(Collections.emptyList(), TreeConfig.builder().rootParentIdValues(new Object[]{}).build()));

    TreeNode parent = new TreeNode("1", "parent", 1, "0");
    TreeNode child = new TreeNode("2", "child", 2, parent);
    assertEquals("1", child.getParentId());
    child.setChildren(Collections.singletonList(new TreeNode("3", "leaf", 3, "2")));
    assertEquals("2", child.get("id"));
    assertEquals("child", child.get("name"));
    assertEquals(2, child.get("order"));
    assertEquals("1", child.get("parentId"));
    assertNotNull(child.get("children"));
    child.put("extra", "value");
    assertEquals("value", child.get("extra"));

    List<TreeNode> nodes = Arrays.asList(parent, child);
    TreeConfig config = TreeConfig.builder().isIgnoreIdTypeMismatch(false).build();
    assertEquals(1, TreeUtil.build(nodes, config).get(0).getChildren().size());

    assertEquals(0, TreeUtil.flatten(null, "children", ArrayList::new).size());
    assertEquals(3, TreeUtil.flatten(nodes.iterator(), "children", ArrayList::new).size());
    assertEquals(1, TreeUtil.flatten(new TreeNode("single", "single", 1, "0"), "children", ArrayList::new).size());
    assertEquals(1, TreeUtil.flatten(Collections.singletonList("x"), "children", ArrayList::new).size());
    assertNull(TreeUtil.flatten((TreeNode[]) null, "children"));
    assertEquals(0, TreeUtil.flatten(new TreeNode[0], "children").length);

    ArrayNode arrayChild = new ArrayNode();
    ArrayNode arrayParent = new ArrayNode(arrayChild);
    assertEquals(2, TreeUtil.flatten(new ArrayNode[]{arrayParent}, "children").length);
    SingleNode singleChild = new SingleNode();
    SingleNode singleParent = new SingleNode(singleChild);
    IteratorNode iteratorChild = new IteratorNode();
    IteratorNode iteratorParent = new IteratorNode(Collections.singletonList(iteratorChild).iterator());
    assertEquals(2, TreeUtil.flatten(new IteratorNode[]{iteratorParent}, "children").length);
    assertEquals(1, TreeUtil.flatten(new String[]{"x"}, "children").length);

    assertThrows(Exception.class, () -> TreeUtil.build(Collections.singletonList(new TreeNode(null, "blank", 1, "0"))));
    assertThrows(Exception.class, () -> new TreeNode("1", "orphan", 1, (TreeNode) null));
    assertThrows(IllegalArgumentException.class, () -> TreeUtil.build(Collections.singletonList(new TreeNode("1", "node", 1, "0")), TreeConfig.builder().isSort(true).build()));

    List<TreeNode> sortNodes = Arrays.asList(new TreeNode("2", "child", 2, "1"), new TreeNode("1", "parent", 1, "0"));
    TreeConfig fullConfig = TreeConfig.builder()
      .isSort(true)
      .comparator((left, right) -> Integer.compare((Integer) left.getOrder(), (Integer) right.getOrder()))
      .isGenLevel(true)
      .isGenAncestors(true)
      .isGenHasChildren(true)
      .build();
    TreeNode sortedParent = TreeUtil.build(sortNodes, fullConfig).get(0);
    assertEquals(1, sortedParent.get("level"));
    assertEquals("1", sortedParent.get("ancestors"));
    assertEquals(true, sortedParent.get("hasChildren"));
    assertEquals("1,2", sortedParent.getChildren().get(0).get("ancestors"));

    assertEquals(1, TreeUtil.build(Collections.singletonList(new TreeNode("1", "orphan", 1, "missing")), TreeConfig.builder().isRootByNullParent(true).build()).size());
    assertEquals(0, TreeUtil.build(Collections.singletonList(new TreeNode("1", "orphan", 1, "missing"))).size());
  }

  @Test
  void idCardBranchesAndNullGuards() throws Exception {
    new IdCardUtil();
    String female18 = "110101194910011580";
    String male15 = "110101491001599";
    assertEquals(2, IdCardUtil.getGender(female18, false));
    assertFalse(IdCardUtil.isMale(female18, false));
    assertTrue(IdCardUtil.isFemale(female18, false));
    assertEquals("1949-10-01", IdCardUtil.getBirthday(female18, String.class, false));
    assertNotNull(IdCardUtil.getBirthday(female18, java.util.Date.class, false));
    assertNotNull(IdCardUtil.getBirthday(female18, Long.class, false));
    assertNotNull(IdCardUtil.getBirthday(female18, long.class, false));
    assertEquals(LocalDateTime.of(1949, 10, 1, 0, 0), IdCardUtil.getBirthday(female18, LocalDateTime.class, false));
    assertEquals("1949-10-01", IdCardUtil.getBirthday(male15, String.class, false));
    assertEquals(9, IdCardUtil.getGenderCode(male15, false));
    assertNull(IdCardUtil.getGenderCode("1", false));
    assertEquals(-1, IdCardUtil.getGender("1", false));
    assertNull(IdCardUtil.isMale("1", false));
    assertNull(IdCardUtil.isFemale("1", false));
    assertNull(IdCardUtil.getCheckCode(male15, false));
    assertEquals("null-null-null", IdCardUtil.getBirthday("1", String.class, false));
    assertFalse(IdCardUtil.validate("01010119491001159X"));
    assertFalse(IdCardUtil.validate("110101194910011500"));
    assertFalse(IdCardUtil.validate("1101011949100115A0"));
    assertFalse(IdCardUtil.validate("110101194910011/00"));
    assertFalse(IdCardUtil.validate("110101491301599"));
    assertFalse(IdCardUtil.validate("11010149100159A"));
    assertFalse(IdCardUtil.validate("11010149100159/"));
    assertNull(IdCardUtil.getBirthday("0", LocalDate.class, true));
    assertThrows(NullPointerException.class, () -> IdCardUtil.getBirthday("11010119491001159X", null, true));

    for (Method method : IdCardUtil.class.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        assertIdCardNullGuards(method);
      }
    }
  }

  @Test
  void beanAndSimpleUtilityBranches() throws Exception {
    new BeanUtil();
    new ConvertUtil();
    new UnicodeUtil();
    new ThreadLocalUtil();
    assertEquals(1, ConvertUtil.convert("1", Integer.class));

    BeanUtilTest.TestBean iterableBean = new BeanUtilTest.TestBean();
    iterableBean.setName("iterable");
    iterableBean.setDeepObject(Arrays.asList("a", "b"));
    assertTrue(BeanUtil.toMap(iterableBean).get("deepObject") instanceof java.util.Iterator);

    BeanUtilTest.TestBean iteratorBean = new BeanUtilTest.TestBean();
    iteratorBean.setName("iterator");
    iteratorBean.setDeepObject(Arrays.asList("a", "b").iterator());
    assertTrue(BeanUtil.toMap(iteratorBean).get("deepObject") instanceof java.util.Iterator);

    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("null", null);
    mapValue.put("string", "x");
    mapValue.put("boolean", true);
    mapValue.put("bean", new BeanUtilTest.TestBean("map-bean"));
    mapValue.put("map", Collections.singletonMap("n", "v"));
    mapValue.put("iterable", Collections.singletonList(new BeanUtilTest.TestBean("map-list")));
    mapValue.put("iterator", Collections.singletonList(new BeanUtilTest.TestBean("map-iterator")).iterator());
    mapValue.put("array", new BeanUtilTest.TestBean[]{new BeanUtilTest.TestBean("map-array")});
    mapValue.put("object", new Object());
    assertNotNull(BeanUtil.toMap(Collections.singletonMap("nested", mapValue)).get("nested"));

    BeanUtilTest.TestBean arrayBean = new BeanUtilTest.TestBean();
    arrayBean.setDeepObject(new BeanUtilTest.TestBean[]{new BeanUtilTest.TestBean("array-bean")});
    assertNotNull(BeanUtil.toMap(arrayBean).get("deepObject"));
    BeanUtilTest.TestBean primitiveArrayBean = new BeanUtilTest.TestBean();
    primitiveArrayBean.setDeepObject(new int[]{1, 2});
    assertNotNull(BeanUtil.toMap(primitiveArrayBean).get("deepObject"));
    BeanUtilTest.TestBean objectArrayBean = new BeanUtilTest.TestBean();
    objectArrayBean.setDeepObject(new Object[]{null, "plain", 1});
    assertThrows(IllegalArgumentException.class, () -> BeanUtil.toMap(objectArrayBean));
    assertThrows(IllegalArgumentException.class, () -> {
      BeanUtilTest.TestBean mixedArrayBean = new BeanUtilTest.TestBean();
      mixedArrayBean.setDeepObject(new Object[]{new BeanUtilTest.TestBean("array-bean"), "plain"});
      BeanUtil.toMap(mixedArrayBean);
    });
    BeanUtilTest.TestBean nullArrayBean = new BeanUtilTest.TestBean();
    nullArrayBean.setDeepObject(new Map[]{null, Collections.singletonMap("a", "b")});
    assertNotNull(BeanUtil.toMap(nullArrayBean).get("deepObject"));

    BeanUtilTest.TestBean deepIterableBean = new BeanUtilTest.TestBean();
    deepIterableBean.setDeepObject(new TestIterable(Arrays.asList(null, "x", new Object(), new BeanUtilTest.TestBean("iter-bean"), Collections.singletonMap("a", "b"), new TestIterable(Collections.singletonList("nested")), Collections.singletonList("it").iterator(), new BeanUtilTest.TestBean[]{new BeanUtilTest.TestBean("iter-array")})));
    assertTrue(BeanUtil.toMap(deepIterableBean).get("deepObject") instanceof java.util.Iterator);

    BeanUtilTest.TestBean deepIteratorBean = new BeanUtilTest.TestBean();
    deepIteratorBean.setDeepObject(new TestIterator(Arrays.asList(null, "x", new Object(), new BeanUtilTest.TestBean("iterator-bean"), Collections.singletonMap("a", "b"), new TestIterable(Collections.singletonList("nested")), Collections.singletonList("it").iterator(), new BeanUtilTest.TestBean[]{new BeanUtilTest.TestBean("iterator-array")})));
    assertTrue(BeanUtil.toMap(deepIteratorBean).get("deepObject") instanceof java.util.Iterator);

    assertThrows(NullPointerException.class, () -> BeanUtil.hasProperty(null, "name"));
    assertThrows(NullPointerException.class, () -> BeanUtil.hasProperty(new BeanUtilTest.TestBean(), null));
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties((Object) null, BeanUtilTest.TestBean.class));
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties(new BeanUtilTest.TestBean(), (Class<BeanUtilTest.TestBean>) null));
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties((List<?>) null, BeanUtilTest.TestBean.class));
    assertThrows(NullPointerException.class, () -> BeanUtil.copyProperties(Collections.singletonList(new BeanUtilTest.TestBean()), null));

    PropFunc<BeanUtilTest.TestBean, Object> brokenFn = new PropFunc<BeanUtilTest.TestBean, Object>() {
      @Override
      public Object apply(BeanUtilTest.TestBean bean) {
        return bean.getName();
      }
    };
    assertThrows(RuntimeException.class, () -> BeanUtil.getPropertyName(brokenFn));
    assertThrows(RuntimeException.class, () -> BeanUtil.getPropertyClass(brokenFn));

    ThrowingReadBean throwingReadBean = new ThrowingReadBean();
    assertThrows(RuntimeException.class, () -> BeanUtil.getProperty(throwingReadBean, "name"));
    ThrowingWriteBean throwingWriteBean = new ThrowingWriteBean();
    assertFalse(BeanUtil.setProperty(throwingWriteBean, "name", "x"));

    Method to = Md5Util.class.getDeclaredMethod("to", Object.class, boolean.class);
    to.setAccessible(true);
    assertThrows(Exception.class, () -> to.invoke(null, 1, true));
    assertEquals("", Md5Util.toLowerCase(""));
    assertEquals("", Md5Util.toUpperCase((byte[]) null));
    assertEquals("900150983CD24FB0D6963F7D28E17F72", Md5Util.toUpperCase("abc"));
    assertEquals("900150983cd24fb0d6963f7d28e17f72", Md5Util.toLowerCase("abc"));
    assertEquals("3CD24FB0D6963F7D", Md5Util.toUpperCaseShort("abc"));
    assertEquals("3cd24fb0d6963f7d", Md5Util.toLowerCaseShort("abc"));
    assertEquals("900150983cd24fb0d6963f7d28e17f72", Md5Util.toLowerCase("abc".getBytes(StandardCharsets.UTF_8)));
    assertEquals("3cd24fb0d6963f7d", Md5Util.toLowerCaseShort("abc".getBytes(StandardCharsets.UTF_8)));
    assertEquals("\\u0041\\u4f60", UnicodeUtil.toUnicode("A你"));
    assertEquals("00414f60", UnicodeUtil.toHex("A你"));
    assertEquals("A你", UnicodeUtil.toString("A\\u4f60"));
    assertEquals("\\uZZZZ", UnicodeUtil.toString("\\uZZZZ"));
    assertEquals("A你", UnicodeUtil.fromHex("00414f60"));
    assertThrows(IllegalArgumentException.class, () -> UnicodeUtil.fromHex("123"));
    assertThrows(IllegalArgumentException.class, () -> UnicodeUtil.fromHex("ZZZZ"));

    ThreadLocalUtil.clear();
    assertTrue(ThreadLocalUtil.getAll().isEmpty());
    ThreadLocalUtil.set("a", "b");
    assertEquals("b", ThreadLocalUtil.get("a"));
    assertEquals("b", ThreadLocalUtil.getAll().get("a"));
    ThreadLocalUtil.remove("a");
    assertNull(ThreadLocalUtil.get("a"));
    assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5Util.toLowerCase(new byte[0]));
  }

  @Test
  void retryBranches() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> RetryUtil.config(0, 1));
    assertThrows(IllegalArgumentException.class, () -> RetryUtil.config(1, -1));
    assertThrows(IllegalArgumentException.class, () -> RetryUtil.config(0, io.github.resilience4j.core.IntervalFunction.ofDefaults(), null, null));
    assertThrows(NullPointerException.class, () -> RetryUtil.config(1, null, null, null));
    assertNotNull(RetryUtil.config(2, 0));
    assertNotNull(RetryUtil.config(2, io.github.resilience4j.core.IntervalFunction.ofDefaults(), throwable -> true, String.class, null));
    assertNotNull(RetryUtil.config(2, io.github.resilience4j.core.IntervalFunction.ofDefaults(), throwable -> false, String.class, value -> value.startsWith("retry")));
    assertThrows(NullPointerException.class, () -> RetryUtil.config(2, io.github.resilience4j.core.IntervalFunction.ofDefaults(), null, null, value -> true));

    io.github.resilience4j.retry.Retry retry = RetryUtil.retry(" ", RetryUtil.config(2, 0));
    assertNotNull(RetryUtil.retry(null, null));
    AtomicInteger calls = new AtomicInteger();
    assertEquals("ok", RetryUtil.execute(retry, () -> calls.incrementAndGet() == 1 ? "ok" : "no"));
    assertEquals(1, calls.get());
    assertEquals("ok", RetryUtil.executeSupplier(retry, () -> "ok"));
    AtomicInteger runCalls = new AtomicInteger();
    RetryUtil.run(retry, runCalls::incrementAndGet);
    assertEquals(1, runCalls.get());
    assertThrows(NullPointerException.class, () -> RetryUtil.execute(null, (Callable<String>) () -> "ok"));
    assertThrows(NullPointerException.class, () -> RetryUtil.execute(retry, null));
    assertThrows(NullPointerException.class, () -> RetryUtil.executeSupplier(null, () -> "ok"));
    assertThrows(NullPointerException.class, () -> RetryUtil.run(null, () -> {}));

    RuntimeException runtimeException = new RuntimeException("boom");
    assertThrows(RuntimeException.class, () -> RetryUtil.execute(retry, () -> {
      throw runtimeException;
    }));
    RuntimeException interrupted = assertThrows(RuntimeException.class, () -> RetryUtil.execute(retry, () -> {
      throw new InterruptedException("stop");
    }));
    assertTrue(interrupted.getCause() instanceof InterruptedException);
    assertTrue(Thread.interrupted());

    Method testResultPredicate = RetryUtil.class.getDeclaredMethod("testResultPredicate", Predicate.class, Object.class);
    testResultPredicate.setAccessible(true);
    Predicate<String> stringPredicate = value -> value.length() > 1;
    assertFalse((Boolean) testResultPredicate.invoke(null, stringPredicate, 1));
    assertTrue((Boolean) testResultPredicate.invoke(null, stringPredicate, "ok"));
  }
  @Test
  void cryptoBranchesAndEnums() throws Exception {
    new AesUtil();
    new DesUtil();
    new Sm4Util();
    new BlockCipherUtil();
    new DigestUtil();
    new Md5Util();
    assertEquals("900150983cd24fb0d6963f7d28e17f72", DigestUtil.md5Hex("abc"));

    for (BlockCipherType type : BlockCipherType.values()) {
      String value = type.getValue();
      type.setValue(value + "-test");
      assertEquals(value + "-test", type.getValue());
      type.setValue(value);
    }
    for (Mode mode : Mode.values()) {
      String value = mode.getValue();
      mode.setValue(value + "-test");
      assertEquals(value + "-test", mode.getValue());
      mode.setValue(value);
    }
    for (Padding padding : Padding.values()) {
      String value = padding.getValue();
      padding.setValue(value + "-test");
      assertEquals(value + "-test", padding.getValue());
      padding.setValue(value);
    }

    assertThrows(NullPointerException.class, () -> BlockCipher.builder(null));
    BlockCipher aes = new BlockCipher(BlockCipherType.AES, 16, 16, EncodingType.UTF_8, EncodingType.UTF_8);
    assertEquals(16, new BlockCipher(BlockCipherType.AES, null, null, null, null).getKeyLength());
    assertEquals(16, new BlockCipher(BlockCipherType.SM4, null, null, null, null).getIvLength());
    Method builder = BlockCipher.class.getDeclaredMethod("builder");
    builder.setAccessible(true);
    Object builderInstance = builder.invoke(null);
    Method typeMethod = builderInstance.getClass().getDeclaredMethod("type", BlockCipherType.class);
    typeMethod.setAccessible(true);
    assertNotNull(typeMethod.invoke(builderInstance, BlockCipherType.AES));
    Method decodeAndPad = BlockCipher.class.getDeclaredMethod("decodeAndPad", Object.class, EncodingType.class, int.class);
    decodeAndPad.setAccessible(true);
    assertArrayEquals("abcd".getBytes(), (byte[]) decodeAndPad.invoke(aes, "abcd", null, 3));
    assertArrayEquals("abcd".getBytes(), (byte[]) decodeAndPad.invoke(aes, "61626364", EncodingType.HEX, 3));
    assertBrokenBlockCipherType(BlockCipherType.AES, () -> assertThrows(IllegalArgumentException.class, () -> new BlockCipher(BlockCipherType.AES, null, 16, null, null)));
    assertBrokenBlockCipherType(BlockCipherType.AES, () -> assertThrows(IllegalArgumentException.class, () -> new BlockCipher(BlockCipherType.AES, 16, null, null, null)));
    Method encode = BlockCipher.class.getDeclaredMethod("encode", byte[].class, EncodingType.class);
    encode.setAccessible(true);
    Method decode = BlockCipher.class.getDeclaredMethod("decode", String.class, EncodingType.class);
    decode.setAccessible(true);
    assertUnsupportedEncoding(aes, encode, decode);

    Method encrypt = BlockCipher.class.getDeclaredMethod("encrypt", Object.class, Object.class, Object.class, Mode.class, Padding.class, EncodingType.class);
    encrypt.setAccessible(true);
    Method check = BlockCipher.class.getDeclaredMethod("check", Object.class, Padding.class);
    check.setAccessible(true);
    assertThrows(Exception.class, () -> check.invoke(aes, null, Padding.PKCS7));
    assertThrows(Exception.class, () -> check.invoke(aes, "123", null));
    Method decrypt = BlockCipher.class.getDeclaredMethod("decrypt", Object.class, Object.class, Object.class, Mode.class, Padding.class, EncodingType.class);
    decrypt.setAccessible(true);
    String noPaddingCipher = (String) encrypt.invoke(aes, "1234567890123456".getBytes(), "1234567890123456", null, Mode.ECB, Padding.NO, EncodingType.BASE_64);
    assertEquals("1234567890123456", decrypt.invoke(aes, java.util.Base64.getDecoder().decode(noPaddingCipher), "1234567890123456", null, Mode.ECB, Padding.NO, EncodingType.BASE_64));
    assertThrows(Exception.class, () -> encrypt.invoke(aes, "short", "1234567890123456", null, Mode.ECB, Padding.NO, EncodingType.BASE_64));
    BlockCipher shortKeyAes = new BlockCipher(BlockCipherType.AES, 1, 16, EncodingType.UTF_8, EncodingType.UTF_8);
    assertEquals("", encryptOrDecrypt(shortKeyAes, "1234567890123456".getBytes(), new byte[]{1}, "1234567890123456", Mode.CBC, Padding.NO, Cipher.ENCRYPT_MODE));

    Method encryptOrDecrypt = BlockCipher.class.getDeclaredMethod("encryptOrDecrypt", byte[].class, Object.class, Object.class, Mode.class, Padding.class, int.class);
    encryptOrDecrypt.setAccessible(true);
    assertBlockCipherNullGuards(aes, encrypt, decrypt, encryptOrDecrypt, noPaddingCipher);
  }

  private void assertBrokenBlockCipherType(BlockCipherType type, ThrowingRunnable runnable) throws Exception {
    Class<?> switchMapClass = Class.forName("top.csaf.crypto.BlockCipher$1");
    Field switchMapField = switchMapClass.getDeclaredField("$SwitchMap$top$csaf$crypto$enums$BlockCipherType");
    switchMapField.setAccessible(true);
    int[] switchMap = (int[]) switchMapField.get(null);
    int originalSwitchValue = switchMap[type.ordinal()];
    try {
      switchMap[type.ordinal()] = 0;
      runnable.run();
    } finally {
      switchMap[type.ordinal()] = originalSwitchValue;
    }
  }

  private String encryptOrDecrypt(BlockCipher blockCipher, byte[] data, Object key, Object iv, Mode mode, Padding padding, int cipherMode) throws Exception {
    Method encryptOrDecrypt = BlockCipher.class.getDeclaredMethod("encryptOrDecrypt", byte[].class, Object.class, Object.class, Mode.class, Padding.class, int.class);
    encryptOrDecrypt.setAccessible(true);
    return new String((byte[]) encryptOrDecrypt.invoke(blockCipher, data, key, iv, mode, padding, cipherMode));
  }

  private void assertUnsupportedEncoding(BlockCipher aes, Method encode, Method decode) throws Exception {
    Class<?> switchMapClass = Class.forName("top.csaf.crypto.BlockCipher$1");
    Field switchMapField = switchMapClass.getDeclaredField("$SwitchMap$top$csaf$crypto$enums$EncodingType");
    switchMapField.setAccessible(true);
    int[] switchMap = (int[]) switchMapField.get(null);
    int originalSwitchValue = switchMap[EncodingType.UTF_8.ordinal()];
    try {
      switchMap[EncodingType.UTF_8.ordinal()] = 0;
      assertThrows(Exception.class, () -> encode.invoke(aes, "abc".getBytes(), EncodingType.UTF_8));
      assertThrows(Exception.class, () -> decode.invoke(aes, "abc", EncodingType.UTF_8));
    } finally {
      switchMap[EncodingType.UTF_8.ordinal()] = originalSwitchValue;
    }
  }

  private void assertYamlUtilNullGuards(Method method) {
    Class<?>[] types = method.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      if (types[i].equals(boolean.class)) {
        continue;
      }
      Object[] args = yamlArgs(method, types);
      args[i] = null;
      assertThrows(Exception.class, () -> method.invoke(null, args));
    }
  }

  private Object[] yamlArgs(Method method, Class<?>[] types) {
    Object[] args = new Object[types.length];
    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];
      if (Map.class.isAssignableFrom(type)) {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "b");
        args[i] = map;
      } else if (type.equals(String.class)) {
        args[i] = method.getName().equals("load") || i == 0 && types.length > 1 ? YML_FILE_PATH : "a";
      } else if (type.equals(boolean.class)) {
        args[i] = false;
      } else if (type.equals(File.class)) {
        args[i] = YML_FILE;
      } else if (Reader.class.isAssignableFrom(type)) {
        try {
          args[i] = new FileReader(YML_FILE);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else if (InputStream.class.isAssignableFrom(type)) {
        try {
          args[i] = Files.newInputStream(YML_FILE.toPath());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    return args;
  }

  private void assertXmlNullGuards(Method method) {
    Class<?>[] types = method.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      if (types[i].equals(SAXReader.class) || types[i].equals(boolean.class) || types[i].equals(Class.class) || types[i].isArray()) {
        continue;
      }
      Object[] args = xmlArgs(method, types);
      args[i] = null;
      assertThrows(Exception.class, () -> method.invoke(null, args));
    }
  }

  private Object[] xmlArgs(Method method, Class<?>[] types) {
    String xml = "<root><name>A</name></root>";
    Object[] args = new Object[types.length];
    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];
      try {
        if (type.equals(SAXReader.class)) {
          args[i] = new SAXReader();
        } else if (type.equals(java.net.URL.class)) {
          args[i] = new File(XML_FILE_PATH).toURI().toURL();
        } else if (type.equals(File.class)) {
          args[i] = new File(XML_FILE_PATH);
        } else if (Reader.class.isAssignableFrom(type)) {
          args[i] = new StringReader(xml);
        } else if (type.equals(InputSource.class)) {
          args[i] = new InputSource(new StringReader(xml));
        } else if (InputStream.class.isAssignableFrom(type)) {
          args[i] = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        } else if (type.equals(String.class)) {
          args[i] = method.getName().equals("parse") ? xml : XML_FILE_PATH;
        } else if (type.equals(Document.class)) {
          args[i] = DocumentHelper.parseText(xml);
        } else if (type.equals(OutputFormat.class)) {
          args[i] = OutputFormat.createPrettyPrint();
        } else if (type.equals(Element.class)) {
          args[i] = DocumentHelper.parseText(xml).getRootElement();
        } else if (type.equals(boolean.class)) {
          args[i] = true;
        } else if (type.equals(Class.class)) {
          args[i] = TestBean.class;
        } else if (type.isArray()) {
          args[i] = java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return args;
  }

  private void assertIdCardNullGuards(Method method) {
    Class<?>[] types = method.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      if (types[i].equals(boolean.class)) {
        continue;
      }
      Object[] args = new Object[types.length];
      for (int j = 0; j < types.length; j++) {
        if (types[j].equals(String.class)) {
          args[j] = "11010119491001159X";
        } else if (types[j].equals(Class.class)) {
          args[j] = LocalDate.class;
        } else if (types[j].equals(boolean.class)) {
          args[j] = false;
        }
      }
      args[i] = null;
      assertThrows(Exception.class, () -> method.invoke(null, args));
    }
  }

  private void assertBlockCipherNullGuards(BlockCipher aes, Method encrypt, Method decrypt, Method encryptOrDecrypt, String ciphertext) {
    Object[][] encryptArgs = {
      {null, "1234567890123456", null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64},
      {"hello", null, null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64},
      {"hello", "1234567890123456", null, null, Padding.PKCS7, EncodingType.BASE_64},
      {"hello", "1234567890123456", null, Mode.ECB, null, EncodingType.BASE_64}
    };
    for (Object[] args : encryptArgs) {
      assertThrows(Exception.class, () -> encrypt.invoke(aes, args));
    }

    Object[][] decryptArgs = {
      {null, "1234567890123456", null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64},
      {ciphertext, null, null, Mode.ECB, Padding.PKCS7, EncodingType.BASE_64},
      {ciphertext, "1234567890123456", null, null, Padding.PKCS7, EncodingType.BASE_64},
      {ciphertext, "1234567890123456", null, Mode.ECB, null, EncodingType.BASE_64}
    };
    for (Object[] args : decryptArgs) {
      assertThrows(Exception.class, () -> decrypt.invoke(aes, args));
    }

    Object[][] encryptOrDecryptArgs = {
      {null, "1234567890123456", null, Mode.ECB, Padding.PKCS7, Cipher.ENCRYPT_MODE},
      {"hello".getBytes(), null, null, Mode.ECB, Padding.PKCS7, Cipher.ENCRYPT_MODE},
      {"hello".getBytes(), "1234567890123456", null, null, Padding.PKCS7, Cipher.ENCRYPT_MODE},
      {"hello".getBytes(), "1234567890123456", null, Mode.ECB, null, Cipher.ENCRYPT_MODE}
    };
    for (Object[] args : encryptOrDecryptArgs) {
      assertThrows(Exception.class, () -> encryptOrDecrypt.invoke(aes, args));
    }
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  static class TestBean {
    private String name;
    private Integer age;
  }

  static class ArrayNode {
    ArrayNode[] children;

    ArrayNode(ArrayNode... children) {
      this.children = children;
    }
  }

  static class SingleNode {
    SingleNode children;

    SingleNode() {
    }

    SingleNode(SingleNode children) {
      this.children = children;
    }
  }

  static class ListNode {
    List<ListNode> children;

    ListNode() {
    }

    ListNode(List<ListNode> children) {
      this.children = children;
    }
  }

  static class IteratorNode {
    Iterator<IteratorNode> children;

    IteratorNode() {
    }

    IteratorNode(Iterator<IteratorNode> children) {
      this.children = children;
    }
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }

  static class TestIterable implements Iterable<Object> {
    private final List<Object> values;

    TestIterable(List<Object> values) {
      this.values = values;
    }

    @Override
    public Iterator<Object> iterator() {
      return values.iterator();
    }
  }

  static class TestIterator implements Iterator<Object> {
    private final Iterator<Object> iterator;

    TestIterator(List<Object> values) {
      this.iterator = values.iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Object next() {
      return iterator.next();
    }
  }

  static class ThrowingReadBean {
    public String getName() {
      throw new IllegalStateException("read");
    }
  }

  static class ThrowingWriteBean {
    public void setName(String name) {
      throw new IllegalStateException("write");
    }
  }
}
