package top.csaf.id;

import lombok.extern.slf4j.Slf4j;

/**
 * Twitter SnowFlake 雪花算法
 * <p>
 * 结构：0（1位） - 时间戳（41位） - 数据中心（5位） - 机器 ID（5位） - 序列号（12位）
 */
@Slf4j
public class SnowFlake {

  /**
   * 默认开始时间截：2025-01-01 00:00:00
   */
  private final static long DEFAULT_START_TIME = 1735689600000L;

  /**
   * 数据中心占用位数
   */
  private final static long DATACENTER_BIT = 5;
  /**
   * 机器 ID 占用位数
   */
  private final static long MACHINE_BIT = 5;
  /**
   * 序列号占用位数
   */
  private final static long SEQUENCE_BIT = 12;

  /**
   * 数据中心最大值
   */
  private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
  /**
   * 机器 ID 最大值
   */
  private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
  /**
   * 序列号最大值
   */
  private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

  /**
   * 数据中心 ID 左移位数
   */
  private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
  /**
   * 机器 ID 左移位数
   */
  private final static long MACHINE_LEFT = SEQUENCE_BIT;
  /**
   * 时间戳左移位数
   */
  private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

  /**
   * 数据中心 ID（0~31）
   */
  private final long datacenterId;
  /**
   * 机器 ID（0~31）
   */
  private final long machineId;
  /**
   * 开始时间戳
   */
  private final long startTimeMillis;
  /**
   * 序列号
   */
  private long sequence = 0L;
  /**
   * 上一次生成 ID 的时间戳
   */
  private long lastTimestamp = -1L;

  /**
   * 构造函数 (使用默认开始时间)
   *
   * @param datacenterId 数据中心 ID（0~31）
   * @param machineId    机器 ID（0~31）
   */
  public SnowFlake(long datacenterId, long machineId) {
    this(datacenterId, machineId, DEFAULT_START_TIME);
  }

  /**
   * 构造函数 (自定义开始时间)
   *
   * @param datacenterId    数据中心 ID（0~31）
   * @param machineId       机器 ID（0~31）
   * @param startTimeMillis 开始时间戳 (Twepoch)，一旦上线不可更改
   */
  public SnowFlake(long datacenterId, long machineId, long startTimeMillis) {
    if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
      throw new IllegalArgumentException("datacenterId can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
    }
    if (machineId > MAX_MACHINE_NUM || machineId < 0) {
      throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
    }
    // 校验开始时间，不能晚于当前时间太多（虽然理论上可以，但容易出问题）
    if (startTimeMillis > timeGen()) {
      throw new IllegalArgumentException("startTimeMillis cannot be in the future");
    }

    this.datacenterId = datacenterId;
    this.machineId = machineId;
    this.startTimeMillis = startTimeMillis;
  }

  /**
   * 获得下一个 ID
   *
   * @return SnowflakeId
   */
  public synchronized long next() {
    long currTimestamp = timeGen();

    if (currTimestamp < lastTimestamp) {
      long offset = lastTimestamp - currTimestamp;
      if (offset <= 5) {
        try {
          wait(offset << 1);
          currTimestamp = timeGen();
          if (currTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      } else {
        throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
      }
    }

    if (lastTimestamp == currTimestamp) {
      sequence = (sequence + 1) & MAX_SEQUENCE;
      if (sequence == 0) {
        currTimestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      sequence = 0L;
    }

    lastTimestamp = currTimestamp;

    // 使用自定义的起始时间计算时间戳差值
    return ((currTimestamp - this.startTimeMillis) << TIMESTAMP_LEFT) //
      | (datacenterId << DATACENTER_LEFT) //
      | (machineId << MACHINE_LEFT) //
      | sequence;
  }

  private long tilNextMillis(long lastTimestamp) {
    long timestamp = timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen();
    }
    return timestamp;
  }

  /**
   * 获取当前时间戳，提取为 protected 方法以便测试类重写 (Mock)
   *
   * @return 当前时间戳
   */
  protected long timeGen() {
    return System.currentTimeMillis();
  }
}
