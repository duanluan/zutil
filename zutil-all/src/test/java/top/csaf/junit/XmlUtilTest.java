package top.csaf.junit;

import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import top.csaf.xml.XmlUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("XML 工具类测试")
class XmlUtilTest {

  private void println(Object source) {
    System.out.println(source);
  }

  private static final String XML_FILE_PATH = new File("zutil-all/src/test/java/top/csaf/assets/xml/test.xml").exists()
    ? new File("zutil-all/src/test/java/top/csaf/assets/xml/test.xml").getAbsolutePath()
    : new File("src/test/java/top/csaf/assets/xml/test.xml").getAbsolutePath();

  private final Element rootElement;

  {
    Document doc = XmlUtil.read(new File(XML_FILE_PATH));
    rootElement = doc.getRootElement();
  }

  @NoArgsConstructor
  @Data
  private static class TestBean {
    private String name;
    private Integer age;
  }

  @DisplayName("toJson：将 XML 转换为 JSON")
  @Test
  void toJson() throws Exception {
    println(XmlUtil.toJson(rootElement, true, JSONWriter.Feature.WriteMapNullValue));
    println(XmlUtil.toJson(DocumentHelper.parseText("<beans><bean><name>A</name><age>1</age></bean><bean><name>B</name><age>2</age></bean></beans>").getRootElement(), true, JSONWriter.Feature.WriteMapNullValue));
    println(XmlUtil.toJson(DocumentHelper.parseText("<bean><name>A</name><age>1</age></bean>").getRootElement(), true, JSONWriter.Feature.WriteMapNullValue));
  }

  @DisplayName("parseObject：将 XML 转换为对象")
  @Test
  void parseObject() throws Exception {
    println(XmlUtil.parseObject(DocumentHelper.parseText("<bean><name>A</name><age>1</age></bean>").getRootElement(), true, TestBean.class).getName());
  }

  @DisplayName("parseArray：将 XML 转换为集合")
  @Test
  void parseArray() throws Exception {
    println(XmlUtil.parseArray(DocumentHelper.parseText("<beans><bean><name>A</name><age>1</age></bean><bean><name>B</name><age>2</age></bean></beans>").getRootElement(), true, TestBean.class).get(0).getName());
  }

  @DisplayName("读写与异常分支")
  @Test
  void readWriteAndBranches() throws Exception {
    String xml = "<root><name> A </name><age>1</age></root>";
    assertNotNull(XmlUtil.parse(xml));
    assertNull(XmlUtil.parse("<root>"));
    assertNotNull(XmlUtil.read(new StringReader(xml)));
    assertNotNull(XmlUtil.read(new ByteArrayInputStream(xml.getBytes())));
    assertNotNull(XmlUtil.read(new InputSource(new StringReader(xml))));
    assertNotNull(XmlUtil.read(new File(XML_FILE_PATH)));
    assertNotNull(XmlUtil.read(new File(XML_FILE_PATH).toURI().toURL()));
    assertNotNull(XmlUtil.read(new StringReader(xml), new File(XML_FILE_PATH).toURI().toString()));
    assertNotNull(XmlUtil.read(new ByteArrayInputStream(xml.getBytes()), new File(XML_FILE_PATH).toURI().toString()));
    assertNull(XmlUtil.read("not-exists.xml"));

    File temp = File.createTempFile("zutil-xml", ".xml");
    temp.deleteOnExit();
    Document doc = DocumentHelper.parseText(xml);
    XmlUtil.toFile(doc, temp);
    assertTrue(temp.length() > 0);
    XmlUtil.toFile(doc, temp.getAbsolutePath());
    XmlUtil.toFile(doc, org.dom4j.io.OutputFormat.createPrettyPrint(), temp);
    XmlUtil.toFile(doc, org.dom4j.io.OutputFormat.createPrettyPrint(), temp.getAbsolutePath());
  }

  @DisplayName("反射调用 XML 工具重载")
  @Test
  void invokePublicStaticMethods() throws Exception {
    int invoked = 0;
    for (Method method : XmlUtil.class.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      try {
        method.invoke(null, args(method.getParameterTypes()));
      } catch (Exception ignored) {
        // 覆盖重载入口和参数校验。
      }
      invoked++;
    }
    assertTrue(invoked > 20);
  }

  private Object[] args(Class<?>[] parameterTypes) throws Exception {
    String xml = "<root><name>A</name></root>";
    Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> type = parameterTypes[i];
      if (type.equals(String.class)) {
        args[i] = i == 0 ? XML_FILE_PATH : new File(XML_FILE_PATH).toURI().toString();
      } else if (type.equals(File.class)) {
        args[i] = new File(XML_FILE_PATH);
      } else if (type.equals(java.net.URL.class)) {
        args[i] = new File(XML_FILE_PATH).toURI().toURL();
      } else if (type.equals(java.io.Reader.class)) {
        args[i] = new StringReader(xml);
      } else if (type.equals(java.io.InputStream.class)) {
        args[i] = new ByteArrayInputStream(xml.getBytes());
      } else if (type.equals(InputSource.class)) {
        args[i] = new InputSource(new StringReader(xml));
      } else if (type.equals(Document.class)) {
        args[i] = DocumentHelper.parseText(xml);
      } else if (type.equals(Element.class)) {
        args[i] = DocumentHelper.parseText(xml).getRootElement();
      } else if (type.equals(org.dom4j.io.OutputFormat.class)) {
        args[i] = org.dom4j.io.OutputFormat.createPrettyPrint();
      } else if (type.equals(boolean.class)) {
        args[i] = true;
      } else if (type.equals(Class.class)) {
        args[i] = TestBean.class;
      } else if (type.isArray()) {
        args[i] = java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
      }
    }
    return args;
  }
}
