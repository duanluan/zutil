# IpUtil IP 工具类

> 📦 **包路径**: `top.csaf.ip.IpUtil`
>
> 🔗 **说明**: 提供 IP 校验、转换、范围判断、本机地址获取与 ip2region 区域查询能力。

**IpUtil** 是一个全静态的 IP 工具类，覆盖 IPv4/IPv6 的校验、转换、范围计算与归类判断，同时封装了 ip2region 查询接口，便于快速获取 IP 所属区域信息。

## ✨ 特性

* **双栈支持**: 同时支持 IPv4/IPv6 校验与范围判断。
* **多种转换**: IPv4 ⇄ long、IP ⇄ byte[]、IP ⇄ InetAddress。
* **网络计算**: CIDR 判断、掩码与前缀互转、网络地址与广播地址计算。
* **本机地址**: 获取主机名、主机地址以及本机 IPv4/IPv6 列表。
* **区域查询**: 内置 ip2region 搜索器封装，支持文件或内存数据加载。

## 🚀 快速开始

### 1. 校验与规范化

```java
boolean valid = IpUtil.isValidIp("192.168.1.1");
boolean ipv6 = IpUtil.isValidIpv6("[2001:db8::1]");
String normalized = IpUtil.normalizeIp("[2001:db8::1]");
```

### 2. IPv4 转换

```java
long value = IpUtil.ipv4ToLong("1.2.3.4");
String ip = IpUtil.longToIpv4(value);
byte[] bytes = IpUtil.ipToBytes("1.2.3.4");
```

### 3. 范围与 CIDR

```java
boolean inRange = IpUtil.isInRange("1.2.3.4", "1.2.3.0", "1.2.3.255");
boolean inCidr = IpUtil.isInCidr("1.2.3.4", "1.2.3.0/24");
```

### 4. 掩码与前缀

```java
String mask = IpUtil.prefixToMask(24);
int prefix = IpUtil.maskToPrefix("255.255.255.0");
```

### 5. 本机地址

```java
String hostName = IpUtil.getLocalHostName();
String hostAddress = IpUtil.getLocalHostAddress();
List<String> ipv4s = IpUtil.getLocalIpv4s(true);
List<String> ipv6s = IpUtil.getLocalIpv6s(true);
```

### 6. ip2region 区域查询

```java
// 直接通过 dbPath 查询（内部会创建并关闭搜索器）
String region = IpUtil.searchRegion("ip2region.xdb", "1.2.3.4");
IpUtil.RegionInfo info = IpUtil.searchRegionInfo("ip2region.xdb", "1.2.3.4");

// 复用搜索器（高频调用建议此方式）
try (IpUtil.Ip2RegionSearcher searcher = IpUtil.Ip2RegionSearcher.newWithFileOnly("ip2region.xdb")) {
  String regionText = IpUtil.searchRegion(searcher, "1.2.3.4");
}
```

## 📚 API 速览

### 校验与规范化

| 方法名 | 描述 |
| :--- | :--- |
| `isValidIp(String)` | 判断是否为合法 IPv4/IPv6 |
| `isValidIpv4(String)` | 判断是否为合法 IPv4 |
| `isValidIpv6(String)` | 判断是否为合法 IPv6（支持 `[::1]`） |
| `normalizeIp(String)` | 规范化 IP（非法返回空串） |

### 转换与解析

| 方法名 | 描述 |
| :--- | :--- |
| `ipv4ToLong(String)` | IPv4 → 无符号 long |
| `longToIpv4(long)` | 无符号 long → IPv4 |
| `ipToBytes(String)` | IP → byte[]（非法抛异常） |
| `ipToBytesOrNull(String)` | IP → byte[]（非法返回 null） |
| `toInetAddress(String)` | IP → InetAddress |

### IP 类型判断（IPv4）

| 方法名 | 描述 |
| :--- | :--- |
| `isPrivateIpv4(String)` | 是否为内网地址 |
| `isCarrierNatIpv4(String)` | 是否为运营商 NAT 地址 |
| `isBroadcastIpv4(String)` | 是否为广播地址 |
| `isReservedIpv4(String)` | 是否为保留地址 |
| `isPublicIpv4(String)` | 是否为公网地址 |

### 通用类型判断

| 方法名 | 描述 |
| :--- | :--- |
| `isLoopback(String)` | 是否为回环地址 |
| `isAnyLocal(String)` | 是否为任意地址（0.0.0.0 / ::） |
| `isLinkLocal(String)` | 是否为链路本地地址 |
| `isSiteLocal(String)` | 是否为站点本地地址 |
| `isMulticast(String)` | 是否为组播地址 |

### 范围、CIDR 与掩码

| 方法名 | 描述 |
| :--- | :--- |
| `isInRange(String, String, String)` | 判断 IP 是否在指定范围内 |
| `isInCidr(String, String)` | 判断 IP 是否在 CIDR 内 |
| `getNetworkAddress(String, int)` | 计算 IPv4 网络地址 |
| `getBroadcastAddress(String, int)` | 计算 IPv4 广播地址 |
| `getIpv4Range(String)` | 计算 IPv4 CIDR 的起止范围 |
| `prefixToMask(int)` | 前缀长度 → 掩码 |
| `maskToPrefix(String)` | 掩码 → 前缀长度 |

### 本机地址

| 方法名 | 描述 |
| :--- | :--- |
| `getLocalHostName()` | 获取主机名 |
| `getLocalHostAddress()` | 获取主机地址 |
| `getLocalIpv4s()` / `getLocalIpv4s(boolean)` | 获取本机 IPv4 列表 |
| `getLocalIpv6s()` / `getLocalIpv6s(boolean)` | 获取本机 IPv6 列表 |
| `getLocalIps(boolean, boolean)` | 获取本机 IP 列表（支持过滤） |

### 区域查询（ip2region）

| 方法名/类型 | 描述 |
| :--- | :--- |
| `searchRegion(RegionSearcher, String)` | 使用自定义搜索器查询区域 |
| `searchRegionInfo(RegionSearcher, String)` | 查询并返回 RegionInfo |
| `searchRegion(String, String)` | 使用 dbPath 查询区域 |
| `searchRegionInfo(String, String)` | 使用 dbPath 查询并返回 RegionInfo |
| `Ip2RegionSearcher` | ip2region 搜索器适配类 |
| `RegionInfo` | 区域信息对象（country/region/province/city/isp） |

## ⚙️ 异常处理

* **参数校验**: IP、CIDR、前缀等非法时会抛出 `IllegalArgumentException`。
* **解析失败**: `ipToBytes` / `toInetAddress` 解析失败会抛出 `RuntimeException`；`ipToBytesOrNull` 仅返回 null。
* **本机地址**: 获取网卡或主机信息失败时抛出 `RuntimeException`。
* **ip2region**: 仅支持 IPv4；初始化或查询失败会抛出 `RuntimeException`。

## 📝 最佳实践

1. **先校验再计算**: 对用户输入的 IP，建议先调用 `isValidIp`。
2. **高频查询复用搜索器**: ip2region 查询建议使用 `Ip2RegionSearcher` 并复用实例。
3. **IPv4/IPv6 分开处理**: `isInRange` 与 `isInCidr` 会要求 IP 版本一致。
