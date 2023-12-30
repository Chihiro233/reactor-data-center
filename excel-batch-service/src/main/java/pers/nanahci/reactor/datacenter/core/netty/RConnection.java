package pers.nanahci.reactor.datacenter.core.netty;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataMessage;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;

@Getter
@Slf4j
public class RConnection {

    private int sinkId;


    private Connection connection;

    public RConnection(int sinkId, Connection connection) {
        this.sinkId = sinkId;
        this.connection = connection;
    }

    public void openInbound() {
        connection.inbound()
                .receiveObject()
                .cast(DataMessage.class).take(1)
                .doOnNext((value)->{
                    Sinks.One<DataMessage> inboundSink = EndPointSinkPoll.remove(sinkId);
                    if (inboundSink == null) {
                        return;
                    }
                    inboundSink.tryEmitValue(value);
                })
                .onErrorResume(t -> {
                    log.error("error info", t);
                    return Mono.empty();
                })
                .doFinally(signalType -> {
                    connection.dispose();
                })
                .subscribe();
        connection.onDispose()
                .subscribe();
    }

    public boolean isDisposed() {
        return connection.isDisposed();
    }

    public void handleRequest(Object obj) {
        connection.outbound().sendObject(obj)
                .then().subscribe();
    }


}
