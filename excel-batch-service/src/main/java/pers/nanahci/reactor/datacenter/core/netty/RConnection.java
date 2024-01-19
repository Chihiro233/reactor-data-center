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
import java.net.SocketAddress;

@Getter
@Slf4j
public class RConnection {

    private final int sinkId;

    private final String remoteHost;

    private final Connection connection;

    private final ConnectionManager.Recycle recycler;


    public RConnection(int sinkId, Connection connection, ConnectionManager.Recycle recycler) {
        this.sinkId = sinkId;
        this.connection = connection;
        InetSocketAddress socketAddress = (InetSocketAddress)connection.channel().remoteAddress();
        this.remoteHost = socketAddress.getHostName();
        this.recycler = recycler;
    }

    public void openInbound() {

        connection.onDispose(()->{
            recycler.recycle(this);
        });

        connection.inbound()
                .receiveObject()
                .cast(MessageProtocol.class).take(1)
                .doOnNext((value) -> {
                    Sinks.One<RpcResponse<?>> inboundSink = EndPointSinkPoll.remove(sinkId);
                    if (inboundSink == null) {
                        return;
                    }
                    RpcResponse<?> rpcResponse = JSON.parseObject(value.getData(), RpcResponse.class);
                    inboundSink.tryEmitValue(rpcResponse);
                })
                .onErrorResume(t -> {
                    log.error("error info", t);
                    return Mono.empty();
                })
                .doFinally(signalType -> {
                    recycler.recycle(this);
                })
                .subscribe();

    }


    public boolean isDisposed() {
        return connection.isDisposed();
    }

    public void handleRequest(RpcRequest<?> request) {
        MessageProtocol.MessageProtocolBuilder builder
                = MessageProtocol.builder();
        MessageProtocol.ProtocolHeader header = new MessageProtocol.ProtocolHeader();
        header.setTaskType(request.getAttach().getTaskType());
        builder.command(CommandType.Req);
        builder.data(SerializeFactory.serialize(SerializeEnum.FASTJSON2, request.getData()));
        builder.header(header);

        connection.outbound()
                .sendObject(builder.build())
                .then()
                .subscribe();
    }


}
