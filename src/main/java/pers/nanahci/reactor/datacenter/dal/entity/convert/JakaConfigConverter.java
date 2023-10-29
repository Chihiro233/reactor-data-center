package pers.nanahci.reactor.datacenter.dal.entity.convert;

import com.alibaba.fastjson2.JSON;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;

@Converter
public class JakaConfigConverter implements AttributeConverter<TemplateDO.Config,String> {

    @Override
    public String convertToDatabaseColumn(TemplateDO.Config config) {
        return JSON.toJSONString(config);
    }

    @Override
    public TemplateDO.Config convertToEntityAttribute(String s) {
        return JSON.parseObject(s, TemplateDO.Config.class);
    }
}
