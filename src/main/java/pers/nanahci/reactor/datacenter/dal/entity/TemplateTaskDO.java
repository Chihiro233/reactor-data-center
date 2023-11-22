package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("template_task")
public class TemplateTaskDO {

    @Id
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
     * 错误文件地址
     */
    private String errFileUrl;
    /**
     *
     */
    private String title;
    /**
     * custom business identify, give by the business service then a task is created
     */
    private String bizInfo;
    /**
     * task status. 0 - no start 1-processing 2-complete
     */
    private Integer status;

}
