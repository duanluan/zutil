package top.csaf.io;

import lombok.NonNull;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.Deflater;

/**
 * 压缩/解压工具，支持多种常见归档格式。
 */
public class CompressUtil {
  private static final long GZIP_EPOCH_SECONDS_MAX = 0xFFFFFFFFL;

  /**
   * 支持的格式枚举。
   */
  public enum Format {
    ZIP, // ZIP 归档
    TAR, // TAR 归档
    TAR_GZ, // TAR + GZ
    TAR_BZ2, // TAR + BZ2
    TAR_XZ, // TAR + XZ
    GZ, // 单文件 GZ
    BZ2, // 单文件 BZ2
    XZ, // 单文件 XZ
    SEVEN_Z, // 7z 归档
    RAR // RAR 归档
  }

  /**
   * 压缩单个源路径到目标文件，格式由目标文件后缀识别。
   */
  public static void compress(@NonNull Path source, @NonNull Path target) {
    compress(Collections.singletonList(source), target, new CompressOptions());
  }

  /**
   * 压缩单个源路径到目标文件，可传入压缩选项。
   */
  public static void compress(@NonNull Path source, @NonNull Path target, CompressOptions options) {
    compress(Collections.singletonList(source), target, options);
  }

  /**
   * 压缩多个源路径到目标文件，格式由目标文件后缀识别。
   */
  public static void compress(@NonNull List<Path> sources, @NonNull Path target) {
    compress(sources, target, new CompressOptions());
  }

  /**
   * 压缩入口：校验参数、识别格式并分派到具体实现。
   */
  public static void compress(@NonNull List<Path> sources, @NonNull Path target, CompressOptions options) {
    // 允许传 null，使用默认选项
    CompressOptions opts = options == null ? new CompressOptions() : options;
    // 至少包含一个源
    if (sources.isEmpty()) {
      throw new IllegalArgumentException("Sources are empty");
    }
    // 根据目标文件后缀识别格式
    Format format = detectFormat(target);
    if (format == null) {
      throw new IllegalArgumentException("Unsupported archive format: " + target);
    }
    // 规范化并校验源路径
    List<Path> normalized = normalizeSources(sources);
    // 准备目标文件（父目录/覆盖策略）
    prepareTarget(target, opts);
    // 单文件格式只允许单个文件
    if (isSingleFileFormat(format)) {
      if (normalized.size() != 1) {
        throw new IllegalArgumentException("Single file format only supports one source");
      }
      Path source = normalized.get(0);
      if (Files.isDirectory(source)) {
        throw new IllegalArgumentException("Single file format does not support directory");
      }
      compressSingle(source, target, format, opts);
      return;
    }
    // 按格式分派
    switch (format) {
      case ZIP:
        compressZip(normalized, target, opts);
        break;
      case TAR:
      case TAR_GZ:
      case TAR_BZ2:
      case TAR_XZ:
        compressTar(normalized, target, format, opts);
        break;
      case SEVEN_Z:
        compressSevenZ(normalized, target, opts);
        break;
      default:
        throw new IllegalArgumentException("Unsupported archive format: " + format);
    }
  }

  /**
   * 解压到目标目录，格式由源文件后缀识别。
   */
  public static void decompress(@NonNull Path source, @NonNull Path targetDir) {
    decompress(source, targetDir, new CompressOptions());
  }

  /**
   * 解压入口：校验参数、识别格式并分派到具体实现。
   */
  public static void decompress(@NonNull Path source, @NonNull Path targetDir, CompressOptions options) {
    // 允许传 null，使用默认选项
    CompressOptions opts = options == null ? new CompressOptions() : options;
    // 源文件必须存在
    if (!Files.exists(source)) {
      throw new IllegalArgumentException("Source does not exist: " + source);
    }
    // 根据源文件后缀识别格式
    Format format = detectFormat(source);
    if (format == null) {
      throw new IllegalArgumentException("Unsupported archive format: " + source);
    }
    // 确保目标目录存在
    ensureDirectory(targetDir);
    // 按选项创建限额追踪器
    ExtractTracker tracker = ExtractTracker.fromOptions(opts);
    // 按格式分派
    switch (format) {
      case ZIP:
        decompressZip(source, targetDir, opts, tracker);
        break;
      case TAR:
      case TAR_GZ:
      case TAR_BZ2:
      case TAR_XZ:
        decompressTar(source, targetDir, format, opts, tracker);
        break;
      case GZ:
      case BZ2:
      case XZ:
        decompressSingle(source, targetDir, format, opts, tracker);
        break;
      case SEVEN_Z:
        decompressSevenZ(source, targetDir, opts, tracker);
        break;
      case RAR:
        decompressRar(source, targetDir, opts, tracker);
        break;
      default:
        throw new IllegalArgumentException("Unsupported archive format: " + format);
    }
  }

  /**
   * 根据路径后缀推断格式。
   */
  public static Format detectFormat(@NonNull Path path) {
    Path fileName = path.getFileName();
    if (fileName == null) {
      return null;
    }
    return detectFormat(fileName.toString());
  }

  /**
   * 根据文件名后缀匹配归档格式。
   */
  private static Format detectFormat(String name) {
    // 统一为小写进行后缀匹配
    String lower = name.toLowerCase(Locale.ROOT);
    // tar.gz / tgz
    if (lower.endsWith(".tar.gz") || lower.endsWith(".tgz")) {
      return Format.TAR_GZ;
    }
    // tar.bz2 / tbz / tbz2
    if (lower.endsWith(".tar.bz2") || lower.endsWith(".tbz") || lower.endsWith(".tbz2")) {
      return Format.TAR_BZ2;
    }
    // tar.xz / txz
    if (lower.endsWith(".tar.xz") || lower.endsWith(".txz")) {
      return Format.TAR_XZ;
    }
    // tar
    if (lower.endsWith(".tar")) {
      return Format.TAR;
    }
    // zip
    if (lower.endsWith(".zip")) {
      return Format.ZIP;
    }
    // 7z
    if (lower.endsWith(".7z")) {
      return Format.SEVEN_Z;
    }
    // rar 归档
    if (lower.endsWith(".rar")) {
      return Format.RAR;
    }
    // gz
    if (lower.endsWith(".gz")) {
      return Format.GZ;
    }
    // bz2
    if (lower.endsWith(".bz2")) {
      return Format.BZ2;
    }
    // xz
    if (lower.endsWith(".xz")) {
      return Format.XZ;
    }
    // 未匹配到任何格式
    return null;
  }

  /**
   * 是否为单文件压缩格式（非归档）。
   */
  private static boolean isSingleFileFormat(Format format) {
    // 仅 GZ/BZ2/XZ 为单文件压缩格式
    return format == Format.GZ || format == Format.BZ2 || format == Format.XZ;
  }

  /**
   * 规范化源路径并校验存在性。
   */
  private static List<Path> normalizeSources(List<Path> sources) {
    List<Path> normalized = new ArrayList<>(sources.size());
    for (Path source : sources) {
      // 统一绝对路径并消除冗余段
      Path path = Objects.requireNonNull(source, "Source is null").toAbsolutePath().normalize();
      // 源路径必须存在
      if (!Files.exists(path)) {
        throw new IllegalArgumentException("Source does not exist: " + path);
      }
      normalized.add(path);
    }
    return normalized;
  }

  /**
   * 准备目标文件：创建父目录并处理覆盖策略。
   */
  private static void prepareTarget(Path target, CompressOptions options) {
    Path parent = target.toAbsolutePath().normalize().getParent();
    // 先保证父目录存在
    if (parent != null) {
      ensureDirectory(parent);
    }
    // 不允许覆盖时直接抛错
    if (Files.exists(target) && !options.isOverwrite()) {
      throw new IllegalArgumentException("Target exists: " + target);
    }
  }

  /**
   * 确保目录存在，不存在则创建。
   */
  private static void ensureDirectory(Path dir) {
    try {
      // 目录不存在则创建
      if (!Files.exists(dir)) {
        Files.createDirectories(dir);
      } else if (!Files.isDirectory(dir)) {
        // 路径存在但不是目录
        throw new IllegalArgumentException("Target is not a directory: " + dir);
      }
    } catch (IOException e) {
      // 统一包装 IO 异常
      throw new RuntimeException("Create directory failed: " + dir, e);
    }
  }

  /**
   * ZIP 压缩实现。
   */
  private static void compressZip(List<Path> sources, Path target, CompressOptions options) {
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    try (
      OutputStream fileOut = newOutputStream(target, options);
      BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut, bufferSize);
      ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(bufferedOut)
    ) {
      // 设置编码与压缩级别
      zipOut.setEncoding(options.getCharset().name());
      zipOut.setLevel(resolveDeflaterLevel(options.getCompressionLevel()));
      // 按需启用 Zip64
      zipOut.setUseZip64(Zip64Mode.AsNeeded);
      // 遍历源文件/目录并写入 ZIP
      walkSources(sources, options, new EntryWriter() {
        @Override
        public void writeDirectory(Path dir, String entryName) throws IOException {
          // 目录项需要以 / 结尾
          if (!entryName.endsWith("/")) {
            entryName = entryName + "/";
          }
          ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
          if (options.isPreserveLastModified()) {
            // 保留目录最后修改时间
            entry.setTime(Files.getLastModifiedTime(dir).toMillis());
          }
          zipOut.putArchiveEntry(entry);
          zipOut.closeArchiveEntry();
        }

        @Override
        public void writeFile(Path file, String entryName) throws IOException {
          ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
          if (options.isPreserveLastModified()) {
            // 保留文件最后修改时间
            entry.setTime(Files.getLastModifiedTime(file).toMillis());
          }
          zipOut.putArchiveEntry(entry);
          try (InputStream in = new BufferedInputStream(Files.newInputStream(file), bufferSize)) {
            // 复制文件内容
            copy(in, zipOut, buffer, null);
          }
          zipOut.closeArchiveEntry();
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("Compress zip failed: " + target, e);
    }
  }

  /**
   * TAR 及其变体压缩实现。
   */
  private static void compressTar(List<Path> sources, Path target, Format format, CompressOptions options) {
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    try (
      OutputStream fileOut = newOutputStream(target, options);
      BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut, bufferSize);
      // 根据格式包裹压缩输出流
      OutputStream compressedOut = wrapCompressorOutputStream(bufferedOut, format, options);
      TarArchiveOutputStream tarOut = new TarArchiveOutputStream(compressedOut)
    ) {
      // 兼容长文件名与大数字
      tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
      tarOut.setAddPaxHeadersForNonAsciiNames(true);
      // 遍历源文件/目录并写入 TAR
      walkSources(sources, options, new EntryWriter() {
        @Override
        public void writeDirectory(Path dir, String entryName) throws IOException {
          // 目录项需要以 / 结尾
          if (!entryName.endsWith("/")) {
            entryName = entryName + "/";
          }
          TarArchiveEntry entry = new TarArchiveEntry(entryName);
          if (options.isPreserveLastModified()) {
            // 保留目录最后修改时间
            entry.setModTime(Files.getLastModifiedTime(dir).toMillis());
          }
          tarOut.putArchiveEntry(entry);
          tarOut.closeArchiveEntry();
        }

        @Override
        public void writeFile(Path file, String entryName) throws IOException {
          TarArchiveEntry entry = new TarArchiveEntry(entryName);
          // 手动设置大小，避免使用 File 构造器隐式带入源文件时间戳
          entry.setSize(Files.size(file));
          if (options.isPreserveLastModified()) {
            // 保留文件最后修改时间
            entry.setModTime(Files.getLastModifiedTime(file).toMillis());
          }
          tarOut.putArchiveEntry(entry);
          try (InputStream in = new BufferedInputStream(Files.newInputStream(file), bufferSize)) {
            // 复制文件内容
            copy(in, tarOut, buffer, null);
          }
          tarOut.closeArchiveEntry();
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("Compress tar failed: " + target, e);
    }
  }

  /**
   * 7z 压缩实现。
   */
  private static void compressSevenZ(List<Path> sources, Path target, CompressOptions options) {
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    // 创建 7z 输出文件
    try (SevenZOutputFile sevenZ = new SevenZOutputFile(target.toFile())) {
      // 遍历源文件/目录并写入 7z
      walkSources(sources, options, new EntryWriter() {
        @Override
        public void writeDirectory(Path dir, String entryName) throws IOException {
          // 目录项需要以 / 结尾
          if (!entryName.endsWith("/")) {
            entryName = entryName + "/";
          }
          SevenZArchiveEntry entry = new SevenZArchiveEntry();
          entry.setName(entryName);
          entry.setDirectory(true);
          entry.setHasStream(false);
          if (options.isPreserveLastModified()) {
            // 保留目录最后修改时间
            entry.setLastModifiedDate(new Date(Files.getLastModifiedTime(dir).toMillis()));
          } else {
            entry.setHasLastModifiedDate(false);
          }
          sevenZ.putArchiveEntry(entry);
          sevenZ.closeArchiveEntry();
        }

        @Override
        public void writeFile(Path file, String entryName) throws IOException {
          SevenZArchiveEntry entry = new SevenZArchiveEntry();
          entry.setName(entryName);
          entry.setDirectory(false);
          entry.setHasStream(true);
          entry.setSize(Files.size(file));
          if (options.isPreserveLastModified()) {
            // 保留文件最后修改时间
            entry.setLastModifiedDate(new Date(Files.getLastModifiedTime(file).toMillis()));
          } else {
            entry.setHasLastModifiedDate(false);
          }
          sevenZ.putArchiveEntry(entry);
          try (InputStream in = new BufferedInputStream(Files.newInputStream(file), bufferSize)) {
            // 复制文件内容
            copy(in, sevenZ, buffer);
          }
          sevenZ.closeArchiveEntry();
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("Compress 7z failed: " + target, e);
    }
  }

  /**
   * 单文件压缩实现（GZ/BZ2/XZ）。
   */
  private static void compressSingle(Path source, Path target, Format format, CompressOptions options) {
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    try (
      InputStream in = new BufferedInputStream(Files.newInputStream(source), bufferSize);
      OutputStream fileOut = newOutputStream(target, options);
      BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut, bufferSize);
      // 根据格式包裹压缩输出流
      OutputStream compressedOut = format == Format.GZ
        ? new GzipCompressorOutputStream(bufferedOut, buildSingleGzipParameters(source, options))
        : wrapCompressorOutputStream(bufferedOut, format, options)
    ) {
      // 直接压缩文件内容
      copy(in, compressedOut, buffer, null);
    } catch (IOException e) {
      throw new RuntimeException("Compress file failed: " + target, e);
    }
  }

  /**
   * ZIP 解压实现。
   */
  private static void decompressZip(Path source, Path targetDir, CompressOptions options, ExtractTracker tracker) {
    Path normalizedTarget = targetDir.toAbsolutePath().normalize();
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    // 按指定编码读取 ZIP 条目名
    try (ZipFile zipFile = ZipFile.builder().setPath(source).setCharset(options.getCharset()).get()) {
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();
        // 解压条目数限制
        if (tracker != null) {
          tracker.onEntry(entry.getName());
        }
        // 解析条目路径并防止 Zip Slip
        Path entryPath = resolveEntryPath(normalizedTarget, entry.getName(), options);
        if (prepareEntryTarget(entryPath, entry.isDirectory())) {
          continue;
        }
        try (InputStream in = new BufferedInputStream(zipFile.getInputStream(entry), bufferSize);
             OutputStream out = newOutputStream(entryPath, options)) {
          // 写出文件内容
          copy(in, out, buffer, tracker);
        }
        // 尝试还原最后修改时间
        applyLastModified(entryPath, entry.getTime(), options);
      }
    } catch (IOException e) {
      throw new RuntimeException("Decompress zip failed: " + source, e);
    }
  }

  /**
   * TAR 及其变体解压实现。
   */
  private static void decompressTar(Path source, Path targetDir, Format format, CompressOptions options, ExtractTracker tracker) {
    Path normalizedTarget = targetDir.toAbsolutePath().normalize();
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    try (
      InputStream fileIn = Files.newInputStream(source);
      BufferedInputStream bufferedIn = new BufferedInputStream(fileIn, bufferSize);
      // 根据格式包裹解压输入流
      InputStream compressedIn = wrapCompressorInputStream(bufferedIn, format);
      TarArchiveInputStream tarIn = new TarArchiveInputStream(compressedIn)
    ) {
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextEntry()) != null) {
        // 解压条目数限制
        if (tracker != null) {
          tracker.onEntry(entry.getName());
        }
        // 解析条目路径并防止目录穿越
        Path entryPath = resolveEntryPath(normalizedTarget, entry.getName(), options);
        if (prepareEntryTarget(entryPath, entry.isDirectory())) {
          continue;
        }
        try (OutputStream out = newOutputStream(entryPath, options)) {
          // 写出文件内容
          copy(tarIn, out, buffer, tracker);
        }
        // 尝试还原最后修改时间
        applyLastModified(entryPath, entry.getModTime().getTime(), options);
      }
    } catch (IOException e) {
      throw new RuntimeException("Decompress tar failed: " + source, e);
    }
  }

  /**
   * 7z 解压实现。
   */
  private static void decompressSevenZ(Path source, Path targetDir, CompressOptions options, ExtractTracker tracker) {
    Path normalizedTarget = targetDir.toAbsolutePath().normalize();
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    // 打开 7z 文件读取
    try (SevenZFile sevenZ = SevenZFile.builder().setPath(source).get()) {
      SevenZArchiveEntry entry;
      while ((entry = sevenZ.getNextEntry()) != null) {
        // 解压条目数限制
        if (tracker != null) {
          tracker.onEntry(entry.getName());
        }
        // 解析条目路径并防止目录穿越
        Path entryPath = resolveEntryPath(normalizedTarget, entry.getName(), options);
        if (prepareEntryTarget(entryPath, entry.isDirectory())) {
          continue;
        }
        try (OutputStream out = newOutputStream(entryPath, options)) {
          // 写出文件内容
          copy(sevenZ, out, buffer, tracker);
        }
        // 7z 条目可能没有时间戳
        Date lastModified = null;
        if (entry.getHasLastModifiedDate()) {
          // 仅在标记存在时读取时间戳，避免异常
          lastModified = entry.getLastModifiedDate();
        }
        if (lastModified != null) {
          applyLastModified(entryPath, lastModified.getTime(), options);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Decompress 7z failed: " + source, e);
    }
  }

  /**
   * RAR 解压实现。
   */
  private static void decompressRar(Path source, Path targetDir, CompressOptions options, ExtractTracker tracker) {
    // 打开 RAR 文件读取
    try (Archive archive = new Archive(source.toFile())) {
      decompressRarEntries(archive.getFileHeaders(), archive::extractFile,
        targetDir, options, tracker);
    } catch (IOException | RarException e) {
      throw new RuntimeException("Decompress rar failed: " + source, e);
    }
  }

  /**
   * RAR 条目解压器。
   */
  @FunctionalInterface
  private interface RarEntryExtractor {
    /**
     * 解压指定条目到输出流。
     */
    void extract(FileHeader header, OutputStream out) throws IOException, RarException;
  }

  /**
   * 处理 RAR 条目列表，执行解压与路径校验。
   */
  private static void decompressRarEntries(List<FileHeader> headers, RarEntryExtractor extractor, Path targetDir,
                                           CompressOptions options, ExtractTracker tracker) throws IOException, RarException {
    Path normalizedTarget = targetDir.toAbsolutePath().normalize();
    for (FileHeader header : headers) {
      // 获取条目名称并统计条目数
      String entryName = header.getFileName();
      if (entryName == null || entryName.isEmpty()) {
        continue;
      }
      if (tracker != null) {
        tracker.onEntry(entryName);
      }
      // 解析条目路径并防止路径穿越
      Path entryPath = resolveEntryPath(normalizedTarget, entryName, options);
      if (prepareEntryTarget(entryPath, header.isDirectory())) {
        continue;
      }
      try (OutputStream fileOut = newOutputStream(entryPath, options);
           OutputStream trackedOut = tracker == null ? fileOut : new TrackingOutputStream(fileOut, tracker)) {
        // 输出解压内容并进行限额统计
        try {
          extractor.extract(header, trackedOut);
        } catch (RarException e) {
          // 透传限额异常，避免被包装为 RarException
          Throwable cause = e.getCause();
          if (cause instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) cause;
          }
          throw e;
        }
      }
      // 读取并尝试恢复最后修改时间
      FileTime lastModified = header.getLastModifiedTime();
      if (lastModified != null) {
        applyLastModified(entryPath, lastModified.toMillis(), options);
      }
    }
  }

  /**
   * 单文件解压实现（GZ/BZ2/XZ）。
   */
  private static void decompressSingle(Path source, Path targetDir, Format format, CompressOptions options, ExtractTracker tracker) {
    int bufferSize = bufferSize(options);
    byte[] buffer = new byte[bufferSize];
    ensureDirectory(targetDir);
    // 计算解压后的文件名
    String targetName = resolveSingleOutputName(source.getFileName().toString(), format);
    // 解压条目数限制
    if (tracker != null) {
      tracker.onEntry(targetName);
    }
    // 生成并规范化输出文件路径
    Path targetFile = targetDir.resolve(targetName).normalize();
    try (
      InputStream fileIn = Files.newInputStream(source);
      BufferedInputStream bufferedIn = new BufferedInputStream(fileIn, bufferSize);
      // 根据格式包裹解压输入流
      InputStream decompressedIn = wrapCompressorInputStream(bufferedIn, format);
      OutputStream out = newOutputStream(targetFile, options)
    ) {
      // 写出解压内容
      copy(decompressedIn, out, buffer, tracker);
      if (decompressedIn instanceof GzipCompressorInputStream) {
        // 读取 GZ 元数据中的时间戳
        GzipCompressorInputStream gzipIn = (GzipCompressorInputStream) decompressedIn;
        // GZIP 元数据中的修改时间单位为秒
        applyLastModified(targetFile, gzipEpochSecondsToMillis(gzipIn.getMetaData().getModificationTime()), options);
      }
    } catch (IOException e) {
      throw new RuntimeException("Decompress file failed: " + source, e);
    }
  }

  /**
   * 根据单文件格式解析输出文件名。
   */
  private static String resolveSingleOutputName(String sourceName, Format format) {
    String lower = sourceName.toLowerCase(Locale.ROOT);
    switch (format) {
      case GZ:
        // 去掉 .gz 后缀
        return stripExtension(lower, sourceName, ".gz");
      case BZ2:
        // 去掉 .bz2 后缀
        return stripExtension(lower, sourceName, ".bz2");
      case XZ:
        // 去掉 .xz 后缀
        return stripExtension(lower, sourceName, ".xz");
      default:
        // 未识别格式时追加 .out
        return sourceName + ".out";
    }
  }

  /**
   * 按后缀裁剪文件名，未命中则追加 .out。
   */
  private static String stripExtension(String lowerName, String originalName, String suffix) {
    // 后缀匹配成功则截取
    if (lowerName.endsWith(suffix)) {
      String stripped = originalName.substring(0, originalName.length() - suffix.length());
      // 避免得到空名或仅 "."/".." 的非法输出名
      if (!stripped.isEmpty() && !".".equals(stripped) && !"..".equals(stripped)) {
        return stripped;
      }
    }
    // 未匹配则追加 .out
    return originalName + ".out";
  }

  /**
   * 为单文件 GZ 压缩构建参数，按选项控制是否写入源文件时间戳。
   */
  private static GzipParameters buildSingleGzipParameters(Path source, CompressOptions options) throws IOException {
    GzipParameters params = new GzipParameters();
    params.setCompressionLevel(resolveDeflaterLevel(options.getCompressionLevel()));
    if (options.isPreserveLastModified()) {
      params.setModificationTime(millisToGzipEpochSeconds(Files.getLastModifiedTime(source).toMillis()));
    } else {
      // 0 表示不写入有效修改时间
      params.setModificationTime(0L);
    }
    return params;
  }

  /**
   * GZIP 头中的时间戳为 epoch seconds，统一转换为毫秒。
   */
  private static long gzipEpochSecondsToMillis(long epochSeconds) {
    if (epochSeconds <= 0) {
      return epochSeconds;
    }
    if (epochSeconds > Long.MAX_VALUE / 1000L) {
      return Long.MAX_VALUE;
    }
    return epochSeconds * 1000L;
  }

  /**
   * 将毫秒时间戳转换为 GZIP 头使用的 epoch seconds。
   */
  private static long millisToGzipEpochSeconds(long timeMillis) {
    if (timeMillis <= 0) {
      return 0L;
    }
    long epochSeconds = timeMillis / 1000L;
    return Math.min(epochSeconds, GZIP_EPOCH_SECONDS_MAX);
  }

  /**
   * 为解压条目准备输出路径：目录条目创建目录并返回 true，文件条目创建父目录并返回 false。
   */
  private static boolean prepareEntryTarget(Path entryPath, boolean directoryEntry) {
    if (directoryEntry) {
      ensureDirectory(entryPath);
      return true;
    }
    Path parent = entryPath.getParent();
    if (parent != null) {
      ensureDirectory(parent);
    }
    return false;
  }

  /**
   * 根据格式包装输出流。
   */
  private static OutputStream wrapCompressorOutputStream(OutputStream out, Format format, CompressOptions options) throws IOException {
    switch (format) {
      case TAR_GZ:
      case GZ:
        // GZ 可配置压缩级别
        GzipParameters params = new GzipParameters();
        params.setCompressionLevel(resolveDeflaterLevel(options.getCompressionLevel()));
        return new GzipCompressorOutputStream(out, params);
      case TAR_BZ2:
      case BZ2:
        // BZ2 使用块大小（1-9）
        int level = resolveDeflaterLevel(options.getCompressionLevel());
        int blockSize = Math.max(1, Math.min(level, 9));
        return new BZip2CompressorOutputStream(out, blockSize);
      case TAR_XZ:
      case XZ:
        // XZ 使用预设等级创建输出流
        return createXzOutputStream(out, options.getCompressionLevel());
      case TAR:
        // TAR 本身不压缩
        return out;
      default:
        // 兜底：不做包装
        return out;
    }
  }

  /**
   * 创建 XZ 输出流，优先使用带预设级别的构造函数。
   */
  private static OutputStream createXzOutputStream(OutputStream out, int compressionLevel) throws IOException {
    int preset = resolveDeflaterLevel(compressionLevel);
    try {
      return XZCompressorOutputStream.class
        .getConstructor(OutputStream.class, int.class)
        .newInstance(out, preset);
    } catch (ReflectiveOperationException ignored) {
      // 回退到默认构造
      return new XZCompressorOutputStream(out);
    }
  }

  /**
   * 根据格式包装输入流。
   */
  private static InputStream wrapCompressorInputStream(InputStream in, Format format) throws IOException {
    switch (format) {
      case TAR_GZ:
      case GZ:
        return new GzipCompressorInputStream(in);
      case TAR_BZ2:
      case BZ2:
        return new BZip2CompressorInputStream(in);
      case TAR_XZ:
      case XZ:
        return new XZCompressorInputStream(in);
      case TAR:
        // TAR 本身不需要解压包装
        return in;
      default:
        // 兜底：直接返回原始流
        return in;
    }
  }

  /**
   * 标准化压缩等级到合法区间。
   */
  private static int resolveDeflaterLevel(int level) {
    // 默认压缩级别映射到 6
    if (level == Deflater.DEFAULT_COMPRESSION) {
      return 6;
    }
    // 其余值钳制到合法区间
    return Math.max(Deflater.NO_COMPRESSION, Math.min(level, Deflater.BEST_COMPRESSION));
  }

  /**
   * 获取缓冲区大小，保证最小默认值。
   */
  private static int bufferSize(CompressOptions options) {
    // 非法值回退默认大小
    return options.getBufferSize() > 0 ? options.getBufferSize() : 64 * 1024;
  }

  /**
   * 根据覆盖策略创建输出流。
   */
  private static OutputStream newOutputStream(Path target, CompressOptions options) throws IOException {
    // 根据是否覆盖选择创建策略
    OpenOption[] openOptions = options.isOverwrite()
      ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE}
      : new OpenOption[]{StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE};
    return Files.newOutputStream(target, openOptions);
  }

  /**
   * 解析条目路径，并防止目录穿越。
   */
  private static Path resolveEntryPath(Path targetDir, String entryName, CompressOptions options) {
    Path normalizedTarget = targetDir.toAbsolutePath().normalize();
    // 统一为 / 并去除绝对路径前缀
    String cleaned = entryName.replace('\\', '/');
    while (cleaned.startsWith("/")) {
      cleaned = cleaned.substring(1);
    }
    // 解析并规范化目标路径
    Path resolved = normalizedTarget.resolve(cleaned).normalize();
    // 默认禁止越界路径
    if (!options.isAllowUnsafePath() && !resolved.startsWith(normalizedTarget)) {
      throw new IllegalArgumentException("Entry is outside target dir: " + entryName);
    }
    return resolved;
  }

  /**
   * 尝试设置最后修改时间，失败则忽略。
   */
  private static void applyLastModified(Path path, long timeMillis, CompressOptions options) {
    if (!options.isPreserveLastModified() || timeMillis <= 0) {
      return;
    }
    try {
      Files.setLastModifiedTime(path, FileTime.fromMillis(timeMillis));
    } catch (IOException ignored) {
      // 忽略时间设置失败。
    }
  }

  /**
   * 遍历源路径并写入条目，处理目录、文件与软链接策略。
   */
  private static void walkSources(List<Path> sources, CompressOptions options, EntryWriter writer) throws IOException {
    // 是否跟随软链接决定遍历选项
    EnumSet<FileVisitOption> visitOptions = options.isFollowLinks()
      ? EnumSet.of(FileVisitOption.FOLLOW_LINKS)
      : EnumSet.noneOf(FileVisitOption.class);
    for (Path source : sources) {
      if (Files.isDirectory(source)) {
        // 计算相对路径基准
        Path base = resolveBaseForDirectory(source, options.isIncludeRootDir());
        Files.walkFileTree(source, visitOptions, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
          @SuppressWarnings("NullableProblems")
          @Override
          public FileVisitResult preVisitDirectory(@NonNull Path dir, @NonNull BasicFileAttributes attrs) throws IOException {
            // 不包含根目录时跳过源目录本身
            if (!options.isIncludeRootDir() && dir.equals(source)) {
              return FileVisitResult.CONTINUE;
            }
            String entryName = toEntryName(base, dir);
            if (!entryName.isEmpty()) {
              writer.writeDirectory(dir, entryName);
            }
            return FileVisitResult.CONTINUE;
          }

          @SuppressWarnings("NullableProblems")
          @Override
          public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
            // 不跟随软链接时跳过
            if (!options.isFollowLinks() && Files.isSymbolicLink(file)) {
              return FileVisitResult.CONTINUE;
            }
            String entryName = toEntryName(base, file);
            if (!entryName.isEmpty()) {
              writer.writeFile(file, entryName);
            }
            return FileVisitResult.CONTINUE;
          }
        });
      } else {
        // 单文件源也需要考虑软链接策略
        if (!options.isFollowLinks() && Files.isSymbolicLink(source)) {
          continue;
        }
        String entryName = source.getFileName().toString();
        writer.writeFile(source, entryName);
      }
    }
  }

  /**
   * 根据是否包含根目录确定相对路径基准。
   */
  private static Path resolveBaseForDirectory(Path source, boolean includeRootDir) {
    if (includeRootDir) {
      // 基准设为父目录，确保根目录也作为条目写入
      Path parent = source.getParent();
      return parent == null ? source : parent;
    }
    // 不包含根目录时以自身为基准
    return source;
  }

  /**
   * 将实际路径转换为归档条目名。
   */
  private static String toEntryName(Path base, Path path) {
    if (base == null) {
      return safeFileName(path);
    }
    // 计算相对路径作为条目名
    Path relative = base.relativize(path);
    String name = relative.toString();
    if (name.isEmpty()) {
      // 相对路径为空时回退到文件名
      return safeFileName(path);
    }
    // 归档中统一使用 /
    return name.replace('\\', '/');
  }

  /**
   * 获取安全的文件名字符串。
   */
  private static String safeFileName(Path path) {
    Path fileName = path.getFileName();
    // 可能为 null（如根路径）
    return fileName == null ? "" : fileName.toString();
  }

  /**
   * 复制输入流到输出流，并进行解压大小统计。
   */
  private static void copy(InputStream in, OutputStream out, byte[] buffer, ExtractTracker tracker) throws IOException {
    long total = 0;
    int read;
    while ((read = in.read(buffer)) != -1) {
      if (tracker != null) {
        // 解压过程计数与限额检查
        tracker.onBytes(read, total + read);
      }
      out.write(buffer, 0, read);
      total += read;
    }
  }

  /**
   * 复制输入流到 7z 输出流。
   */
  private static void copy(InputStream in, SevenZOutputFile out, byte[] buffer) throws IOException {
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }

  /**
   * 从 7z 文件读取并写出，同时进行解压大小统计。
   */
  private static void copy(SevenZFile in, OutputStream out, byte[] buffer, ExtractTracker tracker) throws IOException {
    long total = 0;
    int read;
    while ((read = in.read(buffer)) != -1) {
      if (read == 0) {
        continue;
      }
      if (tracker != null) {
        // 解压过程计数与限额检查
        tracker.onBytes(read, total + read);
      }
      out.write(buffer, 0, read);
      total += read;
    }
  }

  /**
   * 归档条目写入抽象。
   */
  private interface EntryWriter {
    /**
     * 写入目录条目。
     */
    void writeDirectory(Path dir, String entryName) throws IOException;

    /**
     * 写入文件条目。
     */
    void writeFile(Path file, String entryName) throws IOException;
  }

  /**
   * 解压时统计输出大小的包装流，便于触发限额校验。
   */
  private static final class TrackingOutputStream extends OutputStream {
    // 实际输出流
    private final OutputStream delegate;
    // 限额跟踪器
    private final ExtractTracker tracker;
    // 当前条目已写入大小
    private long entrySize;

    // 绑定输出流与追踪器
    private TrackingOutputStream(OutputStream delegate, ExtractTracker tracker) {
      this.delegate = delegate;
      this.tracker = tracker;
    }

    @Override
    public void write(int b) throws IOException {
      // 单字节写入也要计入限额
      onBytes(1);
      delegate.write(b);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
      // 批量写入前先触发限额检查
      onBytes(len);
      delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      // 透传 flush
      delegate.flush();
    }

    @Override
    public void close() throws IOException {
      // 透传 close
      delegate.close();
    }

    /**
     * 累计条目大小并触发限额校验。
     */
    private void onBytes(int len) {
      if (len <= 0) {
        return;
      }
      long nextSize = entrySize + len;
      tracker.onBytes(len, nextSize);
      entrySize = nextSize;
    }
  }

  /**
   * 解压限额追踪器，防止 Zip Bomb 等异常数据。
   */
  private static class ExtractTracker {
    // 允许的最大条目数
    private final long maxEntries;
    // 允许的最大解压总大小
    private final long maxTotalSize;
    // 允许的单条目最大大小
    private final long maxEntrySize;
    // 当前已处理条目数
    private long entryCount;
    // 当前累计解压大小
    private long totalSize;

    /**
     * 使用限额创建追踪器。
     */
    private ExtractTracker(long maxEntries, long maxTotalSize, long maxEntrySize) {
      this.maxEntries = maxEntries;
      this.maxTotalSize = maxTotalSize;
      this.maxEntrySize = maxEntrySize;
    }

    /**
     * 从选项创建追踪器，无限制则返回 null。
     */
    static ExtractTracker fromOptions(CompressOptions options) {
      // 所有限额均未设置时返回 null
      if (options.getMaxEntries() <= 0 && options.getMaxTotalSize() <= 0 && options.getMaxEntrySize() <= 0) {
        return null;
      }
      return new ExtractTracker(options.getMaxEntries(), options.getMaxTotalSize(), options.getMaxEntrySize());
    }

    /**
     * 记录条目数并进行限制检查。
     */
    void onEntry(String name) {
      // 限制条目数量
      if (maxEntries > 0 && ++entryCount > maxEntries) {
        throw new IllegalArgumentException("Too many entries: " + name);
      }
    }

    /**
     * 记录解压字节并进行大小限制检查。
     */
    void onBytes(long bytesRead, long entrySize) {
      // 限制单条目大小
      if (maxEntrySize > 0 && entrySize > maxEntrySize) {
        throw new IllegalArgumentException("Entry size exceeds limit: " + entrySize);
      }
      // 限制总解压大小
      if (maxTotalSize > 0 && totalSize + bytesRead > maxTotalSize) {
        throw new IllegalArgumentException("Total size exceeds limit: " + (totalSize + bytesRead));
      }
      // 累加总大小
      totalSize += bytesRead;
    }
  }
}
