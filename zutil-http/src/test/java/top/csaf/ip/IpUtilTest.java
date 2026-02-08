package top.csaf.ip;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lionsoul.ip2region.xdb.Searcher;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IpUtil 工具类测试。
 */
@DisplayName("IpUtil 工具类测试")
class IpUtilTest {

  /**
   * 验证 IPv4 合法性判断。
   */
  @Test
  void testIpv4Validation() {
    assertFalse(IpUtil.isValidIpv4(null));
    assertFalse(IpUtil.isValidIpv4(""));
    assertFalse(IpUtil.isValidIpv4(" "));
    assertTrue(IpUtil.isValidIpv4("0.0.0.0"));
    assertTrue(IpUtil.isValidIpv4("192.168.1.1"));
    assertTrue(IpUtil.isValidIp("192.168.1.1"));
    assertTrue(IpUtil.isIpv4("192.168.1.1"));
    assertFalse(IpUtil.isIpv6("192.168.1.1"));
    assertTrue(IpUtil.isValidIpv4("255.255.255.255"));
    assertFalse(IpUtil.isValidIpv4("256.1.1.1"));
    assertFalse(IpUtil.isValidIpv4("1.2.3"));
    assertFalse(IpUtil.isValidIpv4("1.2.3.4.5"));
    assertFalse(IpUtil.isValidIpv4("1..3.4"));
    assertFalse(IpUtil.isValidIpv4("1234.1.1.1"));
    assertFalse(IpUtil.isValidIpv4("1.2.3./"));
    assertFalse(IpUtil.isValidIpv4("1.2.3.a"));
    assertFalse(IpUtil.isValidIpv4("abc"));
    assertFalse(IpUtil.isValidIp("999.1.1.1"));
  }

  /**
   * 验证 IPv6 合法性判断。
   */
  @Test
  void testIpv6Validation() {
    assertFalse(IpUtil.isValidIpv6(null));
    assertFalse(IpUtil.isValidIpv6(""));
    assertFalse(IpUtil.isValidIpv6(" "));
    assertFalse(IpUtil.isValidIpv6("abcd"));
    assertTrue(IpUtil.isValidIpv6("::1"));
    assertTrue(IpUtil.isValidIpv6("[::1]"));
    assertTrue(IpUtil.isValidIpv6("2001:db8::1"));
    assertTrue(IpUtil.isValidIp("2001:db8::1"));
    assertTrue(IpUtil.isIpv6("2001:db8::1"));
    assertFalse(IpUtil.isValidIpv6("2001:db8::gg"));
    assertFalse(IpUtil.isValidIpv6("2001:::1"));
  }

  /**
   * 验证 IPv4 转换。
   */
  @Test
  void testIpv4Conversion() {
    assertEquals(16909060L, IpUtil.ipv4ToLong("1.2.3.4"));
    assertEquals("1.2.3.4", IpUtil.longToIpv4(16909060L));
    assertEquals("0.0.0.0", IpUtil.longToIpv4(0));
    assertEquals("1.2.3.4", IpUtil.normalizeIp("1.2.3.4"));
    assertEquals("", IpUtil.normalizeIp("999.1.1.1"));
    assertArrayEquals(new byte[]{1, 2, 3, 4}, IpUtil.ipToBytes("1.2.3.4"));
    assertArrayEquals(new byte[]{1, 2, 3, 4}, IpUtil.ipToBytesOrNull("1.2.3.4"));
    assertEquals("1.2.3.4", IpUtil.toInetAddress("1.2.3.4").getHostAddress());
    assertEquals("255.255.255.255", IpUtil.longToIpv4(0xFFFFFFFFL));
    assertNull(IpUtil.ipToBytesOrNull("999.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.ipToBytes("999.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.toInetAddress("999.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.ipv4ToLong("999.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.longToIpv4(-1));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.longToIpv4(0x1_0000_0000L));
  }

  /**
   * 验证 IP 解析异常分支。
   */
  @Test
  void testIpBytesGetByNameFailure() {
    // 静态 mock InetAddress，触发解析失败分支。
    try (MockedStatic<InetAddress> mocked = Mockito.mockStatic(InetAddress.class)) {
      mocked.when(() -> InetAddress.getByName("1.2.3.4")).thenThrow(new RuntimeException("boom"));
      RuntimeException bytesError = assertThrows(RuntimeException.class, () -> IpUtil.ipToBytes("1.2.3.4"));
      assertEquals("Ip: parse failed", bytesError.getMessage());
      assertNull(IpUtil.ipToBytesOrNull("1.2.3.4"));
      RuntimeException addressError = assertThrows(RuntimeException.class, () -> IpUtil.toInetAddress("1.2.3.4"));
      assertEquals("Ip: parse failed", addressError.getMessage());
      InetAddress address = ReflectionTestUtils.invokeMethod(IpUtil.class, "toInetAddressOrNull", "1.2.3.4");
      assertNull(address);
    }
  }

  /**
   * 验证前缀与掩码互转。
   */
  @Test
  void testPrefixMask() {
    assertEquals("0.0.0.0", IpUtil.prefixToMask(0));
    assertEquals("255.255.255.0", IpUtil.prefixToMask(24));
    assertEquals(24, IpUtil.maskToPrefix("255.255.255.0"));
    assertEquals(0, IpUtil.maskToPrefix("0.0.0.0"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.prefixToMask(-1));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.prefixToMask(33));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.maskToPrefix("999.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.maskToPrefix("255.0.255.0"));
  }

  /**
   * 覆盖掩码连续性兜底检查。
   */
  @Test
  void testMaskToPrefixDefensiveCheck() {
    // 伪造包含高位的掩码，命中兜底连续性校验。
    try (MockedStatic<IpUtil> mocked = Mockito.mockStatic(IpUtil.class, Mockito.CALLS_REAL_METHODS)) {
      long maskWithHighBits = (1L << 40) | 0xFFFFFF00L;
      mocked.when(() -> IpUtil.ipv4ToLong("255.255.255.0")).thenReturn(maskWithHighBits);
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> IpUtil.maskToPrefix("255.255.255.0"));
      assertEquals("Mask: should be contiguous", exception.getMessage());
    }
  }

  /**
   * 验证 CIDR 与范围判断。
   */
  @Test
  void testCidrAndRange() {
    assertTrue(IpUtil.isInCidr("192.168.1.10", "192.168.1.0/24"));
    assertFalse(IpUtil.isInCidr("192.168.2.10", "192.168.1.0/24"));
    assertTrue(IpUtil.isInCidr("2001:db8::1", "2001:db8::/32"));
    assertFalse(IpUtil.isInCidr("2001:db9::1", "2001:db8::/32"));
    assertFalse(IpUtil.isInCidr("999.1.1.1", "192.168.1.0/24"));
    assertFalse(IpUtil.isInCidr("2001:db8::1", "192.168.1.0/24"));
    assertFalse(IpUtil.isInCidr("1.1.1.1", "2001:db8::/32"));

    assertTrue(IpUtil.isInRange("10.0.0.5", "10.0.0.1", "10.0.0.10"));
    assertFalse(IpUtil.isInRange("10.0.0.20", "10.0.0.1", "10.0.0.10"));
    assertTrue(IpUtil.isInRange("2001:db8::1", "2001:db8::1", "2001:db8::2"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInRange("10.0.0.5", "10.0.0.10", "10.0.0.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInRange("2001:db8::2", "2001:db8::2", "2001:db8::1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInRange("1.1.1.1", "2001:db8::1", "2001:db8::2"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInRange("999.1.1.1", "10.0.0.1", "10.0.0.2"));
  }

  /**
   * 验证 CIDR 非法输入。
   */
  @Test
  void testCidrInvalidInputs() {
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", ""));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "1.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "/24"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "1.1.1.1/"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "1.1.1.1/abc"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "1.1.1.1/33"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "2001:db8::/129"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.isInCidr("1.1.1.1", "invalid/24"));
  }

  /**
   * 验证私网与公网判断。
   */
  @Test
  void testPrivatePublicFlags() {
    assertTrue(IpUtil.isPrivateIpv4("10.0.0.1"));
    assertFalse(IpUtil.isPrivateIpv4("8.8.8.8"));
    assertFalse(IpUtil.isPrivateIpv4("999.1.1.1"));
    assertTrue(IpUtil.isCarrierNatIpv4("100.64.0.1"));
    assertFalse(IpUtil.isCarrierNatIpv4("8.8.8.8"));
    assertFalse(IpUtil.isCarrierNatIpv4("999.1.1.1"));
    assertTrue(IpUtil.isLoopback("127.0.0.1"));
    assertTrue(IpUtil.isBroadcastIpv4("255.255.255.255"));
    assertFalse(IpUtil.isBroadcastIpv4("8.8.8.8"));
    assertFalse(IpUtil.isBroadcastIpv4("999.1.1.1"));
    assertTrue(IpUtil.isSiteLocal("10.0.0.1"));
    assertTrue(IpUtil.isReservedIpv4("0.0.0.1"));
    assertTrue(IpUtil.isReservedIpv4("240.0.0.1"));
    assertFalse(IpUtil.isReservedIpv4("8.8.8.8"));
    assertFalse(IpUtil.isReservedIpv4("999.1.1.1"));
    assertTrue(IpUtil.isPublicIpv4("8.8.8.8"));
    assertFalse(IpUtil.isPublicIpv4("10.0.0.1"));
  }

  /**
   * 验证本机地址标记判断。
   */
  @Test
  void testLocalAddressFlags() {
    assertTrue(IpUtil.isLoopback("127.0.0.1"));
    assertFalse(IpUtil.isLoopback("8.8.8.8"));
    assertFalse(IpUtil.isLoopback("999.1.1.1"));

    assertTrue(IpUtil.isAnyLocal("0.0.0.0"));
    assertFalse(IpUtil.isAnyLocal("8.8.8.8"));
    assertFalse(IpUtil.isAnyLocal("999.1.1.1"));

    assertTrue(IpUtil.isLinkLocal("169.254.1.1"));
    assertFalse(IpUtil.isLinkLocal("8.8.8.8"));
    assertFalse(IpUtil.isLinkLocal("999.1.1.1"));

    assertTrue(IpUtil.isSiteLocal("10.0.0.1"));
    assertFalse(IpUtil.isSiteLocal("8.8.8.8"));
    assertFalse(IpUtil.isSiteLocal("999.1.1.1"));

    assertTrue(IpUtil.isMulticast("224.0.0.1"));
    assertFalse(IpUtil.isMulticast("8.8.8.8"));
    assertFalse(IpUtil.isMulticast("999.1.1.1"));
  }

  /**
   * 覆盖公网 IPv4 条件分支。
   */
  @Test
  void testPublicIpv4Conditions() {
    assertFalse(IpUtil.isPublicIpv4("10.0.0.1"));
    assertFalse(IpUtil.isPublicIpv4("100.64.0.1"));
    assertFalse(IpUtil.isPublicIpv4("127.0.0.1"));
    assertFalse(IpUtil.isPublicIpv4("169.254.1.1"));
    assertFalse(IpUtil.isPublicIpv4("0.0.0.0"));
    assertFalse(IpUtil.isPublicIpv4("224.0.0.1"));
    assertFalse(IpUtil.isPublicIpv4("255.255.255.255"));
    assertFalse(IpUtil.isPublicIpv4("240.0.0.1"));
    assertTrue(IpUtil.isPublicIpv4("8.8.8.8"));
    assertFalse(IpUtil.isPublicIpv4("999.1.1.1"));
  }

  /**
   * 验证本机地址相关方法调用。
   */
  @Test
  void testLocalAddressHelpers() {
    String hostName = IpUtil.getLocalHostName();
    assertNotNull(hostName);
    assertFalse(hostName.trim().isEmpty());

    String hostAddress = IpUtil.getLocalHostAddress();
    assertNotNull(hostAddress);
    assertFalse(hostAddress.trim().isEmpty());

    List<String> ipv4s = IpUtil.getLocalIpv4s();
    assertNotNull(ipv4s);
    List<String> ipv4sWithLoopback = IpUtil.getLocalIpv4s(true);
    assertNotNull(ipv4sWithLoopback);

    List<String> ipv6s = IpUtil.getLocalIpv6s();
    assertNotNull(ipv6s);
    List<String> ipv6sWithLoopback = IpUtil.getLocalIpv6s(true);
    assertNotNull(ipv6sWithLoopback);
  }

  /**
   * 验证本机地址获取异常分支。
   */
  @Test
  void testLocalHostFailure() {
    // 静态 mock getLocalHost，覆盖异常分支。
    try (MockedStatic<InetAddress> mocked = Mockito.mockStatic(InetAddress.class)) {
      mocked.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException("boom"));
      assertThrows(RuntimeException.class, IpUtil::getLocalHostName);
      assertThrows(RuntimeException.class, IpUtil::getLocalHostAddress);
    }
  }

  /**
   * 验证本机地址列表为空的情况。
   */
  @Test
  void testLocalIpsEmptyWhenNoInterfaces() {
    // getNetworkInterfaces 返回 null 时应回退为空列表。
    try (MockedStatic<NetworkInterface> mocked = Mockito.mockStatic(NetworkInterface.class)) {
      mocked.when(NetworkInterface::getNetworkInterfaces).thenReturn(null);
      assertEquals(Collections.emptyList(), IpUtil.getLocalIps(false, false));
    }
  }

  /**
   * 验证本机地址过滤逻辑。
   */
  @Test
  void testLocalIpsFiltering() throws Exception {
    InetAddress loopback = InetAddress.getByName("127.0.0.1");
    InetAddress ipv4 = InetAddress.getByName("192.168.1.10");
    InetAddress ipv6 = InetAddress.getByName("2001:db8::1");

    // 组合接口与地址，验证 up/loopback/IPv4/IPv6 的过滤逻辑。
    NetworkInterface downInterface = Mockito.mock(NetworkInterface.class);
    Mockito.when(downInterface.isUp()).thenReturn(false);

    NetworkInterface loopbackInterface = Mockito.mock(NetworkInterface.class);
    Mockito.when(loopbackInterface.isUp()).thenReturn(true);
    Mockito.when(loopbackInterface.isLoopback()).thenReturn(true);
    // 每次返回新的 Enumeration，避免被前一次遍历耗尽。
    Mockito.when(loopbackInterface.getInetAddresses()).thenAnswer(invocation -> enumeration(loopback));

    NetworkInterface activeInterface = Mockito.mock(NetworkInterface.class);
    Mockito.when(activeInterface.isUp()).thenReturn(true);
    Mockito.when(activeInterface.isLoopback()).thenReturn(false);
    // 每次返回新的 Enumeration，避免被前一次遍历耗尽。
    Mockito.when(activeInterface.getInetAddresses())
        .thenAnswer(invocation -> enumeration(loopback, ipv4, ipv6));

    try (MockedStatic<NetworkInterface> mocked = Mockito.mockStatic(NetworkInterface.class)) {
      mocked.when(NetworkInterface::getNetworkInterfaces)
          .thenAnswer(invocation -> enumeration(downInterface, loopbackInterface, activeInterface));

      assertEquals(Collections.singletonList(ipv4.getHostAddress()), IpUtil.getLocalIps(false, false));
      assertEquals(Collections.singletonList(ipv6.getHostAddress()), IpUtil.getLocalIps(true, true));
    }
  }

  /**
   * 验证本机地址列表异常分支。
   */
  @Test
  void testLocalIpsException() throws Exception {
    // 静态 mock 抛异常，覆盖错误处理分支。
    try (MockedStatic<NetworkInterface> mocked = Mockito.mockStatic(NetworkInterface.class)) {
      mocked.when(NetworkInterface::getNetworkInterfaces).thenThrow(new SocketException("boom"));
      RuntimeException exception = assertThrows(RuntimeException.class, () -> IpUtil.getLocalIps(false, false));
      assertEquals("Get local addresses failed", exception.getMessage());
    }
  }

  /**
   * 验证网络地址与广播地址计算。
   */
  @Test
  void testNetworkBroadcast() {
    assertEquals("192.168.1.0", IpUtil.getNetworkAddress("192.168.1.10", 24));
    assertEquals("192.168.1.255", IpUtil.getBroadcastAddress("192.168.1.10", 24));
    assertArrayEquals(new String[]{"192.168.1.0", "192.168.1.255"}, IpUtil.getIpv4Range("192.168.1.10/24"));
  }

  /**
   * 验证网络地址相关非法输入。
   */
  @Test
  void testNetworkAddressInvalidInputs() {
    assertThrows(IllegalArgumentException.class, () -> IpUtil.getNetworkAddress("999.1.1.1", 24));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.getNetworkAddress("1.1.1.1", -1));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.getBroadcastAddress("999.1.1.1", 24));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.getBroadcastAddress("1.1.1.1", 33));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.getIpv4Range("2001:db8::/32"));
  }

  /**
   * 验证区域解析。
   */
  @Test
  void testRegionParsing() {
    IpUtil.RegionInfo info = IpUtil.parseRegion("中国|0|广东|深圳|电信");
    assertEquals("中国", info.getCountry());
    assertEquals("", info.getRegion());
    assertEquals("广东", info.getProvince());
    assertEquals("深圳", info.getCity());
    assertEquals("电信", info.getIsp());
    assertFalse(info.isUnknown());
    assertEquals("中国|0|广东|深圳|电信", info.toString());

    IpUtil.RegionInfo empty = IpUtil.parseRegion("");
    assertTrue(empty.isUnknown());

    IpUtil.RegionInfo nullRegion = IpUtil.parseRegion(null);
    assertTrue(nullRegion.isUnknown());
    assertEquals("||||", nullRegion.toString());
  }

  /**
   * 验证区域拆分与归一化。
   */
  @Test
  void testSplitRegion() {
    String[] parts = IpUtil.splitRegion("中国|0|广东");
    assertEquals(5, parts.length);
    assertEquals("中国", parts[0]);
    assertEquals("", parts[1]);
    assertEquals("广东", parts[2]);
    assertEquals("", parts[3]);
    assertEquals("", parts[4]);

    assertArrayEquals(new String[]{"", "", "", "", ""}, IpUtil.splitRegion(null));
    assertArrayEquals(new String[]{"", "", "", "", ""}, IpUtil.splitRegion(" "));
  }

  /**
   * 验证区域查询接口（使用桩实现）。
   */
  @Test
  void testSearchRegionWithStub() {
    IpUtil.RegionSearcher searcher = ip -> "中国|0|广东|深圳|电信";
    assertEquals("中国|0|广东|深圳|电信", IpUtil.searchRegion(searcher, "1.2.3.4"));
    assertEquals("广东", IpUtil.searchRegionInfo(searcher, "1.2.3.4").getProvince());
    assertThrows(IllegalArgumentException.class, () -> IpUtil.searchRegion(searcher, "999.1.1.1"));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.searchRegionInfo(searcher, "999.1.1.1"));
    IpUtil.RegionSearcher emptySearcher = ip -> null;
    assertEquals("", IpUtil.searchRegion(emptySearcher, "1.2.3.4"));
    assertTrue(IpUtil.searchRegionInfo(emptySearcher, "1.2.3.4").isUnknown());
    assertThrows(IllegalArgumentException.class, () -> IpUtil.searchRegionInfo("", "1.2.3.4"));
  }

  /**
   * 验证 ip2region 搜索器工厂参数校验。
   */
  @Test
  void testRegionSearcherFactory() {
    assertThrows(IllegalArgumentException.class, () -> IpUtil.Ip2RegionSearcher.newWithFileOnly(""));
    assertThrows(IllegalArgumentException.class, () -> IpUtil.Ip2RegionSearcher.newWithBuffer(null));
  }

  /**
   * 验证 dbPath 方式查询区域。
   */
  @Test
  void testSearchRegionWithDbPathMocked() {
    IpUtil.Ip2RegionSearcher searcher = Mockito.mock(IpUtil.Ip2RegionSearcher.class);
    Mockito.when(searcher.search("1.2.3.4")).thenReturn("中国|0|广东|深圳|电信");
    // 静态 mock 工厂方法，避免依赖真实 db 文件。
    try (MockedStatic<IpUtil.Ip2RegionSearcher> mocked = Mockito.mockStatic(IpUtil.Ip2RegionSearcher.class)) {
      mocked.when(() -> IpUtil.Ip2RegionSearcher.newWithFileOnly("db.xdb")).thenReturn(searcher);
      assertEquals("中国|0|广东|深圳|电信", IpUtil.searchRegion("db.xdb", "1.2.3.4"));
      assertEquals("广东", IpUtil.searchRegionInfo("db.xdb", "1.2.3.4").getProvince());
      assertThrows(IllegalArgumentException.class, () -> IpUtil.searchRegion("db.xdb", "999.1.1.1"));
    }
  }

  /**
   * 验证 ip2region 搜索器工厂异常处理。
   */
  @Test
  void testIp2RegionSearcherFactories() {
    Searcher searcher = Mockito.mock(Searcher.class);
    byte[] content = new byte[]{1, 2, 3};

    // 覆盖 Searcher 工厂方法成功与失败路径。
    try (MockedStatic<Searcher> mocked = Mockito.mockStatic(Searcher.class)) {
      mocked.when(() -> Searcher.newWithFileOnly("db.xdb")).thenReturn(searcher);
      assertNotNull(IpUtil.Ip2RegionSearcher.newWithFileOnly("db.xdb"));
    }

    try (MockedStatic<Searcher> mocked = Mockito.mockStatic(Searcher.class)) {
      mocked.when(() -> Searcher.newWithFileOnly("db.xdb")).thenThrow(new RuntimeException("boom"));
      RuntimeException exception = assertThrows(RuntimeException.class, () -> IpUtil.Ip2RegionSearcher.newWithFileOnly("db.xdb"));
      assertEquals("Init ip2region searcher failed", exception.getMessage());
    }

    try (MockedStatic<Searcher> mocked = Mockito.mockStatic(Searcher.class)) {
      mocked.when(() -> Searcher.newWithBuffer(content)).thenReturn(searcher);
      assertNotNull(IpUtil.Ip2RegionSearcher.newWithBuffer(content));
    }

    try (MockedStatic<Searcher> mocked = Mockito.mockStatic(Searcher.class)) {
      mocked.when(() -> Searcher.newWithBuffer(content)).thenThrow(new RuntimeException("boom"));
      RuntimeException exception = assertThrows(RuntimeException.class, () -> IpUtil.Ip2RegionSearcher.newWithBuffer(content));
      assertEquals("Init ip2region searcher failed", exception.getMessage());
    }
  }

  /**
   * 验证 ip2region 搜索器生命周期。
   */
  @Test
  void testIp2RegionSearcherLifecycle() throws Exception {
    Searcher searcher = Mockito.mock(Searcher.class);
    // Searcher.search/close 声明受检异常，这里统一在测试中覆盖。
    Mockito.when(searcher.search("1.2.3.4")).thenReturn("中国|0|广东|深圳|电信");
    IpUtil.Ip2RegionSearcher ip2RegionSearcher = newIp2RegionSearcher(searcher);

    assertEquals("中国|0|广东|深圳|电信", ip2RegionSearcher.search("1.2.3.4"));
    assertThrows(NullPointerException.class, () -> ip2RegionSearcher.search(null));

    Mockito.when(searcher.search("1.2.3.5")).thenThrow(new RuntimeException("boom"));
    RuntimeException searchError =
        assertThrows(RuntimeException.class, () -> ip2RegionSearcher.search("1.2.3.5"));
    assertEquals("Search ip2region failed", searchError.getMessage());

    assertThrows(IllegalArgumentException.class, () -> ip2RegionSearcher.search("999.1.1.1"));

    ip2RegionSearcher.close();
    Mockito.doThrow(new RuntimeException("boom")).when(searcher).close();
    RuntimeException closeError = assertThrows(RuntimeException.class, ip2RegionSearcher::close);
    assertEquals("Close ip2region searcher failed", closeError.getMessage());
  }

  /**
   * 验证 @NonNull 参数约束。
   */
  @Test
  void testNonNullContracts() throws Exception {
    IpUtil.RegionSearcher searcher = ip -> "";
    try {
      IpUtil.ipv4ToLong(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.ipToBytes(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.toInetAddress(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.isInRange(null, "1.1.1.1", "1.1.1.2");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.isInRange("1.1.1.1", null, "1.1.1.2");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.isInRange("1.1.1.1", "1.1.1.2", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.isInCidr(null, "1.1.1.0/24");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.isInCidr("1.1.1.1", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.getNetworkAddress(null, 24);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.getBroadcastAddress(null, 24);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.getIpv4Range(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.maskToPrefix(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegion((IpUtil.RegionSearcher) null, "1.1.1.1");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegion(searcher, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegionInfo((IpUtil.RegionSearcher) null, "1.1.1.1");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegionInfo(searcher, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegion((String) null, "1.1.1.1");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegion("db", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegionInfo((String) null, "1.1.1.1");
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.searchRegionInfo("db", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
    try {
      IpUtil.Ip2RegionSearcher.newWithFileOnly(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // expected
    } catch (Exception e) {
      fail("Expected NullPointerException", e);
    }
  }

  /**
   * 验证私有方法覆盖。
   */
  @Test
  void testPrivateHelpers() {
    // 通过反射覆盖私有辅助方法分支。
    assertEquals("", ReflectionTestUtils.invokeMethod(IpUtil.class, "stripBrackets", (Object) null));
    assertEquals("::1", ReflectionTestUtils.invokeMethod(IpUtil.class, "stripBrackets", "[::1]"));
    assertEquals("1.2.3.4", ReflectionTestUtils.invokeMethod(IpUtil.class, "stripBrackets", "1.2.3.4"));
    assertEquals("[]", ReflectionTestUtils.invokeMethod(IpUtil.class, "stripBrackets", "[]"));

    assertThrows(IllegalArgumentException.class,
        () -> ReflectionTestUtils.invokeMethod(IpUtil.class, "parseIpv4Parts", "999.1.1.1"));
    int[] parts = ReflectionTestUtils.invokeMethod(IpUtil.class, "parseIpv4Parts", "1.2.3.4");
    assertArrayEquals(new int[]{1, 2, 3, 4}, parts);

    BigInteger bigValue = ReflectionTestUtils.invokeMethod(IpUtil.class, "ipToBigInteger", new byte[]{1, 2, 3, 4});
    assertEquals(new BigInteger(1, new byte[]{1, 2, 3, 4}), bigValue);

    assertEquals("", ReflectionTestUtils.invokeMethod(IpUtil.class, "normalizeRegionItem", "0"));
    assertEquals("", ReflectionTestUtils.invokeMethod(IpUtil.class, "normalizeRegionItem", ""));
    assertEquals("广东", ReflectionTestUtils.invokeMethod(IpUtil.class, "normalizeRegionItem", "广东"));

    assertThrows(IllegalArgumentException.class,
        () -> ReflectionTestUtils.invokeMethod(IpUtil.class, "ensureIpv4", "999.1.1.1"));
    assertThrows(IllegalArgumentException.class,
        () -> ReflectionTestUtils.invokeMethod(IpUtil.class, "ensureIpv4Prefix", -1));
    assertThrows(IllegalArgumentException.class,
        () -> ReflectionTestUtils.invokeMethod(IpUtil.class, "ensureIpv4Prefix", 33));

    assertEquals(0L, ((Long) ReflectionTestUtils.invokeMethod(IpUtil.class, "prefixToMaskLong", 0)).longValue());
    assertEquals(0xFFFFFFFFL,
        ((Long) ReflectionTestUtils.invokeMethod(IpUtil.class, "prefixToMaskLong", 32)).longValue());
  }

  private static <T> Enumeration<T> enumeration(T... items) {
    return Collections.enumeration(Arrays.asList(items));
  }

  private static IpUtil.Ip2RegionSearcher newIp2RegionSearcher(Searcher searcher) {
    try {
      // 通过反射访问私有构造器，避免破坏生产代码可见性。
      java.lang.reflect.Constructor<IpUtil.Ip2RegionSearcher> constructor =
        IpUtil.Ip2RegionSearcher.class.getDeclaredConstructor(Searcher.class);
      constructor.setAccessible(true);
      return constructor.newInstance(searcher);
    } catch (Exception e) {
      throw new RuntimeException("Init Ip2RegionSearcher failed", e);
    }
  }
}
