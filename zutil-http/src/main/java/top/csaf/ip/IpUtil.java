package top.csaf.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.lionsoul.ip2region.xdb.Searcher;
import top.csaf.lang.StrUtil;

import java.io.Closeable;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * IP 工具类，提供校验、转换、范围判断及 ip2region 查询能力。
 */
public class IpUtil {

  private static final int IPV4_SEGMENT_COUNT = 4;
  private static final int IPV4_BIT_LENGTH = 32;
  private static final int IPV6_BIT_LENGTH = 128;
  private static final long IPV4_MAX = 0xFFFFFFFFL;
  private static final int REGION_PARTS = 5;
  private static final String REGION_SPLIT_REGEX = "\\|";

  /**
   * 判断是否为合法 IP（IPv4 或 IPv6）。
   *
   * @param ip IP
   * @return 是否合法
   */
  public static boolean isValidIp(String ip) {
    return isValidIpv4(ip) || isValidIpv6(ip);
  }

  /**
   * 判断是否为合法 IPv4 地址（点分十进制）。
   *
   * @param ip IPv4
   * @return 是否合法
   */
  public static boolean isValidIpv4(String ip) {
    if (StrUtil.isBlank(ip)) {
      return false;
    }
    String[] parts = ip.split("\\.", -1);
    if (parts.length != IPV4_SEGMENT_COUNT) {
      return false;
    }
    // 逐段校验，拒绝空段、非数字或超范围的值。
    for (String part : parts) {
      if (part.isEmpty() || part.length() > 3) {
        return false;
      }
      int value = 0;
      for (int i = 0; i < part.length(); i++) {
        char c = part.charAt(i);
        if (c < '0' || c > '9') {
          return false;
        }
        value = value * 10 + (c - '0');
      }
      if (value > 255) {
        return false;
      }
    }
    return true;
  }

  /**
   * 判断是否为合法 IPv6 地址。
   *
   * @param ip IPv6
   * @return 是否合法
   */
  public static boolean isValidIpv6(String ip) {
    if (StrUtil.isBlank(ip)) {
      return false;
    }
    // 兼容形如 [::1] 的括号包裹形式。
    String candidate = stripBrackets(ip);
    if (!candidate.contains(":")) {
      return false;
    }
    try {
      return InetAddress.getByName(candidate) instanceof Inet6Address;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 判断是否为 IPv4。
   *
   * @param ip IP
   * @return 是否为 IPv4
   */
  public static boolean isIpv4(String ip) {
    return isValidIpv4(ip);
  }

  /**
   * 判断是否为 IPv6。
   *
   * @param ip IP
   * @return 是否为 IPv6
   */
  public static boolean isIpv6(String ip) {
    return isValidIpv6(ip);
  }

  /**
   * 规范化 IP 表达（使用 Java 标准格式）。
   *
   * @param ip IP
   * @return 规范化后的 IP，非法时返回空串
   */
  public static String normalizeIp(String ip) {
    InetAddress address = toInetAddressOrNull(ip);
    return address == null ? "" : address.getHostAddress();
  }

  /**
   * IPv4 转无符号 long。
   *
   * @param ip IPv4
   * @return 无符号 long
   */
  public static long ipv4ToLong(@NonNull String ip) {
    int[] parts = parseIpv4Parts(ip);
    // 按网络字节序拼接为无符号 long。
    return ((long) parts[0] << 24)
      | ((long) parts[1] << 16)
      | ((long) parts[2] << 8)
      | (long) parts[3];
  }

  /**
   * 无符号 long 转 IPv4。
   *
   * @param value 无符号 long
   * @return IPv4
   */
  public static String longToIpv4(long value) {
    if (value < 0 || value > IPV4_MAX) {
      throw new IllegalArgumentException("Value: should be between 0 and 4294967295");
    }
    return ((value >> 24) & 0xFF) + "." +
      ((value >> 16) & 0xFF) + "." +
      ((value >> 8) & 0xFF) + "." +
      (value & 0xFF);
  }

  /**
   * IP 转字节数组。
   *
   * @param ip IP
   * @return 字节数组
   */
  public static byte[] ipToBytes(@NonNull String ip) {
    if (!isValidIp(ip)) {
      throw new IllegalArgumentException("Ip: should be a valid IP");
    }
    try {
      // 交由 InetAddress 解析，兼容 IPv4/IPv6。
      return InetAddress.getByName(stripBrackets(ip)).getAddress();
    } catch (Exception e) {
      throw new RuntimeException("Ip: parse failed", e);
    }
  }

  /**
   * IP 转字节数组（非法时返回 null）。
   *
   * @param ip IP
   * @return 字节数组或 null
   */
  public static byte[] ipToBytesOrNull(String ip) {
    if (!isValidIp(ip)) {
      return null;
    }
    try {
      return InetAddress.getByName(stripBrackets(ip)).getAddress();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * IP 转 InetAddress。
   *
   * @param ip IP
   * @return InetAddress
   */
  public static InetAddress toInetAddress(@NonNull String ip) {
    if (!isValidIp(ip)) {
      throw new IllegalArgumentException("Ip: should be a valid IP");
    }
    try {
      return InetAddress.getByName(stripBrackets(ip));
    } catch (Exception e) {
      throw new RuntimeException("Ip: parse failed", e);
    }
  }

  /**
   * 判断是否为回环地址。
   *
   * @param ip IP
   * @return 是否为回环地址
   */
  public static boolean isLoopback(String ip) {
    InetAddress address = toInetAddressOrNull(ip);
    return address != null && address.isLoopbackAddress();
  }

  /**
   * 判断是否为本机任意地址（0.0.0.0 或 ::）。
   *
   * @param ip IP
   * @return 是否为任意地址
   */
  public static boolean isAnyLocal(String ip) {
    InetAddress address = toInetAddressOrNull(ip);
    return address != null && address.isAnyLocalAddress();
  }

  /**
   * 判断是否为链路本地地址。
   *
   * @param ip IP
   * @return 是否为链路本地地址
   */
  public static boolean isLinkLocal(String ip) {
    InetAddress address = toInetAddressOrNull(ip);
    return address != null && address.isLinkLocalAddress();
  }

  /**
   * 判断是否为站点本地地址。
   *
   * @param ip IP
   * @return 是否为站点本地地址
   */
  public static boolean isSiteLocal(String ip) {
    InetAddress address = toInetAddressOrNull(ip);
    return address != null && address.isSiteLocalAddress();
  }

  /**
   * 判断是否为组播地址。
   *
   * @param ip IP
   * @return 是否为组播地址
   */
  public static boolean isMulticast(String ip) {
    InetAddress address = toInetAddressOrNull(ip);
    return address != null && address.isMulticastAddress();
  }

  /**
   * 判断是否为 IPv4 广播地址。
   *
   * @param ip IPv4
   * @return 是否为广播地址
   */
  public static boolean isBroadcastIpv4(String ip) {
    return isValidIpv4(ip) && "255.255.255.255".equals(ip);
  }

  /**
   * 判断是否为私有 IPv4（RFC1918）。
   *
   * @param ip IPv4
   * @return 是否为私有地址
   */
  public static boolean isPrivateIpv4(String ip) {
    if (!isValidIpv4(ip)) {
      return false;
    }
    return isInCidr(ip, "10.0.0.0/8")
      || isInCidr(ip, "172.16.0.0/12")
      || isInCidr(ip, "192.168.0.0/16");
  }

  /**
   * 判断是否为运营商 NAT 地址（100.64.0.0/10）。
   *
   * @param ip IPv4
   * @return 是否为运营商 NAT 地址
   */
  public static boolean isCarrierNatIpv4(String ip) {
    return isValidIpv4(ip) && isInCidr(ip, "100.64.0.0/10");
  }

  /**
   * 判断是否为保留 IPv4 地址段。
   *
   * @param ip IPv4
   * @return 是否为保留地址段
   */
  public static boolean isReservedIpv4(String ip) {
    if (!isValidIpv4(ip)) {
      return false;
    }
    return isInCidr(ip, "0.0.0.0/8")
      || isInCidr(ip, "240.0.0.0/4");
  }

  /**
   * 判断是否为公网 IPv4。
   *
   * @param ip IPv4
   * @return 是否为公网 IPv4
   */
  public static boolean isPublicIpv4(String ip) {
    if (!isValidIpv4(ip)) {
      return false;
    }
    return !isPrivateIpv4(ip)
      && !isCarrierNatIpv4(ip)
      && !isLoopback(ip)
      && !isLinkLocal(ip)
      && !isAnyLocal(ip)
      && !isMulticast(ip)
      && !isBroadcastIpv4(ip)
      && !isReservedIpv4(ip);
  }

  /**
   * 判断 IP 是否在指定范围内。
   *
   * @param ip      IP
   * @param startIp 起始 IP
   * @param endIp   结束 IP
   * @return 是否在范围内
   */
  public static boolean isInRange(@NonNull String ip, @NonNull String startIp, @NonNull String endIp) {
    if (isValidIpv4(ip) && isValidIpv4(startIp) && isValidIpv4(endIp)) {
      long ipVal = ipv4ToLong(ip);
      long startVal = ipv4ToLong(startIp);
      long endVal = ipv4ToLong(endIp);
      if (startVal > endVal) {
        throw new IllegalArgumentException("StartIp: should be less than or equal to EndIp");
      }
      return ipVal >= startVal && ipVal <= endVal;
    }
    if (isValidIpv6(ip) && isValidIpv6(startIp) && isValidIpv6(endIp)) {
      BigInteger ipVal = ipToBigInteger(ipToBytes(ip));
      BigInteger startVal = ipToBigInteger(ipToBytes(startIp));
      BigInteger endVal = ipToBigInteger(ipToBytes(endIp));
      if (startVal.compareTo(endVal) > 0) {
        throw new IllegalArgumentException("StartIp: should be less than or equal to EndIp");
      }
      return ipVal.compareTo(startVal) >= 0 && ipVal.compareTo(endVal) <= 0;
    }
    throw new IllegalArgumentException("Ip: should be valid and same IP version");
  }

  /**
   * 判断 IP 是否在 CIDR 段内。
   *
   * @param ip   IP
   * @param cidr CIDR（如 192.168.1.0/24）
   * @return 是否在 CIDR 段内
   */
  public static boolean isInCidr(@NonNull String ip, @NonNull String cidr) {
    CidrInfo cidrInfo = parseCidr(cidr);
    if (cidrInfo.ipv4) {
      if (!isValidIpv4(ip)) {
        return false;
      }
      long ipVal = ipv4ToLong(ip);
      long baseVal = ipv4ToLong(cidrInfo.baseIp);
      long mask = prefixToMaskLong(cidrInfo.prefix);
      return (ipVal & mask) == (baseVal & mask);
    }
    if (!isValidIpv6(ip)) {
      return false;
    }
    // IPv6 使用前缀长度进行高位匹配。
    BigInteger ipVal = ipToBigInteger(ipToBytes(ip));
    BigInteger baseVal = ipToBigInteger(ipToBytes(cidrInfo.baseIp));
    int shift = IPV6_BIT_LENGTH - cidrInfo.prefix;
    return ipVal.shiftRight(shift).equals(baseVal.shiftRight(shift));
  }

  /**
   * 计算 IPv4 网络地址。
   *
   * @param ip     IPv4
   * @param prefix 前缀长度
   * @return 网络地址
   */
  public static String getNetworkAddress(@NonNull String ip, int prefix) {
    ensureIpv4Prefix(prefix);
    ensureIpv4(ip);
    long ipVal = ipv4ToLong(ip);
    long mask = prefixToMaskLong(prefix);
    return longToIpv4(ipVal & mask);
  }

  /**
   * 计算 IPv4 广播地址。
   *
   * @param ip     IPv4
   * @param prefix 前缀长度
   * @return 广播地址
   */
  public static String getBroadcastAddress(@NonNull String ip, int prefix) {
    ensureIpv4Prefix(prefix);
    ensureIpv4(ip);
    long ipVal = ipv4ToLong(ip);
    long mask = prefixToMaskLong(prefix);
    long broadcast = ipVal | (~mask & IPV4_MAX);
    return longToIpv4(broadcast);
  }

  /**
   * 计算 IPv4 CIDR 的起止范围。
   *
   * @param cidr CIDR
   * @return [network, broadcast]
   */
  public static String[] getIpv4Range(@NonNull String cidr) {
    CidrInfo cidrInfo = parseCidr(cidr);
    if (!cidrInfo.ipv4) {
      throw new IllegalArgumentException("Cidr: should be IPv4");
    }
    String network = getNetworkAddress(cidrInfo.baseIp, cidrInfo.prefix);
    String broadcast = getBroadcastAddress(cidrInfo.baseIp, cidrInfo.prefix);
    return new String[]{network, broadcast};
  }

  /**
   * 前缀长度转 IPv4 掩码。
   *
   * @param prefix 前缀长度
   * @return 掩码
   */
  public static String prefixToMask(int prefix) {
    ensureIpv4Prefix(prefix);
    return longToIpv4(prefixToMaskLong(prefix));
  }

  /**
   * IPv4 掩码转前缀长度。
   *
   * @param mask 掩码
   * @return 前缀长度
   */
  public static int maskToPrefix(@NonNull String mask) {
    ensureIpv4(mask);
    long maskVal = ipv4ToLong(mask);
    int prefix = 0;
    boolean zeroSeen = false;
    for (int i = IPV4_BIT_LENGTH - 1; i >= 0; i--) {
      boolean bit = ((maskVal >> i) & 1) == 1;
      // 掩码必须连续：出现 0 后再遇到 1 即非法。
      if (bit && zeroSeen) {
        throw new IllegalArgumentException("Mask: should be contiguous");
      }
      if (bit) {
        prefix++;
      } else {
        zeroSeen = true;
      }
    }
    if (prefixToMaskLong(prefix) != maskVal) {
      throw new IllegalArgumentException("Mask: should be contiguous");
    }
    return prefix;
  }

  /**
   * 获取本机主机名。
   *
   * @return 主机名
   */
  public static String getLocalHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      throw new RuntimeException("Get local host name failed", e);
    }
  }

  /**
   * 获取本机主机地址。
   *
   * @return 主机地址
   */
  public static String getLocalHostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      throw new RuntimeException("Get local host address failed", e);
    }
  }

  /**
   * 获取本机 IPv4 列表（默认排除回环）。
   *
   * @return IPv4 列表
   */
  public static List<String> getLocalIpv4s() {
    return getLocalIps(false, false);
  }

  /**
   * 获取本机 IPv4 列表。
   *
   * @param includeLoopback 是否包含回环
   * @return IPv4 列表
   */
  public static List<String> getLocalIpv4s(boolean includeLoopback) {
    return getLocalIps(includeLoopback, false);
  }

  /**
   * 获取本机 IPv6 列表（默认排除回环）。
   *
   * @return IPv6 列表
   */
  public static List<String> getLocalIpv6s() {
    return getLocalIps(false, true);
  }

  /**
   * 获取本机 IPv6 列表。
   *
   * @param includeLoopback 是否包含回环
   * @return IPv6 列表
   */
  public static List<String> getLocalIpv6s(boolean includeLoopback) {
    return getLocalIps(includeLoopback, true);
  }

  /**
   * 获取本机 IP 列表。
   *
   * @param includeLoopback 是否包含回环
   * @param ipv6Only        是否仅 IPv6
   * @return IP 列表
   */
  public static List<String> getLocalIps(boolean includeLoopback, boolean ipv6Only) {
    List<InetAddress> addresses = collectLocalAddresses(includeLoopback, ipv6Only);
    if (addresses.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    for (InetAddress address : addresses) {
      result.add(address.getHostAddress());
    }
    return result;
  }

  /**
   * 基于 ip2region 查询区域（需要先创建搜索器）。
   *
   * @param searcher 搜索器
   * @param ip       IPv4
   * @return 区域字符串
   */
  public static String searchRegion(@NonNull RegionSearcher searcher, @NonNull String ip) {
    if (!isValidIpv4(ip)) {
      throw new IllegalArgumentException("Ip: should be a valid IPv4");
    }
    String region = searcher.search(ip);
    // searcher 可能返回 null，统一为空串表示未知。
    return region == null ? "" : region;
  }

  /**
   * 基于 ip2region 查询区域（需要先创建搜索器）。
   *
   * @param searcher 搜索器
   * @param ip       IPv4
   * @return 区域信息
   */
  public static RegionInfo searchRegionInfo(@NonNull RegionSearcher searcher, @NonNull String ip) {
    return parseRegion(searchRegion(searcher, ip));
  }

  /**
   * 通过 ip2region 文件路径查询区域。
   *
   * @param dbPath ip2region 数据库路径
   * @param ip     IPv4
   * @return 区域字符串
   */
  public static String searchRegion(@NonNull String dbPath, @NonNull String ip) {
    try (Ip2RegionSearcher searcher = Ip2RegionSearcher.newWithFileOnly(dbPath)) {
      return searchRegion(searcher, ip);
    }
  }

  /**
   * 通过 ip2region 文件路径查询区域。
   *
   * @param dbPath ip2region 数据库路径
   * @param ip     IPv4
   * @return 区域信息
   */
  public static RegionInfo searchRegionInfo(@NonNull String dbPath, @NonNull String ip) {
    return parseRegion(searchRegion(dbPath, ip));
  }

  /**
   * 拆分 ip2region 区域字符串。
   *
   * @param region 区域字符串
   * @return 长度固定为 5 的数组
   */
  public static String[] splitRegion(String region) {
    String[] result = new String[REGION_PARTS];
    if (StrUtil.isBlank(region)) {
      Arrays.fill(result, "");
      return result;
    }
    String[] parts = region.split(REGION_SPLIT_REGEX, -1);
    // 固定长度 5，缺失部分补空，并规整 "0" 为 ""。
    for (int i = 0; i < REGION_PARTS; i++) {
      String value = i < parts.length ? parts[i] : "";
      result[i] = normalizeRegionItem(value);
    }
    return result;
  }

  /**
   * 解析 ip2region 区域字符串。
   *
   * @param region 区域字符串
   * @return 区域信息对象
   */
  public static RegionInfo parseRegion(String region) {
    String[] parts = splitRegion(region);
    String raw = region == null ? "" : region;
    return new RegionInfo(parts[0], parts[1], parts[2], parts[3], parts[4], raw);
  }

  /**
   * ip2region 搜索器接口。
   */
  @FunctionalInterface
  public interface RegionSearcher {
    /**
     * 查询区域信息。
     *
     * @param ip IPv4
     * @return 区域字符串
     */
    String search(String ip);
  }

  /**
   * ip2region 搜索器适配。
   */
  public static class Ip2RegionSearcher implements RegionSearcher, Closeable {

    private final Searcher searcher;

    private Ip2RegionSearcher(Searcher searcher) {
      this.searcher = searcher;
    }

    /**
     * 仅使用文件模式创建搜索器。
     *
     * @param dbPath 数据库路径
     * @return 搜索器
     */
    public static Ip2RegionSearcher newWithFileOnly(@NonNull String dbPath) {
      if (StrUtil.isBlank(dbPath)) {
        throw new IllegalArgumentException("DbPath: should not be blank");
      }
      try {
        return new Ip2RegionSearcher(Searcher.newWithFileOnly(dbPath));
      } catch (Exception e) {
        throw new RuntimeException("Init ip2region searcher failed", e);
      }
    }

    /**
     * 使用内存数据创建搜索器。
     *
     * @param content 数据库内容
     * @return 搜索器
     */
    public static Ip2RegionSearcher newWithBuffer(byte[] content) {
      if (content == null) {
        throw new IllegalArgumentException("Content: should not be null");
      }
      try {
        return new Ip2RegionSearcher(Searcher.newWithBuffer(content));
      } catch (Exception e) {
        throw new RuntimeException("Init ip2region searcher failed", e);
      }
    }

    /**
     * 查询区域字符串。
     *
     * @param ip IPv4
     * @return 区域字符串
     */
    @Override
    public String search(@NonNull String ip) {
      if (!isValidIpv4(ip)) {
        throw new IllegalArgumentException("Ip: should be a valid IPv4");
      }
      try {
        return searcher.search(ip);
      } catch (Exception e) {
        throw new RuntimeException("Search ip2region failed", e);
      }
    }

    /**
     * 释放资源。
     */
    @Override
    public void close() {
      try {
        searcher.close();
      } catch (Exception e) {
        throw new RuntimeException("Close ip2region searcher failed", e);
      }
    }
  }

  /**
   * 区域信息对象。
   */
  @AllArgsConstructor
  @Getter
  public static final class RegionInfo {
    private final String country;
    private final String region;
    private final String province;
    private final String city;
    private final String isp;
    private final String raw;

    /**
     * 判断是否为未知区域。
     *
     * @return 是否未知
     */
    public boolean isUnknown() {
      return StrUtil.isAllBlank(country, region, province, city, isp);
    }

    @Override
    public String toString() {
      // 优先保留原始区域串，避免二次拼接改变格式。
      if (StrUtil.isBlank(raw)) {
        return String.join("|", country, region, province, city, isp);
      }
      return raw;
    }
  }

  /**
   * 去掉 IPv6 方括号，便于 InetAddress 解析。
   *
   * @param ip 原始 IP（可能是 [::1]）
   * @return 去括号后的 IP；null 时返回空串
   */
  private static String stripBrackets(String ip) {
    if (ip == null) {
      return "";
    }
    // 处理 URI 形式 IPv6：[::1] -> ::1。
    if (ip.length() > 2 && ip.startsWith("[") && ip.endsWith("]")) {
      return ip.substring(1, ip.length() - 1);
    }
    return ip;
  }

  /**
   * 尝试解析 IP，失败或非法时返回 null，供容错分支使用。
   *
   * @param ip IP
   * @return 解析成功的 InetAddress 或 null
   */
  private static InetAddress toInetAddressOrNull(String ip) {
    // 非法 IP 直接返回 null，避免上层判断抛异常。
    if (isValidIp(ip)) {
      try {
        return InetAddress.getByName(stripBrackets(ip));
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  /**
   * 将 IPv4 拆分成 4 段并转为 int 数组。
   *
   * @param ip 已通过合法性校验的 IPv4
   * @return 长度为 4 的段值数组
   */
  private static int[] parseIpv4Parts(String ip) {
    if (!isValidIpv4(ip)) {
      throw new IllegalArgumentException("Ip: should be a valid IPv4");
    }
    String[] parts = ip.split("\\.", -1);
    int[] result = new int[IPV4_SEGMENT_COUNT];
    for (int i = 0; i < IPV4_SEGMENT_COUNT; i++) {
      result[i] = Integer.parseInt(parts[i]);
    }
    return result;
  }

  /**
   * 将字节数组转为无符号 BigInteger，便于 IPv6 比较。
   *
   * @param bytes IP 字节
   * @return 无符号 BigInteger
   */
  private static BigInteger ipToBigInteger(byte[] bytes) {
    return new BigInteger(1, bytes);
  }

  /**
   * 规范化 ip2region 的区域字段："0" 或空白视为未知。
   *
   * @param item 原始字段
   * @return 归一化后的字段
   */
  private static String normalizeRegionItem(String item) {
    if (StrUtil.isBlank(item) || "0".equals(item)) {
      return "";
    }
    return item;
  }

  /**
   * 统一的 IPv4 参数校验入口。
   *
   * @param ip IPv4
   */
  private static void ensureIpv4(String ip) {
    if (!isValidIpv4(ip)) {
      throw new IllegalArgumentException("Ip: should be a valid IPv4");
    }
  }

  /**
   * 校验 IPv4 前缀长度合法范围。
   *
   * @param prefix 前缀长度
   */
  private static void ensureIpv4Prefix(int prefix) {
    if (prefix < 0 || prefix > IPV4_BIT_LENGTH) {
      throw new IllegalArgumentException("Prefix: should be between 0 and 32");
    }
  }

  /**
   * 将 IPv4 前缀长度转换为掩码的无符号 long 表示。
   *
   * @param prefix 前缀长度
   * @return 掩码的 long 值
   */
  private static long prefixToMaskLong(int prefix) {
    if (prefix == 0) {
      return 0L;
    }
    return (IPV4_MAX << (IPV4_BIT_LENGTH - prefix)) & IPV4_MAX;
  }

  /**
   * 解析 CIDR 字符串，抽取 baseIp、prefix 以及 IP 版本。
   *
   * @param cidr CIDR（如 192.168.1.0/24）
   * @return 解析后的结构体
   */
  private static CidrInfo parseCidr(String cidr) {
    if (StrUtil.isBlank(cidr)) {
      throw new IllegalArgumentException("Cidr: should not be blank");
    }
    // 分隔符必须存在且左右都有内容。
    int slashIndex = cidr.indexOf('/');
    if (slashIndex <= 0 || slashIndex >= cidr.length() - 1) {
      throw new IllegalArgumentException("Cidr: should be in format ip/prefix");
    }
    String baseIp = cidr.substring(0, slashIndex);
    String prefixStr = cidr.substring(slashIndex + 1);
    int prefix;
    try {
      prefix = Integer.parseInt(prefixStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Prefix: should be a number");
    }
    // IPv4/IPv6 前缀范围不同，分别校验。
    if (isValidIpv4(baseIp)) {
      if (prefix < 0 || prefix > IPV4_BIT_LENGTH) {
        throw new IllegalArgumentException("Prefix: should be between 0 and 32");
      }
      return new CidrInfo(baseIp, prefix, true);
    }
    if (isValidIpv6(baseIp)) {
      if (prefix < 0 || prefix > IPV6_BIT_LENGTH) {
        throw new IllegalArgumentException("Prefix: should be between 0 and 128");
      }
      return new CidrInfo(baseIp, prefix, false);
    }
    throw new IllegalArgumentException("Cidr: base IP is invalid");
  }

  /**
   * 枚举本机网卡地址，并按是否包含回环/是否仅 IPv6 进行过滤。
   *
   * @param includeLoopback 是否包含回环
   * @param ipv6Only        是否仅 IPv6
   * @return 满足条件的 InetAddress 列表
   */
  private static List<InetAddress> collectLocalAddresses(boolean includeLoopback, boolean ipv6Only) {
    List<InetAddress> result = new ArrayList<>();
    try {
      // 某些环境可能返回 null，统一处理为“空枚举”。
      Enumeration<NetworkInterface> interfaces = Optional.ofNullable(NetworkInterface.getNetworkInterfaces())
        .orElse(Collections.emptyEnumeration());
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();
        if (!networkInterface.isUp()) {
          continue;
        }
        if (!includeLoopback && networkInterface.isLoopback()) {
          continue;
        }
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress address = addresses.nextElement();
          // 统一在这里过滤回环/IPv4/IPv6，保证行为一致。
          if (!includeLoopback && address.isLoopbackAddress()) {
            continue;
          }
          if (ipv6Only && !(address instanceof Inet6Address)) {
            continue;
          }
          if (!ipv6Only && address instanceof Inet6Address) {
            continue;
          }
          result.add(address);
        }
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException("Get local addresses failed", e);
    }
  }

  /**
   * CIDR 解析后的结构体，避免重复拆分计算。
   */
  @AllArgsConstructor
  private static final class CidrInfo {
    /**
     * CIDR 左侧的基础 IP。
     */
    private final String baseIp;
    /**
     * 前缀长度。
     */
    private final int prefix;
    /**
     * 是否为 IPv4（false 表示 IPv6）。
     */
    private final boolean ipv4;
  }
}
