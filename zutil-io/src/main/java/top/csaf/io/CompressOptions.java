package top.csaf.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

/**
 * Compression options for {@link CompressUtil}.
 * CompressUtil 的压缩/解压配置项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompressOptions {
  /**
   * Buffer size for stream copy.
   * 流拷贝的缓冲区大小，默认 64KB。
   */
  @Builder.Default
  private int bufferSize = 64 * 1024; // 默认 64KB
  /**
   * Entry name encoding for archives.
   * 归档条目名编码，默认 UTF-8。
   */
  @Builder.Default
  private Charset charset = StandardCharsets.UTF_8; // 默认 UTF-8
  /**
   * Compression level for deflate-based formats.
   * Deflate 系列压缩级别，默认 BEST_SPEED。
   */
  @Builder.Default
  private int compressionLevel = Deflater.BEST_SPEED; // 默认 BEST_SPEED
  /**
   * Whether to overwrite target files.
   * 是否覆盖目标文件，默认 true。
   */
  @Builder.Default
  private boolean overwrite = true; // 默认允许覆盖
  /**
   * Whether to include the root directory when compressing a directory.
   * 压缩目录时是否包含根目录，默认 true。
   */
  @Builder.Default
  private boolean includeRootDir = true; // 默认包含根目录
  /**
   * Whether to keep last modified time.
   * 是否保留最后修改时间，默认 true。
   */
  @Builder.Default
  private boolean preserveLastModified = true; // 默认保留时间戳
  /**
   * Whether to follow symbolic links when walking directories.
   * 遍历目录时是否跟随软链接，默认 false。
   */
  @Builder.Default
  private boolean followLinks = false; // 默认不跟随软链接
  /**
   * Whether to allow unsafe entry paths during extraction.
   * 解压时是否允许不安全路径，默认 false。
   */
  @Builder.Default
  private boolean allowUnsafePath = false; // 默认不允许越界路径
  /**
   * Max number of entries to extract, 0 means unlimited.
   * 解压条目数上限，0 表示不限制。
   */
  @Builder.Default
  private long maxEntries = 0; // 0 表示不限制
  /**
   * Max total extracted size in bytes, 0 means unlimited.
   * 解压总大小上限（字节），0 表示不限制。
   */
  @Builder.Default
  private long maxTotalSize = 0; // 0 表示不限制
  /**
   * Max size per extracted entry in bytes, 0 means unlimited.
   * 单条目解压大小上限（字节），0 表示不限制。
   */
  @Builder.Default
  private long maxEntrySize = 0; // 0 表示不限制
}
