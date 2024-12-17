package reader.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanConverterTest {

    @Test
    void testConvertBooleanValues() throws Exception {
        BooleanConverter converter = new BooleanConverter();

        // 테스트 입력
        assertTrue(converter.convert("1"));
        assertFalse(converter.convert("0"));
    }

    @Test
    void testConvertInvalidOrNullValues() throws Exception {
        BooleanConverter converter = new BooleanConverter();

        // 잘못된 값은 false로 간주하지 않음
        assertNull(converter.convert(null));
        assertNull(converter.convert(" "));
        assertNull(converter.convert("invalid"));
    }
}