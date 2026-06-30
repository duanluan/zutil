package top.csaf.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.csaf.lang.ObjUtil;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ObjUtil 工具类测试")
public class ObjUtilTest {

    @DisplayName("isAllEquals：小于 0，数字为 null 时返回 false")
    @Test
    void isAllEquals() {
        assertTrue(ObjUtil.isAllEquals(false, true, "test", null, null));
        // TODO 此处抛出空指针异常
        // assertFalse(ObjUtil.isAllEquals(false, false, "test", null, null));
        assertFalse(ObjUtil.isAllEquals(false, false, "test", 123, 123.0));
        assertFalse(ObjUtil.isAllEquals(true, false, "test", 123, 123.0));
        assertFalse(ObjUtil.isAllEquals(true, true, "test", 123, 123.0, null));
        assertTrue(ObjUtil.isAllEquals(false, false, "test", "test", "test"));
        assertTrue(ObjUtil.isAllEquals(true, false, "test", "test", "test"));
        assertFalse(ObjUtil.isAllEquals(true, false, "test", 123, 123.0));
        assertThrows(IllegalArgumentException.class, () -> ObjUtil.isAllEquals(false, false, "test"));
    }
}
