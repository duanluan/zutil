package top.csaf.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

/**
 * {@link CompressUtil}的压缩与解压配置。
 * <p>
 * 该配置对象覆盖了流复制缓冲区、条目名编码、压缩级别、覆盖策略、
 * 目录遍历策略以及解压限额等核心行为。
 * <p>
 * 其中`maxEntries`、`maxTotalSize`与`maxEntrySize`用于控制解压阶段的资源消耗，
 * 可用于降低压缩炸弹等异常输入带来的风险。上述限额为`0`时表示不限制。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompressOptions {
  /**
   * 流复制缓冲区大小（单位：字节）。
   * 默认值为`64 * 1024`（64KB）。
   */
  @Builder.Default
  private int bufferSize = 64 * 1024;

  /**
   * 归档条目名编码。
   * 默认值为`UTF-8`。
   */
  @Builder.Default
  private Charset charset = StandardCharsets.UTF_8;

  /**
   * Deflate系压缩级别。
   * 默认值为`Deflater.BEST_SPEED`。
   */
  @Builder.Default
  private int compressionLevel = Deflater.BEST_SPEED;

  /**
   * 是否允许覆盖已存在的目标文件。
   * 默认值为`true`。
   */
  @Builder.Default
  private boolean overwrite = true;

  /**
   * 压缩目录时是否包含根目录自身。
   * 默认值为`true`。
   */
  @Builder.Default
  private boolean includeRootDir = true;

  /**
   * 是否保留最后修改时间。
   * 默认值为`true`。
   */
  @Builder.Default
  private boolean preserveLastModified = true;

  /**
   * 遍历目录时是否跟随符号链接。
   * 默认值为`false`。
   */
  @Builder.Default
  private boolean followLinks = false;

  /**
   * 解压时是否允许不安全条目路径（例如目录穿越路径）。
   * 默认值为`false`。
   */
  @Builder.Default
  private boolean allowUnsafePath = false;

  /**
   * 解压条目数量上限。
   * 默认值为`0`，表示不限制。
   */
  @Builder.Default
  private long maxEntries = 0;

  /**
   * 解压总字节数上限。
   * 默认值为`0`，表示不限制。
   */
  @Builder.Default
  private long maxTotalSize = 0;

  /**
   * 单个条目的解压字节数上限。
   * 默认值为`0`，表示不限制。
   */
  @Builder.Default
  private long maxEntrySize = 0;
}
