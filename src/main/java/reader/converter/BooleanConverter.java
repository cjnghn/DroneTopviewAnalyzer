package reader.converter;

import com.opencsv.bean.AbstractBeanField;

public class BooleanConverter extends AbstractBeanField<String, Boolean> {
    @Override
    protected Boolean convert(String value) {
        if (value == null || (!value.equals("1") && !value.equals("0"))) {
            return null; // 잘못된 값은 null 반환
        }
        return "1".equals(value); // "1"이면 true, "0"이면 false
    }
}
