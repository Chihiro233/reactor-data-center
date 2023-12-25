package pers.nanahci.reactor.datacenter.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.service.task.TemplateService;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TemplateTaskJobHandler {

    @Resource
    private TemplateService templateService;

    @XxlJob("taskExecutorJob")
    public void taskExecutorJob() {
        log.info("start execution of pending job tasks");
        templateService
                .getUnComplete()
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("no pending job");
                    return Mono.empty();
                }))
                .subscribe(model -> templateService.execute(model));
    }

    @XxlJob("timeoutTaskJob")
    public void timeoutTaskJob() {
        log.info("start searching timeout task");
        templateService
                .getTimeoutTask()
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("no timeout task");
                    return Mono.empty();
                }))
                .subscribe(model -> templateService.resolveTimeoutTask(model));
    }

}
