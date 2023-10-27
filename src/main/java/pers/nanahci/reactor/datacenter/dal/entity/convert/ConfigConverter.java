package pers.nanahci.reactor.datacenter.dal.entity.convert;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;

public class ConfigConverter implements PropertyValueConverter {


    @Override
    public TemplateDO.Config read(Object value, ValueConversionContext context) {
        if (value instanceof String) {
            return JSON.parseObject((String) value, TemplateDO.Config.class);
        }
        return null;
    }

    @Override
    public String write(Object value, ValueConversionContext context) {
        return JSON.toJSONString(value);
    }

}
