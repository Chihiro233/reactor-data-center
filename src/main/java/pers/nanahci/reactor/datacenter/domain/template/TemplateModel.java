package pers.nanahci.reactor.datacenter.domain.template;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.core.task.FailStrategy;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
@Data
@Builder
public class TemplateModel {


    private TemplateDO templateDO;

    private List<TemplateTaskDO> taskList;


    public TemplateModel(TemplateDO templateDO, List<TemplateTaskDO> taskList) {
        this.templateDO = templateDO;
        this.taskList = taskList;
    }

    public boolean whetherRetry() {
        if (templateDO == null) {
            throw new RuntimeException("template is null");
        }
        Integer failStrategy = templateDO.getFailStrategy();
        return Objects.equals(failStrategy, FailStrategy.RETRY);
    }

    public Integer getMaxRetry(){
        if(templateDO == null){
            throw new RuntimeException("template is null");
        }
        return templateDO.getMaxRetry();
    }


}
