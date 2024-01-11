package pers.nanachi.reactor.datacer.sdk.excel.core.task;


import pers.nanachi.reactor.datacenter.common.util.AssertUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskDispatcher {

    private final Map<Integer, TaskTypeProcessor> typeToProcessor = new ConcurrentHashMap<>();

    public TaskDispatcher(List<TaskTypeProcessor> taskTypeProcessors) {
        for (TaskTypeProcessor taskTypeProcessor : taskTypeProcessors) {
            typeToProcessor.put(taskTypeProcessor.type(), taskTypeProcessor);
        }
    }

    public TaskTypeProcessor route(Integer taskType) {
        return AssertUtil.requireNonNull(() -> typeToProcessor.get(taskType), () -> new RuntimeException("task type isn't exist"));
    }



}
