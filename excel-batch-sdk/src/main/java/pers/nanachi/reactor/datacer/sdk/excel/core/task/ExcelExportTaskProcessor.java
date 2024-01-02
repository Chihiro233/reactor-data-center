package pers.nanachi.reactor.datacer.sdk.excel.core.task;

import jakarta.annotation.Resource;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;

public class ExcelExportTaskProcessor implements TaskProcessor {

    @Resource
    private ExcelHandlerFactory excelHandlerFactory;


    @Override
    public Object handle(MessageProtocol messageProtocol) {
        Object data = messageProtocol.getData();
        excelHandlerFactory.getExportHandler();
        return null;
    }

    @Override
    public Integer type() {
        return TaskTypeRecord.EXPORT_TASK;
    }
}
