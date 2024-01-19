package pers.nanahci.reactor.datacenter.core.netty;


import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.EventExecutorPoll;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataChannelManager;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataDecoder;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataEncoder;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.NettyCoreConfig;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;


@Slf4j
public class ConnectionManager {

    private static final ConnectionPool connectionPool = new ConnectionPool();

    private final static DataChannelManager dataChannelManager = new DataChannelManager();


    public interface Recycle {

        void recycle(RConnectionHolder connection);

    }

    private static final Recycle NOOP_RECYCLE = new Recycle() {
        @Override
        public void recycle(RConnectionHolder connection) {
            // do nothing
        }
    };

    private static final Recycle NORMAL_RECYCLE = new Recycle() {
        @Override
        public void recycle(RConnectionHolder connection) {
            if (connection.isDisposed()) {
                return;
            }
            connectionPool.add(connection);
        }
    };


    public static Mono<RConnectionHolder> get(String host) {
        // 从池子拿
        return Mono.fromSupplier(() -> connectionPool.poll(host))
                .switchIfEmpty(
                        Mono.defer(() -> TcpClient.create()
                                        .host(host)
                                        .port(9896)
                                        .runOn(LoopResources.create("rexcel-req-client"))
                                        .doOnChannelInit((connectionObserver, channel, remoteAddress) -> initPipeline(channel.pipeline()))
                                        .doOnConnected(ConnectionManager::initConnection)
                                        .wiretap(true)
                                        .connect()).map(connection ->
                                        new RConnectionHolder(connection, NORMAL_RECYCLE)

                                )
                                .doOnSuccess(connection -> {
                                    log.info("connect success,channel id:[{}]", connection.getConnection().channel().id());
                                })
                );


    }

    private static void initPipeline(ChannelPipeline pipeline) {
        pipeline.addFirst(new IdleStateHandler(0, 0,
                NettyCoreConfig.maxIdleTime), dataChannelManager);
        pipeline.addLast(EventExecutorPoll.DEFAULT_EVENT_EXECUTOR,
                new DataDecoder(NettyCoreConfig.maxFrameLength,
                        NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                        NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip),
                new DataEncoder());
    }

    private static void initConnection(Connection conn) {
        conn.addHandlerFirst(new IdleStateHandler(0, 0,
                NettyCoreConfig.maxIdleTime));
        conn.addHandlerLast(dataChannelManager);
        conn.addHandlerLast(new DataDecoder(NettyCoreConfig.maxFrameLength,
                NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip));
        conn.addHandlerLast(new DataEncoder());
    }


}
