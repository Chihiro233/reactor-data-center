package pers.nanachi.reactor.datacer.sdk.excel.core.netty;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeEnum;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskDispatcher;


@ChannelHandler.Sharable
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class DataProcessServiceHandler extends SimpleChannelInboundHandler<MessageProtocol> {

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
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        MessageProtocol retMsg = null;
        MessageProtocol.ProtocolHeader header = msg.getHeader();
        try {
            // find handle by task type
            Object ret = taskDispatcher.route(header.getTaskType())
                    .handle(msg);
            RpcResponse<?> response = RpcResponse.builder()
                    .code(RpcResponse.RespCode.SUCCESS)
                    .data(ret)
                    .build();

            MessageProtocol.ProtocolHeader respHeader = new MessageProtocol.ProtocolHeader();
            respHeader.setTaskType(header.getTaskType())
                    .setMsgId(msg.getHeader().getMsgId() + 1);

            retMsg = MessageProtocol.builder()
                    .command(CommandType.Resp)
                    .header(respHeader)
                    .data(SerializeFactory.serialize(SerializeEnum.FASTJSON2, response)).build();
        } catch (Exception e) {
            // need filer net error
            log.error("request error", e);
            retMsg = handleException(msg, e);

        } finally {
            ctx.writeAndFlush(retMsg);
        }
    }


    private MessageProtocol handleException(MessageProtocol msg, Throwable cause) {
        MessageProtocol.ProtocolHeader header = new MessageProtocol.ProtocolHeader();
        header.setTaskType(msg.getHeader().getTaskType())
                .setMsgId(msg.getHeader().getMsgId() + 1);

        RpcResponse<?> response = RpcResponse.builder()
                .msg(cause.getMessage())
                .code(RpcResponse.RespCode.FAIL).build();

        return MessageProtocol.builder()
                .command(CommandType.Resp)
                .header(header)
                .data(SerializeFactory.serialize(SerializeEnum.FASTJSON2, response)).build();

    }


}
