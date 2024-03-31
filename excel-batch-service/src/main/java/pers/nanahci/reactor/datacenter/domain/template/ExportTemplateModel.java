package pers.nanahci.reactor.datacenter.domain.template;

import lombok.Getter;
import pers.nanahci.reactor.datacenter.service.task.constant.ExportTypeEnum;


@Getter
public class ExportTemplateModel extends TemplateModel{


    private ExportTypeEnum exportTypeEnum;

    public ExportTemplateModel(TemplateModel templateModel) {
        super(templateModel);
        String mode = this.config.getMode();
        this.exportTypeEnum = ExportTypeEnum.valueOf(mode);

    }

    public ExportTypeEnum getExportType(){
        return exportTypeEnum;
    }

    public String getTemplateUrl(){
        return this.getConfig().getTemplateUrl();
    }

}
