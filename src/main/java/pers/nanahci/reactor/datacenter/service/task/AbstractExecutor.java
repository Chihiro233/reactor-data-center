package pers.nanahci.reactor.datacenter.service.task;

import org.apache.commons.lang3.StringUtils;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.util.FileUtils;
import reactor.core.publisher.Mono;

public abstract class AbstractExecutor {

    protected String getErrorFileNameFromUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return "未命名" + System.currentTimeMillis() + ".xlsx";
        }
        String fileName = FileUtils.getFileNameNoExtension(url);
        return fileName + System.currentTimeMillis() + ".xlsx";
    }


    public abstract Mono<Integer> execute(TemplateTaskDO task, TemplateModel templateModel);


}
