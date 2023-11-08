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

    private String serverName;

    private String uri;

    private Integer rpcType;
    /**
     * 请求方式（1-GET，2-POST）
     */
    private Integer methodType;

    /**
     * 执行方式（1-逐行调用，2-批量调用）
     */
    private Integer executeType;
    /**
     * 是否需要执行文件（0-否，1-是）
     */
    private Integer needExeFile;

    private String config;


    @Data
    public static class Config {

        private String webHook;

        private String userId;

        private String phone;

        private String token;

        private Integer type;

        private String script;

        private Integer scriptType;

        private String headList;


    }


}
