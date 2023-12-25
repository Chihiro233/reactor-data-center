package pers.nanahci.reactor.datacenter.service.task.constant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;

@Component
@Slf4j
public class FailStrategy {

    public static final int NONE = 0;

    public static final int RETRY = 1;

    /**
     * retry
     */
    public void retry(TemplateTaskDO taskDO) {
        log.info("execute the [RETRY] strategy,taskId: [{}]", taskDO.getId());
    }

    /**
     * no action, just tag the task terminal
     */
    public void none(TemplateTaskDO taskDO) {
        log.info("execute the [NONE] strategy,taskId: [{}]", taskDO.getId());

    }


}
