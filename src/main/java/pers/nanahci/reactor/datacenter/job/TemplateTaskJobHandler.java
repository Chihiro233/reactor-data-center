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
        log.info("开始执行未完成的job任务");
        templateService
                .getUnComplete()
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("没有未完成的任务");
                    return Mono.empty();
                }))
                .subscribe(model -> templateService.execute(model));
    }

    @XxlJob("timeoutTaskJob")
    public void timeoutTaskJob() {
        log.info("开始搜索超时job任务");
        templateService
                .getTimeoutTask()
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("没有未完成的任务");
                    return Mono.empty();
                }))
                .subscribe(model -> templateService.resolveTimeoutTask(model));
    }

}
