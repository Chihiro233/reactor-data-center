package pers.nanahci.reactor.datacenter.domain.template;

import lombok.Getter;
import pers.nanachi.reactor.datacenter.common.util.AssertUtil;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;

@Getter
public class TemplateTaskModel {

    private final TemplateTaskDO taskDO;

    private final TemplateModel templateModel;

    public TemplateTaskModel(TemplateTaskDO templateTaskDO, TemplateModel templateModel){
        AssertUtil.requireNonNull(()->templateTaskDO!=null,IllegalArgumentException::new);
        AssertUtil.requireNonNull(()->templateModel!=null,IllegalArgumentException::new);


        this.taskDO = templateTaskDO;
        this.templateModel = templateModel;
    }


    public String getFileUrl() {
        return taskDO.getFileUrl();
    }

    public Long getId() {
        return taskDO.getId();
    }

    public String getBizInfo() {
        return taskDO.getBizInfo();
    }

    public String getBatchNo() {
        return taskDO.getBatchNo();
    }
}
