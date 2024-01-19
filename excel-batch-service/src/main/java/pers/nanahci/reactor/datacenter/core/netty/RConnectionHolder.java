package pers.nanahci.reactor.datacenter.core.netty;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.CommandType;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcRequest;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcResponse;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeEnum;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;

@Getter
@Slf4j
public class RConnectionHolder {


    private final String remoteHost;

    private final Connection connection;

    private final ConnectionManager.Recycle recycler;

    private final LocalDateTime createTime;


    public RConnectionHolder(Connection connection, ConnectionManager.Recycle recycler) {
        this.connection = connection;
        InetSocketAddress socketAddress = (InetSocketAddress) connection.channel().remoteAddress();
        this.remoteHost = socketAddress.getHostName();
        this.recycler = recycler;
        this.createTime = LocalDateTime.now();

        openInbound();
    }

    protected void openInbound() {

        connection.onDispose(() -> {
            log.info("channel close");
        });

        connection.inbound()
                .receiveObject()
                .cast(MessageProtocol.class)
                .doOnNext((protocol) -> {
                    long msgId = protocol.getHeader().getMsgId();
                    Sinks.One<RpcResponse<?>> inboundSink = RequestSinkPoll.remove(msgId);
                    if (inboundSink == null) {
                        return;
                    }
                    RpcResponse<?> rpcResponse = JSON.parseObject(protocol.getData(), RpcResponse.class);
                    inboundSink.tryEmitValue(rpcResponse);
                    // recycle
                })
                .onErrorResume(t -> {
                    log.error("error info", t);
                    return Mono.empty();
                })
                .subscribe();

    }


    public boolean isDisposed() {
        return connection.isDisposed();
    }

    public void recycle() {
        recycler.recycle(this);
    }

    public long handleRequest(RpcRequest<?> request) {
        MessageProtocol.MessageProtocolBuilder builder
                = MessageProtocol.builder();
        MessageProtocol.ProtocolHeader header = new MessageProtocol.ProtocolHeader();
        long requestId = RequestSinkPoll.alloc();
        header.setTaskType(request.getAttach().getTaskType());
        header.setMsgId(requestId);
        builder.command(CommandType.Req);
        builder.data(SerializeFactory.serialize(SerializeEnum.FASTJSON2, request.getData()));
        builder.header(header);

        connection.outbound()
                .sendObject(builder.build())
                .then()
                .subscribe();
        return requestId;
    }


}
