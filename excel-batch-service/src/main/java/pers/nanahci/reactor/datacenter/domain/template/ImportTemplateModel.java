package pers.nanahci.reactor.datacenter.domain.template;

import lombok.Getter;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;

import java.util.List;

@Getter
public class ImportTemplateModel extends TemplateModel{
    public ImportTemplateModel(TemplateDO templateDO, List<TemplateTaskDO> taskList) {
        super(templateDO, taskList);
    }

    public ImportTemplateModel(TemplateModel templateModel) {
        super(templateModel);
    }
}
