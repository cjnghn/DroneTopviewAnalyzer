package reader.converter;

import com.opencsv.bean.AbstractBeanField;

public class FeetToMeterConverter extends AbstractBeanField<String, Double> {
    private static final double FEET_TO_METER = 0.3048;

    @Override
    protected Double convert(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Double.parseDouble(value) * FEET_TO_METER;
    }
}
