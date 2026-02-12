package top.csaf.junit;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.csaf.io.CompressOptions;
import top.csaf.io.CompressUtil;
import top.csaf.io.CompressUtil.Format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompressUtil 与 CompressOptions 的单元测试。
 */
@DisplayName("CompressUtil tests")
class CompressUtilTest {

  /**
   * JUnit 提供的临时目录，测试过程中的文件都落在这里。
   */
  @TempDir
  Path tempDir;

  /**
   * 校验 CompressOptions 的默认值、Builder 默认值与自定义值。
   */
  @Test
  @DisplayName("CompressOptions defaults and custom values")
  void testCompressOptionsDefaultsAndCustom() {
    // 默认构造器值校验
    CompressOptions defaults = new CompressOptions();
    assertEquals(64 * 1024, defaults.getBufferSize());
    assertEquals(StandardCharsets.UTF_8, defaults.getCharset());
    assertEquals(Deflater.BEST_SPEED, defaults.getCompressionLevel());
    assertTrue(defaults.isOverwrite());
    assertTrue(defaults.isIncludeRootDir());
    assertTrue(defaults.isPreserveLastModified());
    assertFalse(defaults.isFollowLinks());
    assertFalse(defaults.isAllowUnsafePath());
    assertEquals(0, defaults.getMaxEntries());
    assertEquals(0, defaults.getMaxTotalSize());
    assertEquals(0, defaults.getMaxEntrySize());

    // Builder 默认值应与默认构造保持一致
    CompressOptions builderDefaults = CompressOptions.builder().build();
    assertEquals(defaults.getBufferSize(), builderDefaults.getBufferSize());
    assertEquals(defaults.getCharset(), builderDefaults.getCharset());
    assertEquals(defaults.getCompressionLevel(), builderDefaults.getCompressionLevel());
    assertEquals(defaults.isOverwrite(), builderDefaults.isOverwrite());
    assertEquals(defaults.isIncludeRootDir(), builderDefaults.isIncludeRootDir());
    assertEquals(defaults.isPreserveLastModified(), builderDefaults.isPreserveLastModified());
    assertEquals(defaults.isFollowLinks(), builderDefaults.isFollowLinks());
    assertEquals(defaults.isAllowUnsafePath(), builderDefaults.isAllowUnsafePath());

    // 自定义参数应正确覆盖默认值
    CompressOptions custom = CompressOptions.builder()
      .bufferSize(1024)
      .charset(StandardCharsets.ISO_8859_1)
      .compressionLevel(Deflater.BEST_COMPRESSION)
      .overwrite(false)
      .includeRootDir(false)
      .preserveLastModified(false)
      .followLinks(true)
      .allowUnsafePath(true)
      .maxEntries(3)
      .maxTotalSize(10)
      .maxEntrySize(5)
      .build();
    assertEquals(1024, custom.getBufferSize());
    assertEquals(StandardCharsets.ISO_8859_1, custom.getCharset());
    assertEquals(Deflater.BEST_COMPRESSION, custom.getCompressionLevel());
    assertFalse(custom.isOverwrite());
    assertFalse(custom.isIncludeRootDir());
    assertFalse(custom.isPreserveLastModified());
    assertTrue(custom.isFollowLinks());
    assertTrue(custom.isAllowUnsafePath());
    assertEquals(3, custom.getMaxEntries());
    assertEquals(10, custom.getMaxTotalSize());
    assertEquals(5, custom.getMaxEntrySize());

    // 全参构造器覆盖所有字段
    CompressOptions allArgs = new CompressOptions(1, StandardCharsets.US_ASCII, Deflater.NO_COMPRESSION, false, false,
      false, true, true, 1, 2, 3);
    assertEquals(1, allArgs.getBufferSize());
    assertEquals(StandardCharsets.US_ASCII, allArgs.getCharset());
    assertEquals(Deflater.NO_COMPRESSION, allArgs.getCompressionLevel());
    assertFalse(allArgs.isOverwrite());
    assertFalse(allArgs.isIncludeRootDir());
    assertFalse(allArgs.isPreserveLastModified());
    assertTrue(allArgs.isFollowLinks());
    assertTrue(allArgs.isAllowUnsafePath());
    assertEquals(1, allArgs.getMaxEntries());
    assertEquals(2, allArgs.getMaxTotalSize());
    assertEquals(3, allArgs.getMaxEntrySize());
  }

  /**
   * 校验格式识别与单文件输出名解析。
   */
  @Test
  @DisplayName("Detect format and resolve output name")
  void testDetectFormatAndResolveOutputName() throws Exception {
    // 覆盖常见后缀格式识别
    assertEquals(Format.TAR_GZ, CompressUtil.detectFormat(Paths.get("a.tar.gz")));
    assertEquals(Format.TAR_GZ, CompressUtil.detectFormat(Paths.get("a.tgz")));
    assertEquals(Format.TAR_BZ2, CompressUtil.detectFormat(Paths.get("a.tar.bz2")));
    assertEquals(Format.TAR_BZ2, CompressUtil.detectFormat(Paths.get("a.tbz")));
    assertEquals(Format.TAR_BZ2, CompressUtil.detectFormat(Paths.get("a.tbz2")));
    assertEquals(Format.TAR_XZ, CompressUtil.detectFormat(Paths.get("a.tar.xz")));
    assertEquals(Format.TAR_XZ, CompressUtil.detectFormat(Paths.get("a.txz")));
    assertEquals(Format.TAR, CompressUtil.detectFormat(Paths.get("a.tar")));
    assertEquals(Format.ZIP, CompressUtil.detectFormat(Paths.get("a.zip")));
    assertEquals(Format.SEVEN_Z, CompressUtil.detectFormat(Paths.get("a.7z")));
    assertEquals(Format.GZ, CompressUtil.detectFormat(Paths.get("a.gz")));
    assertEquals(Format.BZ2, CompressUtil.detectFormat(Paths.get("a.bz2")));
    assertEquals(Format.XZ, CompressUtil.detectFormat(Paths.get("a.xz")));
    assertNull(CompressUtil.detectFormat(Paths.get("a.unknown")));

    // 单文件输出名解析：正常与兜底场景
    assertEquals("data", invokePrivate("resolveSingleOutputName",
      new Class<?>[]{String.class, Format.class}, "data.gz", Format.GZ));
    assertEquals("data.out", invokePrivate("resolveSingleOutputName",
      new Class<?>[]{String.class, Format.class}, "data", Format.GZ));
    assertEquals("data.out", invokePrivate("resolveSingleOutputName",
      new Class<?>[]{String.class, Format.class}, "data", Format.TAR));
  }

  /**
   * 校验压缩等级映射与缓冲区大小回退逻辑。
   */
  @Test
  @DisplayName("Resolve deflater level and buffer size")
  void testResolveDeflaterAndBufferSize() throws Exception {
    // 默认压缩等级映射到 6
    assertEquals(6, (int) invokePrivate("resolveDeflaterLevel", new Class<?>[]{int.class}, Deflater.DEFAULT_COMPRESSION));
    // 小于 0 的等级被钳制到 NO_COMPRESSION
    assertEquals(Deflater.NO_COMPRESSION, (int) invokePrivate("resolveDeflaterLevel", new Class<?>[]{int.class}, -5));
    // 超过最大值被钳制到 BEST_COMPRESSION
    assertEquals(Deflater.BEST_COMPRESSION, (int) invokePrivate("resolveDeflaterLevel", new Class<?>[]{int.class}, 99));
    // 合法值不做修改
    assertEquals(5, (int) invokePrivate("resolveDeflaterLevel", new Class<?>[]{int.class}, 5));

    // bufferSize 为 0 时使用默认 64KB
    CompressOptions options = CompressOptions.builder().bufferSize(0).build();
    assertEquals(64 * 1024, (int) invokePrivate("bufferSize", new Class<?>[]{CompressOptions.class}, options));
    // bufferSize > 0 时使用自定义值
    options.setBufferSize(8);
    assertEquals(8, (int) invokePrivate("bufferSize", new Class<?>[]{CompressOptions.class}, options));
  }

  /**
   * ZIP 压缩/解压：覆盖包含根目录、软链接处理与单文件压缩。
   */
  @Test
  @DisplayName("Zip compression, symlink handling, include root flag")
  void testCompressZipIncludeRootAndSymlink() throws Exception {
    // 构造样例目录与软链接
    Path sourceDir = createSampleDir(tempDir.resolve("zip-src"));
    Path link = Files.createSymbolicLink(sourceDir.resolve("link.txt"), sourceDir.resolve("a.txt"));
    assertTrue(Files.isSymbolicLink(link));

    // 默认包含根目录压缩
    Path zipWithRoot = tempDir.resolve("with-root.zip");
    CompressUtil.compress(sourceDir, zipWithRoot);

    // 解压后应包含根目录结构，且不跟随软链接
    Path unzipRoot = tempDir.resolve("unzip-root");
    CompressUtil.decompress(zipWithRoot, unzipRoot);
    assertTrue(Files.exists(unzipRoot.resolve("zip-src/a.txt")));
    assertTrue(Files.exists(unzipRoot.resolve("zip-src/sub/b.txt")));
    assertFalse(Files.exists(unzipRoot.resolve("zip-src/link.txt")));

    // 不包含根目录且允许跟随软链接
    CompressOptions noRootFollowLinks = CompressOptions.builder()
      .includeRootDir(false)
      .followLinks(true)
      .preserveLastModified(false)
      .build();
    Path zipNoRoot = tempDir.resolve("no-root.zip");
    CompressUtil.compress(sourceDir, zipNoRoot, noRootFollowLinks);

    // 解压后应扁平输出，并包含软链接目标
    Path unzipNoRoot = tempDir.resolve("unzip-no-root");
    CompressUtil.decompress(zipNoRoot, unzipNoRoot, noRootFollowLinks);
    assertTrue(Files.exists(unzipNoRoot.resolve("a.txt")));
    assertTrue(Files.exists(unzipNoRoot.resolve("sub/b.txt")));
    assertTrue(Files.exists(unzipNoRoot.resolve("link.txt")));

    // 仅压缩单文件
    Path singleFileZip = tempDir.resolve("single-file.zip");
    CompressUtil.compress(Collections.singletonList(sourceDir.resolve("a.txt")), singleFileZip);
    Path singleOut = tempDir.resolve("single-out");
    CompressUtil.decompress(singleFileZip, singleOut);
    assertEquals("alpha", readText(singleOut.resolve("a.txt")));

    // 单文件软链接：不跟随时应被跳过
    Path symlinkZip = tempDir.resolve("symlink.zip");
    CompressOptions followFalse = CompressOptions.builder().followLinks(false).build();
    CompressUtil.compress(Collections.singletonList(link), symlinkZip, followFalse);
    Path symlinkOut = tempDir.resolve("symlink-out");
    CompressUtil.decompress(symlinkZip, symlinkOut);
    assertFalse(Files.exists(symlinkOut.resolve("link.txt")));

    // 单文件软链接：跟随时应按目标内容输出
    Path symlinkZip2 = tempDir.resolve("symlink-follow.zip");
    CompressOptions followTrue = CompressOptions.builder().followLinks(true).build();
    CompressUtil.compress(Collections.singletonList(link), symlinkZip2, followTrue);
    Path symlinkOut2 = tempDir.resolve("symlink-follow-out");
    CompressUtil.decompress(symlinkZip2, symlinkOut2);
    assertEquals("alpha", readText(symlinkOut2.resolve("link.txt")));
  }

  /**
   * TAR 及其压缩变体（tar/tar.gz/tar.bz2/tar.xz）压缩与解压验证。
   */
  @Test
  @DisplayName("Tar variants compression and decompression")
  void testCompressTarVariants() throws Exception {
    // 生成样例目录并设置文件时间戳
    Path sourceDir = createSampleDir(tempDir.resolve("tar-src"));
    Path aFile = sourceDir.resolve("a.txt");
    FileTime modTime = FileTime.fromMillis(1_700_000_000_000L);
    Files.setLastModifiedTime(aFile, modTime);

    // 构造格式与文件名映射
    Map<Format, String> formats = new HashMap<>();
    formats.put(Format.TAR, "archive.tar");
    formats.put(Format.TAR_GZ, "archive.tar.gz");
    formats.put(Format.TAR_BZ2, "archive.tar.bz2");
    formats.put(Format.TAR_XZ, "archive.tar.xz");

    // 逐个格式压缩并解压验证
    for (Map.Entry<Format, String> entry : formats.entrySet()) {
      Path archive = tempDir.resolve(entry.getValue());
      // 仅非 TAR 变体保留时间戳
      CompressOptions options = CompressOptions.builder()
        .preserveLastModified(entry.getKey() != Format.TAR)
        .build();
      CompressUtil.compress(sourceDir, archive, options);

      // 解压后校验内容与时间戳（允许 2 秒内偏差）
      Path outDir = tempDir.resolve("tar-out-" + entry.getKey().name().toLowerCase(Locale.ROOT));
      CompressUtil.decompress(archive, outDir, new CompressOptions());
      Path extracted = outDir.resolve("tar-src/a.txt");
      assertEquals("alpha", readText(extracted));
      if (entry.getKey() != Format.TAR) {
        assertTrue(Math.abs(Files.getLastModifiedTime(extracted).toMillis() - modTime.toMillis()) < 2000);
      }
    }
  }

  /**
   * 单文件格式（gz/bz2/xz）压缩与解压验证。
   */
  @Test
  @DisplayName("Single file formats compression and decompression")
  void testCompressSingleFormats() throws Exception {
    // 准备单文件源
    Path sourceFile = tempDir.resolve("data.txt");
    writeText(sourceFile, "single");
    Files.setLastModifiedTime(sourceFile, FileTime.fromMillis(1_650_000_000_000L));

    // GZ 压缩并带条目数限制解压
    Path gz = tempDir.resolve("data.txt.gz");
    CompressUtil.compress(sourceFile, gz);
    Path gzOut = tempDir.resolve("gz-out");
    CompressOptions limited = CompressOptions.builder().maxEntries(1).build();
    CompressUtil.decompress(gz, gzOut, limited);
    assertEquals("single", readText(gzOut.resolve("data.txt")));

    // BZ2 压缩/解压
    Path bz2 = tempDir.resolve("data.txt.bz2");
    CompressUtil.compress(sourceFile, bz2);
    Path bzOut = tempDir.resolve("bz-out");
    CompressUtil.decompress(bz2, bzOut);
    assertEquals("single", readText(bzOut.resolve("data.txt")));

    // XZ 压缩/解压
    Path xz = tempDir.resolve("data.txt.xz");
    CompressUtil.compress(sourceFile, xz);
    Path xzOut = tempDir.resolve("xz-out");
    CompressUtil.decompress(xz, xzOut);
    assertEquals("single", readText(xzOut.resolve("data.txt")));
  }

  /**
   * 7z 压缩/解压与时间戳保留验证。
   */
  @Test
  @DisplayName("7z compression and decompression")
  void testCompressSevenZ() throws Exception {
    // 先使用不保留时间戳压缩
    Path sourceDir = createSampleDir(tempDir.resolve("seven-src"));
    CompressOptions options = CompressOptions.builder().preserveLastModified(false).build();
    Path archive = tempDir.resolve("archive.7z");
    CompressUtil.compress(sourceDir, archive, options);

    // 解压验证内容存在
    Path outDir = tempDir.resolve("seven-out");
    CompressUtil.decompress(archive, outDir, options);
    assertTrue(Files.exists(outDir.resolve("seven-src/a.txt")));
    assertEquals("alpha", readText(outDir.resolve("seven-src/a.txt")));

    // 再使用保留时间戳压缩，验证时间戳回写
    FileTime modTime = FileTime.fromMillis(1_620_000_000_000L);
    Files.setLastModifiedTime(sourceDir.resolve("a.txt"), modTime);
    CompressOptions preserve = CompressOptions.builder().preserveLastModified(true).build();
    Path archivePreserve = tempDir.resolve("archive-preserve.7z");
    CompressUtil.compress(sourceDir, archivePreserve, preserve);
    Path outPreserve = tempDir.resolve("seven-out-preserve");
    CompressUtil.decompress(archivePreserve, outPreserve, preserve);
    Path extracted = outPreserve.resolve("seven-src/a.txt");
    assertTrue(Math.abs(Files.getLastModifiedTime(extracted).toMillis() - modTime.toMillis()) < 2000);
  }

  /**
   * 参数异常、覆盖策略、路径安全与解压限额校验。
   */
  @Test
  @DisplayName("Exceptions, limits, and safety checks")
  void testExceptionsAndLimits() throws Exception {
    // 准备测试源
    Path sourceFile = tempDir.resolve("source.txt");
    writeText(sourceFile, "abc");
    Path sourceDir = createSampleDir(tempDir.resolve("dir-src"));

    // 压缩参数异常
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.compress(Collections.emptyList(), tempDir.resolve("empty.zip")));
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.compress(Collections.singletonList(sourceFile), tempDir.resolve("bad.ext")));
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.compress(Arrays.asList(sourceFile, sourceFile), tempDir.resolve("multi.gz")));
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.compress(sourceDir, tempDir.resolve("dir.gz")));
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.compress(Collections.singletonList(tempDir.resolve("missing.txt")), tempDir.resolve("missing.zip")));
    assertThrows(NullPointerException.class,
      () -> CompressUtil.compress(Collections.singletonList((Path) null), tempDir.resolve("null.zip")));

    // 目标存在且禁止覆盖
    Path existing = tempDir.resolve("exists.zip");
    writeText(existing, "x");
    CompressOptions noOverwrite = CompressOptions.builder().overwrite(false).build();
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.compress(Collections.singletonList(sourceFile), existing, noOverwrite));

    // 解压参数异常
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.decompress(tempDir.resolve("no.zip"), tempDir.resolve("out")));
    Path badArchive = tempDir.resolve("bad.abc");
    writeText(badArchive, "bad");
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.decompress(badArchive, tempDir.resolve("out2")));

    // Zip Slip 路径穿越防护
    Path zipSlip = tempDir.resolve("zipslip.zip");
    Map<String, byte[]> slipEntries = new HashMap<>();
    slipEntries.put("../outside.txt", "oops".getBytes(StandardCharsets.UTF_8));
    slipEntries.put("//abs.txt", "abs".getBytes(StandardCharsets.UTF_8));
    createZip(zipSlip, slipEntries, null);
    Path target = tempDir.resolve("slip-out");
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.decompress(zipSlip, target));

    // 允许不安全路径时可以解压到目标外
    CompressOptions allowUnsafe = CompressOptions.builder().allowUnsafePath(true).build();
    CompressUtil.decompress(zipSlip, target, allowUnsafe);
    assertTrue(Files.exists(tempDir.resolve("outside.txt")));
    assertTrue(Files.exists(target.resolve("abs.txt")));

    // 解压条目/大小限制
    Path limitsZip = tempDir.resolve("limits.zip");
    Map<String, byte[]> limitsEntries = new HashMap<>();
    limitsEntries.put("a.txt", "abc".getBytes(StandardCharsets.UTF_8));
    limitsEntries.put("b.txt", "def".getBytes(StandardCharsets.UTF_8));
    createZip(limitsZip, limitsEntries, null);

    // 条目数限制
    CompressOptions maxEntries = CompressOptions.builder().maxEntries(1).build();
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.decompress(limitsZip, tempDir.resolve("max-entries"), maxEntries));

    // 单条目大小限制
    CompressOptions maxEntrySize = CompressOptions.builder().maxEntrySize(2).build();
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.decompress(limitsZip, tempDir.resolve("max-entry-size"), maxEntrySize));

    // 总大小限制
    CompressOptions maxTotalSize = CompressOptions.builder().maxTotalSize(5).build();
    assertThrows(IllegalArgumentException.class,
      () -> CompressUtil.decompress(limitsZip, tempDir.resolve("max-total-size"), maxTotalSize));

    // 禁止覆盖时解压应失败
    Path overwriteZip = tempDir.resolve("overwrite.zip");
    Map<String, byte[]> overwriteEntries = new HashMap<>();
    overwriteEntries.put("a.txt", "x".getBytes(StandardCharsets.UTF_8));
    createZip(overwriteZip, overwriteEntries, null);
    Path overwriteDir = tempDir.resolve("overwrite");
    Files.createDirectories(overwriteDir);
    writeText(overwriteDir.resolve("a.txt"), "exists");
    assertThrows(RuntimeException.class,
      () -> CompressUtil.decompress(overwriteZip, overwriteDir, noOverwrite));
  }

  /**
   * 空指针参数校验与 options 为 null 的默认行为。
   */
  @Test
  @DisplayName("Null checks and null options defaults")
  void testNullChecksAndNullOptions() throws Exception {
    // options 传 null 时应使用默认配置完成压缩/解压
    Path sourceFile = tempDir.resolve("null-options.txt");
    writeText(sourceFile, "nulls");
    Path zip = tempDir.resolve("null-options.zip");
    CompressUtil.compress(Collections.singletonList(sourceFile), zip, null);

    Path out = tempDir.resolve("null-options-out");
    CompressUtil.decompress(zip, out, null);
    assertEquals("nulls", readText(out.resolve("null-options.txt")));

    // compress 参数为 null 应抛出空指针异常
    assertThrows(NullPointerException.class, () -> CompressUtil.compress((Path) null, zip));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress(sourceFile, (Path) null));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress((Path) null, zip, new CompressOptions()));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress(sourceFile, (Path) null, new CompressOptions()));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress((java.util.List<Path>) null, zip));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress(Collections.singletonList(sourceFile), (Path) null));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress((java.util.List<Path>) null, zip, new CompressOptions()));
    assertThrows(NullPointerException.class, () -> CompressUtil.compress(Collections.singletonList(sourceFile),
      (Path) null, new CompressOptions()));

    // decompress 参数为 null 应抛出空指针异常
    assertThrows(NullPointerException.class, () -> CompressUtil.decompress((Path) null, out));
    assertThrows(NullPointerException.class, () -> CompressUtil.decompress(sourceFile, (Path) null));
    assertThrows(NullPointerException.class, () -> CompressUtil.decompress((Path) null, out, new CompressOptions()));
    assertThrows(NullPointerException.class, () -> CompressUtil.decompress(sourceFile, (Path) null, new CompressOptions()));
    assertThrows(NullPointerException.class, () -> CompressUtil.detectFormat((Path) null));
  }

  /**
   * 通过修改编译器生成的 switch 映射表覆盖 default 分支。
   */
  @Test
  @DisplayName("Switch default branches via map tweak")
  void testSwitchDefaultBranches() throws Exception {
    // 准备一个可识别的 ZIP
    Path file = tempDir.resolve("switch.txt");
    writeText(file, "switch");
    Path zip = tempDir.resolve("switch.zip");
    CompressUtil.compress(file, zip);

    // 获取 switch 映射并强制落入 default 分支
    int[] map = getSwitchMap();
    int idx = Format.ZIP.ordinal();
    int original = map[idx];
    map[idx] = 0;
    try {
      // 压缩与解压均应走到 default 分支抛错
      assertThrows(IllegalArgumentException.class,
        () -> CompressUtil.compress(Collections.singletonList(file), zip));
      assertThrows(IllegalArgumentException.class,
        () -> CompressUtil.decompress(zip, tempDir.resolve("switch-out")));
    } finally {
      // 恢复原映射，避免影响其他测试
      map[idx] = original;
    }
  }

  /**
   * ensureDirectory 在 IO 异常时应包装为 RuntimeException。
   */
  @Test
  @DisplayName("EnsureDirectory wraps IO errors")
  void testEnsureDirectoryIOException() throws Exception {
    // 先创建文件作为父路径，制造创建目录失败场景
    Path parentFile = tempDir.resolve("parent-file");
    writeText(parentFile, "x");
    Path child = parentFile.resolve("child");
    assertThrows(RuntimeException.class,
      () -> invokePrivateVoid("ensureDirectory", new Class<?>[]{Path.class}, child));
  }

  /**
   * 压缩过程出现 IO 异常时应被包装为 RuntimeException。
   */
  @Test
  @DisplayName("Compression catches IO failures")
  void testCompressCatchBlocks() throws Exception {
    // 准备文件与目录源
    Path sourceFile = tempDir.resolve("catch.txt");
    writeText(sourceFile, "x");
    Path sourceDir = createSampleDir(tempDir.resolve("catch-dir"));

    // 目标路径为目录，触发写入失败
    Path zipDir = Files.createDirectories(tempDir.resolve("bad.zip"));
    assertThrows(RuntimeException.class, () -> CompressUtil.compress(sourceDir, zipDir));

    // TAR 目标路径为目录
    Path tarDir = Files.createDirectories(tempDir.resolve("bad.tar"));
    assertThrows(RuntimeException.class, () -> CompressUtil.compress(sourceDir, tarDir));

    // 7z 目标路径为目录
    Path sevenDir = Files.createDirectories(tempDir.resolve("bad.7z"));
    assertThrows(RuntimeException.class, () -> CompressUtil.compress(sourceDir, sevenDir));

    // 单文件压缩目标路径为目录
    Path gzDir = Files.createDirectories(tempDir.resolve("bad.gz"));
    assertThrows(RuntimeException.class, () -> CompressUtil.compress(sourceFile, gzDir));
  }

  /**
   * 解压过程遇到非法输入时应包装为 RuntimeException。
   */
  @Test
  @DisplayName("Decompression catches IO failures")
  void testDecompressCatchBlocks() throws Exception {
    // 非法 tar.gz 内容
    Path badTarGz = tempDir.resolve("bad.tar.gz");
    writeText(badTarGz, "not-gzip");
    assertThrows(RuntimeException.class,
      () -> CompressUtil.decompress(badTarGz, tempDir.resolve("bad-tar-out")));

    // 非法 7z 内容
    Path bad7z = tempDir.resolve("bad.7z");
    writeText(bad7z, "not-7z");
    assertThrows(RuntimeException.class,
      () -> CompressUtil.decompress(bad7z, tempDir.resolve("bad-7z-out")));

    // 非法 gz 内容
    Path badGz = tempDir.resolve("bad.gz");
    writeText(badGz, "not-gz");
    assertThrows(RuntimeException.class,
      () -> CompressUtil.decompress(badGz, tempDir.resolve("bad-gz-out")));
  }

  /**
   * 通过内部 EntryWriter 覆盖目录名以 "/" 结尾的分支。
   */
  @Test
  @DisplayName("Entry writers keep trailing slash")
  void testEntryNameEndsWithSlashInWriters() throws Exception {
    // 准备目录与缓冲区
    CompressOptions options = new CompressOptions();
    Path dir = Files.createDirectories(tempDir.resolve("slash-dir"));
    int bufferSize = 8;
    byte[] buffer = new byte[bufferSize];

    // ZIP 写入器覆盖目录条目
    ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
    try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(zipBytes)) {
      Object writer = newInnerInstance("top.csaf.io.CompressUtil$1",
        new Class<?>[]{CompressOptions.class, ZipArchiveOutputStream.class, int.class, byte[].class},
        options, zipOut, bufferSize, buffer);
      invokeInstanceVoid(writer, "writeDirectory", new Class<?>[]{Path.class, String.class}, dir, "dir/");
      zipOut.finish();
    }

    // TAR 写入器覆盖目录条目
    ByteArrayOutputStream tarBytes = new ByteArrayOutputStream();
    try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tarBytes)) {
      Object writer = newInnerInstance("top.csaf.io.CompressUtil$2",
        new Class<?>[]{CompressOptions.class, TarArchiveOutputStream.class, int.class, byte[].class},
        options, tarOut, bufferSize, buffer);
      invokeInstanceVoid(writer, "writeDirectory", new Class<?>[]{Path.class, String.class}, dir, "dir/");
      tarOut.finish();
    }

    // 7z 写入器覆盖目录条目
    Path sevenPath = tempDir.resolve("slash.7z");
    try (SevenZOutputFile sevenZ = new SevenZOutputFile(sevenPath.toFile())) {
      Object writer = newInnerInstance("top.csaf.io.CompressUtil$3",
        new Class<?>[]{SevenZOutputFile.class, CompressOptions.class, int.class, byte[].class},
        sevenZ, options, bufferSize, buffer);
      invokeInstanceVoid(writer, "writeDirectory", new Class<?>[]{Path.class, String.class}, dir, "dir/");
    }
    // 验证 7z 文件已创建
    assertTrue(Files.exists(sevenPath));
  }

  /**
   * 覆盖 parent 为空分支与 ExtractTracker 触发路径。
   */
  @Test
  @DisplayName("Parent null branch and tracker paths")
  void testParentNullAndTrackerBranches() throws Exception {
    // 使用根目录作为解压目标，便于触发 parent 为 null 的分支
    Path root = tempDir.getRoot();
    // 限制条目数为 1，触发 tracker 校验
    CompressOptions tracking = CompressOptions.builder().maxEntries(1).build();
    byte[] payload = "x".getBytes(StandardCharsets.UTF_8);

    // ZIP 使用 "." 条目名触发特殊路径解析
    Path zip = tempDir.resolve("root.zip");
    Map<String, byte[]> zipEntries = new HashMap<>();
    zipEntries.put(".", payload);
    createZip(zip, zipEntries, null);
    assertThrows(RuntimeException.class, () -> CompressUtil.decompress(zip, root, tracking));

    // TAR 使用 "." 条目名触发特殊路径解析
    Path tar = tempDir.resolve("root.tar");
    createTar(tar, ".", payload);
    assertThrows(RuntimeException.class, () -> CompressUtil.decompress(tar, root, tracking));

    // 7z 使用 "." 条目名触发特殊路径解析
    Path seven = tempDir.resolve("root.7z");
    createSevenZ(seven, ".", payload, false);
    assertThrows(RuntimeException.class, () -> CompressUtil.decompress(seven, root, tracking));
  }

  /**
   * 7z 不含时间戳时的解压路径，以及 tracker 限额逻辑覆盖。
   */
  @Test
  @DisplayName("7z without last modified date and tracker copy")
  void testSevenZTrackerCopyWithLimits() throws Exception {
    // 构造不包含时间戳的 7z
    Path archive = tempDir.resolve("nodate.7z");
    byte[] data = "nodate".getBytes(StandardCharsets.UTF_8);
    createSevenZ(archive, "nodate.txt", data, false);

    // 设置条目数与单条目大小限制
    CompressOptions options = CompressOptions.builder()
      .maxEntries(3)
      .maxEntrySize(1024)
      .build();
    Path outDir = tempDir.resolve("nodate-out");
    // 解压后校验内容
    CompressUtil.decompress(archive, outDir, options);
    assertEquals("nodate", readText(outDir.resolve("nodate.txt")));
  }

  /**
   * walkSources 中 entryName 为空的路径应被跳过。
   */
  @Test
  @DisplayName("WalkSources skips empty entry names")
  void testWalkSourcesEmptyEntryName() throws Exception {
    // 使用根路径作为 base，触发 entryName 为空的分支
    Path root = tempDir.getRoot();
    CompressOptions options = CompressOptions.builder().includeRootDir(true).build();
    // 使用代理确保不会真的写入任何条目
    Class<?> entryWriterClass = Class.forName("top.csaf.io.CompressUtil$EntryWriter");
    Object writer = newEntryWriterProxy(entryWriterClass);
    // 反射创建内部 FileVisitor
    Object visitor = newInnerInstance("top.csaf.io.CompressUtil$4",
      new Class<?>[]{CompressOptions.class, Path.class, Path.class, entryWriterClass},
      options, root, root, writer);
    // 触发 preVisitDirectory 与 visitFile 分支
    BasicFileAttributes attrs = Files.readAttributes(root, BasicFileAttributes.class);
    invokeInstanceVoid(visitor, "preVisitDirectory", new Class<?>[]{Path.class, BasicFileAttributes.class}, root, attrs);
    invokeInstanceVoid(visitor, "visitFile", new Class<?>[]{Path.class, BasicFileAttributes.class}, root, attrs);
  }

  /**
   * 通过反射覆盖私有辅助方法分支。
   */
  @Test
  @DisplayName("Private helpers coverage")
  void testPrivateHelpersCoverage() throws Exception {
    // prepareTarget 覆盖父目录创建逻辑
    Path root = tempDir.getRoot();
    invokePrivateVoid("prepareTarget", new Class<?>[]{Path.class, CompressOptions.class}, root, new CompressOptions());

    // ensureDirectory 传入文件路径应抛错
    Path filePath = tempDir.resolve("file.txt");
    writeText(filePath, "x");
    assertThrows(IllegalArgumentException.class,
      () -> invokePrivateVoid("ensureDirectory", new Class<?>[]{Path.class}, filePath));

    // wrapCompressorOutputStream 的默认分支（ZIP）应返回原流
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStream wrappedOut = invokePrivate("wrapCompressorOutputStream",
      new Class<?>[]{OutputStream.class, Format.class, CompressOptions.class}, out, Format.ZIP, new CompressOptions());
    assertSame(out, wrappedOut);

    // BZ2 分支覆盖 blockSize 计算
    CompressOptions bz2Options = CompressOptions.builder()
      .compressionLevel(Deflater.NO_COMPRESSION)
      .build();
    OutputStream bz2Out = invokePrivate("wrapCompressorOutputStream",
      new Class<?>[]{OutputStream.class, Format.class, CompressOptions.class},
      new ByteArrayOutputStream(), Format.BZ2, bz2Options);
    bz2Out.close();

    // wrapCompressorInputStream 默认分支应返回原流
    InputStream in = new ByteArrayInputStream(new byte[0]);
    InputStream wrappedIn = invokePrivate("wrapCompressorInputStream",
      new Class<?>[]{InputStream.class, Format.class}, in, Format.SEVEN_Z);
    assertSame(in, wrappedIn);

    // createXzOutputStream 触发回退构造路径
    OutputStream xzOut = invokePrivate("createXzOutputStream",
      new Class<?>[]{OutputStream.class, int.class}, new FailOnceOutputStream(), Deflater.BEST_COMPRESSION);
    xzOut.close();

    // resolveEntryPath 允许不安全路径时的解析行为
    CompressOptions allowUnsafe = CompressOptions.builder().allowUnsafePath(true).build();
    Path resolved = invokePrivate("resolveEntryPath",
      new Class<?>[]{Path.class, String.class, CompressOptions.class}, tempDir, "//a.txt", allowUnsafe);
    assertEquals(tempDir.resolve("a.txt").normalize(), resolved);

    // applyLastModified 的分支覆盖
    CompressOptions preserveFalse = CompressOptions.builder().preserveLastModified(false).build();
    invokePrivateVoid("applyLastModified", new Class<?>[]{Path.class, long.class, CompressOptions.class},
      tempDir.resolve("missing.txt"), 123L, preserveFalse);
    invokePrivateVoid("applyLastModified", new Class<?>[]{Path.class, long.class, CompressOptions.class},
      tempDir.resolve("missing.txt"), 123L, new CompressOptions());
    invokePrivateVoid("applyLastModified", new Class<?>[]{Path.class, long.class, CompressOptions.class},
      tempDir.resolve("missing.txt"), 0L, new CompressOptions());

    // toEntryName base 为空的分支
    String entryName = invokePrivate("toEntryName", new Class<?>[]{Path.class, Path.class}, null, root);
    assertEquals("", entryName);

    // toEntryName 相对路径为空时回退到文件名
    String emptyRelative = invokePrivate("toEntryName", new Class<?>[]{Path.class, Path.class}, tempDir, tempDir);
    assertEquals(tempDir.getFileName().toString(), emptyRelative);

    // resolveBaseForDirectory root 路径分支
    Path baseFromRoot = invokePrivate("resolveBaseForDirectory", new Class<?>[]{Path.class, boolean.class}, root, true);
    assertEquals(root, baseFromRoot);

    // createXzOutputStream 传入空流触发 NPE
    assertThrows(NullPointerException.class,
      () -> invokePrivate("createXzOutputStream", new Class<?>[]{OutputStream.class, int.class}, null, 1));
  }

  /**
   * 创建包含文件、子目录和空目录的样例目录。
   */
  private static Path createSampleDir(Path root) throws IOException {
    // 根目录
    Files.createDirectories(root);
    // 普通文件
    writeText(root.resolve("a.txt"), "alpha");
    // 子目录与文件
    Files.createDirectories(root.resolve("sub"));
    writeText(root.resolve("sub/b.txt"), "bravo");
    // 空目录
    Files.createDirectories(root.resolve("empty"));
    return root;
  }

  /**
   * 以 UTF-8 写入文本到指定路径。
   */
  private static void writeText(Path path, String content) throws IOException {
    // 保证父目录存在
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 读取 UTF-8 文本内容。
   */
  private static String readText(Path path) throws IOException {
    // 直接读取全部字节并按 UTF-8 解码
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  /**
   * 创建 ZIP，支持指定条目内容与时间戳。
   */
  private static void createZip(Path zipPath, Map<String, byte[]> entries, Map<String, Long> times) throws IOException {
    try (ZipArchiveOutputStream out = new ZipArchiveOutputStream(Files.newOutputStream(zipPath))) {
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        // 为当前条目创建元数据
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(entry.getKey());
        // 可选时间戳覆盖
        if (times != null && times.containsKey(entry.getKey())) {
          zipEntry.setTime(times.get(entry.getKey()));
        }
        // 写入条目头与内容
        out.putArchiveEntry(zipEntry);
        out.write(entry.getValue());
        out.closeArchiveEntry();
      }
    }
  }

  /**
   * 创建 TAR，写入单个条目。
   */
  private static void createTar(Path tarPath, String entryName, byte[] data) throws IOException {
    try (TarArchiveOutputStream out = new TarArchiveOutputStream(Files.newOutputStream(tarPath))) {
      TarArchiveEntry entry = new TarArchiveEntry(entryName);
      // TAR 条目需要显式设置大小
      entry.setSize(data.length);
      // 写入条目头与内容
      out.putArchiveEntry(entry);
      out.write(data);
      out.closeArchiveEntry();
    }
  }

  /**
   * 创建 7z，支持控制是否写入最后修改时间。
   */
  private static void createSevenZ(Path sevenZPath, String entryName, byte[] data, boolean withLastModified) throws IOException {
    try (SevenZOutputFile out = new SevenZOutputFile(sevenZPath.toFile())) {
      // 初始化 7z 条目元数据
      SevenZArchiveEntry entry = new SevenZArchiveEntry();
      entry.setName(entryName);
      entry.setSize(data.length);
      // 标记该条目包含内容流
      entry.setHasStream(true);
      entry.setDirectory(false);
      // 选择是否写入时间戳
      if (withLastModified) {
        entry.setLastModifiedDate(new Date(1_700_000_000_000L));
      } else {
        entry.setHasLastModifiedDate(false);
      }
      out.putArchiveEntry(entry);
      out.write(data);
      out.closeArchiveEntry();
    }
  }

  /**
   * 获取编译器生成的 switch 映射数组。
   */
  private static int[] getSwitchMap() throws Exception {
    // 定位编译器生成的 switch 映射类与字段
    Class<?> holder = Class.forName("top.csaf.io.CompressUtil$5");
    Field field = holder.getDeclaredField("$SwitchMap$top$csaf$io$CompressUtil$Format");
    // 允许访问私有静态字段
    field.setAccessible(true);
    return (int[]) field.get(null);
  }

  /**
   * 通过反射创建内部类实例。
   */
  private static Object newInnerInstance(String className, Class<?>[] paramTypes, Object... args) throws Exception {
    // 通过类名反射获取内部类
    Class<?> cls = Class.forName(className);
    // 访问私有构造器
    Constructor<?> ctor = cls.getDeclaredConstructor(paramTypes);
    ctor.setAccessible(true);
    // 实例化并返回
    return ctor.newInstance(args);
  }

  /**
   * 调用实例方法并解包 InvocationTargetException。
   */
  private static void invokeInstanceVoid(Object target, String name, Class<?>[] paramTypes, Object... args) throws Exception {
    // 反射获取目标方法
    Method method = target.getClass().getDeclaredMethod(name, paramTypes);
    method.setAccessible(true);
    try {
      // 调用并保持原始异常语义
      method.invoke(target, args);
    } catch (InvocationTargetException e) {
      // 解包真实异常
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw e;
    }
  }

  /**
   * 创建 EntryWriter 代理，若被调用则抛出 AssertionError。
   */
  private static Object newEntryWriterProxy(Class<?> entryWriterClass) {
    return Proxy.newProxyInstance(entryWriterClass.getClassLoader(), new Class<?>[]{entryWriterClass},
      (proxy, method, args) -> {
        // 一旦被调用就抛错，确保走到“不会写入”分支
        throw new AssertionError("EntryWriter should not be called");
      });
  }

  /**
   * 首次写入即失败的输出流，用于覆盖异常分支。
   */
  private static final class FailOnceOutputStream extends OutputStream {
    // 实际接收成功写入的内容
    private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
    // 控制仅首写失败
    private boolean fail = true;

    /**
     * 首次写入抛出异常以触发失败分支。
     */
    @Override
    public void write(int b) throws IOException {
      // 首次写入直接失败
      if (fail) {
        fail = false;
        throw new IOException("fail once");
      }
      // 后续写入转交给真实输出流
      delegate.write(b);
    }

    /**
     * 首次写入抛出异常以触发失败分支。
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      // 首次写入直接失败
      if (fail) {
        fail = false;
        throw new IOException("fail once");
      }
      // 后续写入转交给真实输出流
      delegate.write(b, off, len);
    }
  }

  /**
   * 反射调用 CompressUtil 私有方法并返回结果。
   */
  @SuppressWarnings("unchecked")
  private static <T> T invokePrivate(String name, Class<?>[] paramTypes, Object... args) throws Exception {
    // 通过反射定位 CompressUtil 私有方法
    Method method = CompressUtil.class.getDeclaredMethod(name, paramTypes);
    method.setAccessible(true);
    try {
      // 调用并返回结果
      return (T) method.invoke(null, args);
    } catch (InvocationTargetException e) {
      // 解包真实异常
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw e;
    }
  }

  /**
   * 反射调用 CompressUtil 私有 void 方法并解包异常。
   */
  private static void invokePrivateVoid(String name, Class<?>[] paramTypes, Object... args) throws Exception {
    // 通过反射定位 CompressUtil 私有方法
    Method method = CompressUtil.class.getDeclaredMethod(name, paramTypes);
    method.setAccessible(true);
    try {
      // 调用 void 方法并保持异常语义
      method.invoke(null, args);
    } catch (InvocationTargetException e) {
      // 解包真实异常
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw e;
    }
  }
}
