package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("template_task")
public class TemplateTaskDO {


    private Long id;

    /**
     * identify give by template task service
     */
    private String batchNo;


    /**
     * 模版链接
     */
    private String fileUrl;


    /**
     * 是否需要聚合失败数据EXCEL（0-否，1-是）
     */
    private Integer needFailExcel;

    /**
     * 是否需要批处理执行结束后回调（0-否，1-是）
     */
    private Integer needCallback;
    /**
     *
     */
    private String title;
    /**
     * custom business identify, give by the business service then a task is created
     */
    private String bizInfo;

}
