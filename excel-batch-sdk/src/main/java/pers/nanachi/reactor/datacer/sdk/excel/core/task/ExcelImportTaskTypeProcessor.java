package pers.nanachi.reactor.datacer.sdk.excel.core.task;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.BaseExcelImportHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;
import pers.nanachi.reactor.datacer.sdk.excel.param.ExcelTaskRequest;

public class ExcelImportTaskTypeProcessor implements TaskTypeProcessor {

    @Resource
    private ExcelHandlerFactory excelHandlerFactory;

    @Override
    public Object handle(MessageProtocol messageProtocol) {
        byte[] data = messageProtocol.getData();
        ExcelTaskRequest request = JSON.parseObject(data, ExcelTaskRequest.class);
        BaseExcelImportHandler<?> importHandler = excelHandlerFactory.getImportHandler(request.getTaskName());
        importHandler.importExecute(request.getBizInfo());
        return null;
    }

    @Override
    public Integer type() {
        return TaskTypeRecord.IMPORT_TASK;
    }
}
