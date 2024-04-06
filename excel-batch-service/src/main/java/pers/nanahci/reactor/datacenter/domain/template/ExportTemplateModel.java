package pers.nanahci.reactor.datacenter.domain.template;

import com.alibaba.excel.util.StringUtils;
import lombok.Getter;
import pers.nanahci.reactor.datacenter.service.task.constant.ExportTypeEnum;


@Getter
public class ExportTemplateModel extends TemplateModel{


    private final ExportTypeEnum exportTypeEnum;

    public ExportTemplateModel(TemplateModel templateModel) {
        super(templateModel);
        String mode = this.config.getMode();
        if(StringUtils.isBlank(mode)){
            this.exportTypeEnum = ExportTypeEnum.NONE;
            return;
        }
        this.exportTypeEnum = ExportTypeEnum.of(mode);

    }

    public ExportTypeEnum getExportType(){
        return exportTypeEnum;
    }

    public String getTemplateUrl(){
        return this.getConfig().getTemplateUrl();
    }

}
