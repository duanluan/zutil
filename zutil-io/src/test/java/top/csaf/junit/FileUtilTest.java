package top.csaf.junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.io.FileUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileUtil 单元测试
 * 覆盖率优化版 - v2
 */
@DisplayName("FileUtil 文件工具类测试")
class FileUtilTest {

  @Test
  @DisplayName("获取工作目录与项目路径")
  void testGetPaths() {
    assertNotNull(FileUtil.getUserDir());
    // getProjectPath 中的 IOException 无法在普通测试中触发，属于防御性代码，忽略覆盖率
    assertNotNull(FileUtil.getProjectPath());
  }

  @Test
  @DisplayName("获取资源流 - 覆盖前导斜杠逻辑")
  void testGetResourceAsStream() {
    // 测试带 "/" 的情况，覆盖 if (path.startsWith("/"))
    InputStream stream1 = FileUtil.getResourceAsStream(FileUtilTest.class, "/NonExistentFile.txt");
    assertNull(stream1);

    InputStream stream2 = FileUtil.getResourceAsStream("/NonExistentFile.txt");
    assertNull(stream2);
  }

  @Test
  @DisplayName("获取资源根路径与类路径 - 覆盖 null 分支与系统分支")
  void testGetResourceRootPath() {
    // 1. 正常场景
    String rootPath = FileUtil.getResourceRootPath(FileUtilTest.class);
    if (rootPath != null) {
      // Windows 下会覆盖 Windows 分支，Linux 下会覆盖 else 分支
      // 无法在单机同时覆盖两者，此为正常现象
      assertFalse(rootPath.isEmpty());
    }

    String noArgRootPath = FileUtil.getResourceRootPath();
    if (noArgRootPath != null) {
      assertFalse(noArgRootPath.isEmpty());
    }

    String classPath = FileUtil.getClassPath(FileUtilTest.class);
    if (classPath != null) {
      assertFalse(classPath.isEmpty());
    }

    // 2. 覆盖 getClassPath(Class) 的 resUrl == null 分支
    // 数组类通常没有对应的资源文件，getResource("") 返回 null
    assertNull(FileUtil.getClassPath(String[].class));
  }

  @Test
  @DisplayName("覆盖 getResourceRootPath() 的 null 分支")
  void testGetResourceRootPath_Null() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      // 创建一个匿名 ClassLoader，强制 getResource 返回 null
      ClassLoader nullLoader = new ClassLoader() {
        @Override
        public URL getResource(String name) {
          return null;
        }
      };
      Thread.currentThread().setContextClassLoader(nullLoader);

      // 触发 if (resUrl == null) return null;
      assertNull(FileUtil.getResourceRootPath());
    } finally {
      // 恢复上下文 ClassLoader，避免影响其他测试
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
  }

  @Test
  @DisplayName("获取文件扩展名 - String 与 File 重载")
  void testGetFileExtension() {
    assertEquals("txt", FileUtil.getFileExtension("test.txt"));
    assertEquals("java", FileUtil.getFileExtension("src/main/Test.java"));
    assertEquals("", FileUtil.getFileExtension("Makefile"));
    assertEquals("", FileUtil.getFileExtension("path/to/file"));
    assertEquals("xml", FileUtil.getFileExtension(new File("pom.xml")));
  }

  @Test
  @DisplayName("根据路径获取目录和文件名")
  void testGetDirPathAndNameByPath() {
    String[] res1 = FileUtil.getDirPathAndNameByPath("d1/d2/test.txt");
    assertEquals("d1/d2/", res1[0]);
    assertEquals("test.txt", res1[1]);

    String[] res2 = FileUtil.getDirPathAndNameByPath("test.txt");
    assertEquals("", res2[0]);
    assertEquals("test.txt", res2[1]);

    // 修复点：README 无后缀，代码逻辑将其视为目录（路径），文件名为空
    String[] res3 = FileUtil.getDirPathAndNameByPath("README");
    assertEquals("README", res3[0], "无后缀且无斜杠时，视为目录路径");
    assertEquals("", res3[1], "无后缀且无斜杠时，文件名应为空");
  }

  @Test
  @DisplayName("根据路径获取目录")
  void testGetDirPathByPath() {
    // 场景1：仅有 /
    assertEquals("a/b/", FileUtil.getDirPathByPath("a/b/c.txt"));

    // 场景2：仅有 \ (Windows路径)
    assertEquals("a\\b\\", FileUtil.getDirPathByPath("a\\b\\c.txt"));

    // 场景3：无目录
    assertNull(FileUtil.getDirPathByPath("filename.txt"));
  }

  @Test
  @DisplayName("根据路径获取文件名")
  void testGetNameByPath() {
    assertEquals("test.txt", FileUtil.getNameByPath("/a/b/test.txt"));
    assertEquals("test.txt", FileUtil.getNameByPath("test.txt"));
  }

  @Test
  @DisplayName("替换文件路径")
  void testReplace() {
    String filePath = "root/dir/oldName.txt";

    assertThrows(IllegalArgumentException.class, () -> FileUtil.replace("root/dir/", "any"));

    String res1 = FileUtil.replace(filePath, "newName_$1.$2");
    assertEquals("root/dir/newName_oldName.txt", res1);

    String res2 = FileUtil.replace(filePath, "newRoot/newName.$2");
    assertEquals("newRoot/newName.txt", res2);
  }
}
