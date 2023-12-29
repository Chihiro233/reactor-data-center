package pers.nanachi.reactor.datacer.sdk.excel.core.netty;


import com.alibaba.fastjson2.JSONArray;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacenter.common.util.AssertUtil;
import pers.nanachi.reactor.datacer.sdk.excel.core.*;

import java.util.List;


@ChannelHandler.Sharable
@NoArgsConstructor
@Slf4j
public class DataProcessServiceHandler extends SimpleChannelInboundHandler<DataMessage> {

    private ExcelHandlerFactory excelHandlerFactory;

    public DataProcessServiceHandler(ExcelHandlerFactory excelHandlerFactory) {
        this.excelHandlerFactory = excelHandlerFactory;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channel read");
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel active");
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage msg) throws Exception {
        DataMessage.Attach attach = msg.getAttach();
        String taskName = attach.getTaskName();
        Integer stage = attach.getStage();
        Integer pageNo = attach.getPageNo();

        byte[] data = msg.getData();

        switch (attach.getTaskType()) {
            case TaskTypeRecord.IMPORT_TASK -> {
                ExcelImportHandler<?> importHandler = excelHandlerFactory.getImportHandler(taskName);
                executeImport(ctx, importHandler, data);
            }
            case TaskTypeRecord.EXPORT_TASK -> {
                ExcelExportHandler<?, ?> exportHandler = excelHandlerFactory.getExportHandler(taskName);
                executeExportStage(ctx, exportHandler, stage, data, pageNo);
            }
        }
    }

    private void executeExportStage(ChannelHandlerContext ctx, ExcelExportHandler<?, ?> exportHandler,
                                    Integer stage, byte[] param, Integer pageNo) {
        AssertUtil.isTrue(() -> stage != null, "execute stage can't be null");
        BaseExcelExportHandler<?, ?> baseExcelExportHandler = (BaseExcelExportHandler<?, ?>) exportHandler;
        switch (stage) {
            case ExportExecuteStage._getHead -> {
                List<List<String>> excelHeaders = baseExcelExportHandler.getExcelHeaders0(param);
                JSONArray jsonArray = JSONArray.copyOf(excelHeaders);
                DataMessage resp = DataMessage.buildRespData(jsonArray, DataMessage.RespCode.SUCCESS);
                ctx.writeAndFlush(resp);
            }
            case ExportExecuteStage._getData -> {
                List<?> exportData = baseExcelExportHandler.getExportData0(pageNo, param);
                DataMessage resp = DataMessage.buildRespData(exportData, DataMessage.RespCode.SUCCESS);
                ctx.writeAndFlush(resp);
            }
        }
    }

    private void executeImport(ChannelHandlerContext ctx, ExcelImportHandler<?> importHandler, byte[] data) {
        BaseExcelImportHandler<?> baseExcelImportHandler = (BaseExcelImportHandler<?>) importHandler;
        baseExcelImportHandler.importExecute(data);
    }


}
