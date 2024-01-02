package pers.nanachi.reactor.datacer.sdk.excel.core.task;

import jakarta.annotation.Resource;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;
import pers.nanachi.reactor.datacer.sdk.excel.param.ExcelTaskRequest;

public class ExcelImportTaskProcessor implements TaskProcessor {

    @Resource
    private ExcelHandlerFactory excelHandlerFactory;

    @Override
    public Object handle(MessageProtocol messageProtocol) {
        byte[] data = messageProtocol.getData();

        return null;
    }

    @Override
    public Integer type() {
        return TaskTypeRecord.IMPORT_TASK;
    }
}
