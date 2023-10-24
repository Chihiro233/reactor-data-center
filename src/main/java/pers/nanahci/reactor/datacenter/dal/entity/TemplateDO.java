package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("template")
public class TemplateDO {

    @Id
    private Long id;

    private String sid;

    private String serverName;

    private String uri;

    private Integer rpcType;

    private String extendInfo;

    private Integer callbackFlag;

    private String callbackServerName;

    private String callbackUri;

}
