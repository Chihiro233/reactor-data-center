package pers.nanachi.reactor.datacer.sdk.excel.core.task;


import pers.nanachi.reactor.datacenter.common.util.AssertUtil;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskDispatcher {

    private final Map<Integer, TaskProcessor> typeToProcessor = new ConcurrentHashMap<>();

    public TaskDispatcher(List<TaskProcessor> taskProcessors) {
        for (TaskProcessor taskProcessor : taskProcessors) {
            typeToProcessor.put(taskProcessor.type(), taskProcessor);
        }
    }

    public TaskProcessor route(Integer taskType) {
        return AssertUtil.requireNonNull(() -> typeToProcessor.get(taskType), () -> new RuntimeException("task type isn't exist"));
    }



}
