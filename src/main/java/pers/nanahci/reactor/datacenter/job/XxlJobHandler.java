package pers.nanahci.reactor.datacenter.job;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.service.TemplateService;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class XxlJobHandler {

    @Resource
    private TemplateService templateService;

    public void execute() {
        templateService
                .getUnComplete()
                .subscribe(s -> templateService.execute(s));

    }

}
