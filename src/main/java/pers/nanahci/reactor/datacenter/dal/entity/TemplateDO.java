package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ValueConverter;
import org.springframework.data.relational.core.mapping.Table;
import pers.nanahci.reactor.datacenter.dal.entity.convert.ConfigConverter;


@Data
@Table("template")
public class TemplateDO {

    @Id
    private Long id;

    private String sid;

    private String serverName;

    private String uri;

    private Integer rpcType;

    @ValueConverter(ConfigConverter.class)
    private Config config;


    @Data
    public static class Config {

        private String webHook;

        private String userId;

        private String phone;

        private String token;

        private Integer type;


    }


}
