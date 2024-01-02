package pers.nanahci.reactor.datacenter.core.netty;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.CommandType;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;

@ChannelHandler.Sharable
@Slf4j
@Component
public class DataProcessClientHandler extends SimpleChannelInboundHandler<MessageProtocol> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {
        byte[] data = msg.getData();
        String str = JSON.parseObject(data, String.class);
        log.info("客户端接收到数据:{}", str);
        dispatch(ctx, msg);
        // TODO 对数据做处理
    }

    private void dispatch(ChannelHandlerContext ctx, MessageProtocol msg) {
        switch (msg.getCommand()) {
            case CommandType.Req -> {
                // TODO

            }
            case CommandType.Resp -> {

            }
        }
    }

    private void resolveResp(MessageProtocol msg) {

    }

    private void resolveReq(MessageProtocol msg) {
    }
}
