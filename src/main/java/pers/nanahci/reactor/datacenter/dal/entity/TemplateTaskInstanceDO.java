package pers.nanahci.reactor.datacenter.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("template_task_instance")
public class TemplateTaskInstanceDO {

    private Long id;

    private Long taskId;

    private Integer errRows;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;


}
