package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("template_task")
public class TemplateTaskDO {


    private Long id;

    private String batchNo;

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

    /**
     * 模版链接
     */
    private String fileUrl;

    /**
     * key字段名，用于解析记录批处理明细表的key字段值
     */
    private String keyVarName;

    /**
     * 是否需要聚合失败数据EXCEL（0-否，1-是）
     */
    private Integer needFailExcel;

    /**
     * 是否需要批处理执行结束后回调（0-否，1-是）
     */
    private Integer needCallback;


}
