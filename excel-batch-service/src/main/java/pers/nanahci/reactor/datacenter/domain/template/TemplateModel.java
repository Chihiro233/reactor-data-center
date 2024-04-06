package pers.nanahci.reactor.datacenter.domain.template;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacenter.common.util.AssertUtil;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.service.task.constant.ExecuteTypeEnum;
import pers.nanahci.reactor.datacenter.service.task.constant.FailStrategy;

import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class TemplateModel {


    protected TemplateDO templateDO;

    protected TemplateDO.Config config;

    protected List<TemplateTaskDO> taskList;


    public TemplateModel(TemplateDO templateDO, List<TemplateTaskDO> taskList) {

        AssertUtil.requireNonNull(()->templateDO != null,IllegalArgumentException::new);
        AssertUtil.requireNonNull(()->taskList != null,IllegalArgumentException::new);

        this.templateDO = templateDO;
        this.taskList = taskList;
        this.config = JSON.parseObject(templateDO.getConfig(), TemplateDO.Config.class);

    }

    public TemplateModel(TemplateModel templateModel) {
        AssertUtil.requireNonNull(()->templateDO != null, IllegalArgumentException::new);

        this.templateDO = templateModel.getTemplateDO();
        this.taskList = templateModel.getTaskList();
        this.config = templateModel.getConfig();
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

    /**
     * @return
     */
    public Integer getTaskType(){
        if(templateDO == null){
            throw new RuntimeException("template is null");
        }
        return templateDO.getType();
    }

    public String getServerName(){
        return templateDO.getServerName();
    }

    public String getFileUrl(){
        return templateDO.getUri();
    }


    public boolean isBatch(){
        return Objects.equals(ExecuteTypeEnum.Batch.getValue(), this.templateDO.getExecuteType());
    }

    public Integer getBatchSize(){
        return templateDO.getBatchSize();
    }


    public String getName() {
        return templateDO.getName();
    }



}
