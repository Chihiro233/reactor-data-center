package pers.nanahci.reactor.datacenter.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.service.TemplateService;

@Component
@Slf4j
public class XxlJobHandler {

    @Resource
    private TemplateService templateService;

    @XxlJob("batch ")
    public void execute() {
        templateService
                .getUnComplete()
                .subscribe(s -> templateService.execute(s));

    }

}
