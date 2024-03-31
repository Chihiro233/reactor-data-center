package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("template")
public class TemplateDO {

    @Id
    private Long id;

    private String batchNo;

    private String name;

    private String serverName;

    private String uri;

    private Integer rpcType;

    private Integer type;
    /**
     * 执行方式（1-逐行调用，2-批量调用）
     */
    private Integer executeType;

    private Integer batchSize;
    /**
     * 0-none  1-retry
     */
    private Integer failStrategy;

    private Integer maxRetry;

    private String config;

    private Long timeoutLimit;


    @Data
    public static class Config {

        private String mode;

        private String templateUrl;

        private String webHook;

        private String userId;

        private String phone;

        private String token;

        private Integer type;

        private String script;

        private Integer scriptType;


    }


}
