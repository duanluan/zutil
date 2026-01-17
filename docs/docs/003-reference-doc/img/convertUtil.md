# ConvertUtil 图片转换工具类

> 📦 **包路径**：`top.csaf.img.ConvertUtil`
>
> 🔗 **所属模块**：`zutil-img`

**ConvertUtil** 是一个专注于图片格式转换的工具类，基于 **Apache Batik** 实现。
核心功能是将 **SVG** 矢量图转换为常见的位图格式（PNG、JPG、TIFF），并支持设置分辨率（DPI）和尺寸。针对 JPG 格式，还提供了基于文件大小限制的自动压缩功能。

## ✨ 核心特性

* **SVG 转位图**：支持将 SVG 转换为 `PNG`、`JPG/JPEG`、`TIFF/TIF` 格式。
* **高保真控制**：支持自定义输出图片的 **宽度**、**高度** 和 **DPI**（每英寸像素数）。
* **智能压缩**：在 SVG 转 JPG 时，支持设置最大文件大小限制，若超出限制自动调用 `ThumbnailUtil` 进行压缩。

## 🚀 常用方法概览

### 1. 通用 SVG 转换 (svg2img)

将 SVG 文件转换为指定格式的图片。转换格式由 `targetImgPath` 的后缀名决定。

| 参数名             | 类型       | 描述                                                       |
|:----------------|:---------|:---------------------------------------------------------|
| `svgPath`       | `String` | 源 SVG 文件路径。                                              |
| `targetImgPath` | `String` | 目标图片路径（后缀决定格式：`.png`, `.jpg`, `.jpeg`, `.tiff`, `.tif`）。 |
| `pngWidth`      | `float`  | 目标图片宽度。                                                  |
| `pngHeight`     | `float`  | 目标图片高度。                                                  |
| `dpi`           | `float`  | 像素密度 (Dots Per Inch)。通常屏幕为 72 或 96，打印为 300。              |

* **返回值**：`long` - 生成的文件大小（字节）。若失败返回 `-1`。
* **支持格式**:
    * **PNG**：`PNGTranscoder`
    * **JPG/JPEG**：`JPEGTranscoder` (默认质量 1.0)
    * **TIFF/TIF**：`TIFFTranscoder`

**示例代码**：

```java
// 将 svg 转为 png，宽 100，高 100，dpi 96
long size = ConvertUtil.svg2img("icon.svg", "icon.png", 100f, 100f, 96f);
```

### 2. SVG 转 JPG 带压缩 (svg2jpg)

专门用于 SVG 转 JPG 的增强方法，支持文件大小限制。

| 参数名              | 类型       | 描述                                 |
|:-----------------|:---------|:-----------------------------------|
| `svgPath`        | `String` | 源 SVG 文件路径。                        |
| `jpgPath`        | `String` | 目标 JPG 文件路径（若无后缀会自动添加 `.jpg`）。     |
| `jpgWidth`       | `float`  | 目标图片宽度。                            |
| `jpgHeight`      | `float`  | 目标图片高度。                            |
| `dpi`            | `float`  | 像素密度。                              |
| `maxJpgFileSize` | `long`   | 最大文件大小限制（单位：KB）。若生成的文件超过此大小，会进行压缩。 |

* **返回值**：`long` - 最终生成的文件大小（字节）。
* **逻辑**:
    1.  先调用 `svg2img` 生成高质量 JPG。
    2.  检查生成的文件大小是否超过 `maxJpgFileSize`。
    3.  若超过，调用 `ThumbnailUtil` 进行压缩（压缩质量基准为 0.9）。

**示例代码**：

```java
// 转为 jpg，限制最大为 200KB
long size = ConvertUtil.svg2jpg(
    "chart.svg", 
    "chart.jpg", 
    800f, 
    600f, 
    300f, 
    200L // max size 200kb
);
```

## ⚠️ 注意事项

1. **依赖库**：本工具类依赖 `org.apache.batik` 相关包进行 SVG 解析与渲染。
2. **异常处理**：转换过程中的 `IOException` 或 `TranscoderException` 会被捕获并打印 Error 日志，方法返回 `-1`，不会抛出受检异常。
3. **覆盖行为**：如果目标路径 `targetImgPath` 已存在文件，将会被**直接覆盖**。
