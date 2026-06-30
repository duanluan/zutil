package top.csaf.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.lang.ClassUtil;

@Slf4j
@DisplayName("Class 工具类测试")
class ClassUtilTest {

  private void println(Object source) {
    System.out.println(source);
  }

  @DisplayName("isBasic：是否为基本数据类型")
  @Test
  void isBasic() {
    println(ClassUtil.isPrimitiveType(1, 1.1f, 1.1, true, (byte) 1, (char) 1, (short) 1));
  }
}
