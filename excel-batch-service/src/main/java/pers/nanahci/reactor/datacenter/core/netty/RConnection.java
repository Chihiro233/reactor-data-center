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

@Getter
@Slf4j
public class RConnection {

    private final int sinkId;


    private final Connection connection;

    public RConnection(int sinkId, Connection connection) {
        this.sinkId = sinkId;
        this.connection = connection;
    }

    public void openInbound() {
        // test order
        connection.onDispose()
                .subscribe();
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
                    connection.dispose();
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
