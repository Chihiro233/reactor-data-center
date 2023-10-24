package pers.nanahci.reactor.datacenter.domain.template.convert;

import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;

public class TemplateConvert {

    public static TemplateModel convertTemplateModel(TemplateDO templateDO){

        return new TemplateModel(templateDO);

    }


}
