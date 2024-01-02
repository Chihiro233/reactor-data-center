package pers.nanachi.reactor.datacer.sdk.excel.core.netty;


import com.alibaba.fastjson2.JSONArray;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacenter.common.util.AssertUtil;
import pers.nanachi.reactor.datacer.sdk.excel.core.*;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeEnum;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskDispatcher;

import java.util.List;


@ChannelHandler.Sharable
@NoArgsConstructor
@Slf4j
public class DataProcessServiceHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    @Resource
    private TaskDispatcher taskDispatcher;


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
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {

        MessageProtocol.ProtocolHeader header = msg.getHeader();

        try{
            Object ret = taskDispatcher.route(header.getTaskType())
                    .handle(msg);

            RpcResponse<?> response = RpcResponse.builder()
                    .data(ret).build();

            MessageProtocol.ProtocolHeader respHeader = new MessageProtocol.ProtocolHeader();
            respHeader.setTaskType(header.getTaskType())
                    .setMsgId(1L);

            MessageProtocol.MessageProtocolBuilder resp = MessageProtocol.builder()
                    .command(CommandType.Resp)
                    .header(respHeader)
                    .data(SerializeFactory.serialize(SerializeEnum.FASTJSON2, response));

            ctx.writeAndFlush(resp);
        }finally {

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
                MessageProtocol resp = MessageProtocol.buildRespData(jsonArray, MessageProtocol.RespCode.SUCCESS);
                ctx.writeAndFlush(resp);
            }
            case ExportExecuteStage._getData -> {
                List<?> exportData = baseExcelExportHandler.getExportData0(pageNo, param);
                MessageProtocol resp = MessageProtocol.buildRespData(exportData, MessageProtocol.RespCode.SUCCESS);
                ctx.writeAndFlush(resp);
            }
        }
    }

    private void executeImport(ChannelHandlerContext ctx, ExcelImportHandler<?> importHandler, byte[] data) {
        BaseExcelImportHandler<?> baseExcelImportHandler = (BaseExcelImportHandler<?>) importHandler;
        baseExcelImportHandler.importExecute(data);
    }


}
