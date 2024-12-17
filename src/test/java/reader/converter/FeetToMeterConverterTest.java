package reader.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeetToMeterConverterTest {
    @Test
    void testConvertFeetToMeters() throws Exception {
        FeetToMeterConverter converter = new FeetToMeterConverter();

        // 테스트 입력
        assertEquals(0.3048, converter.convert("1"));  // 1 feet -> meters
        assertEquals(3.048, converter.convert("10"));  // 10 feet -> meters
        assertEquals(30.48, converter.convert("100")); // 100 feet -> meters
    }

    @Test
    void testConvertNullOrEmptyValue() throws Exception {
        FeetToMeterConverter converter = new FeetToMeterConverter();

        // null이나 빈 값은 null 반환
        assertNull(converter.convert(null));
        assertNull(converter.convert(" "));
    }
}