package pers.nanachi.reactor.datacer.sdk.excel.core.task;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.BaseExcelExportHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;
import pers.nanachi.reactor.datacer.sdk.excel.param.ExcelTaskRequest;


public class ExcelExportTaskTypeProcessor implements TaskTypeProcessor {

    @Resource
    private ExcelHandlerFactory excelHandlerFactory;


    @Override
    public Object handle(MessageProtocol messageProtocol) {
        byte[] data = messageProtocol.getData();
        ExcelTaskRequest req = JSON.parseObject(data, ExcelTaskRequest.class);
        BaseExcelExportHandler<?,?> exportHandler = excelHandlerFactory.getExportHandler(req.getTaskName());
        switch (req.getStage()) {
            case ExportExecuteStage._getHead -> {
                return exportHandler.getExcelHeaders0(req.getBizInfo());
            }
            case ExportExecuteStage._getData -> {
                return exportHandler.getExportData0(req.getPageNo(), req.getBizInfo());
            }
        }
        throw new RuntimeException("illegal stage");
    }

    @Override
    public Integer type() {
        return TaskTypeRecord.EXPORT_TASK;
    }


}
