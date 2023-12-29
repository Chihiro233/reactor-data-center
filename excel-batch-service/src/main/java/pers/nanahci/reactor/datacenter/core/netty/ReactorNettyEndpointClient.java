package pers.nanahci.reactor.datacenter.core.netty;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.core.EventExecutorPoll;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.*;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

@Slf4j
@Component
public class ReactorNettyEndpointClient {


    @Resource
    private LoadBalancerClientFactory loadBalancerClientFactory;

    private final DataChannelManager dataChannelManager = new DataChannelManager();


    public Mono<byte[]> execute(String serviceId, DataMessage dataMessage) {
        ReactiveLoadBalancer<ServiceInstance> instance =
                loadBalancerClientFactory.getInstance(serviceId);
        // 将消息转换成
        return Mono.from(instance.choose())
                .flatMap(response -> {
                    ServiceInstance serverInstance = response.getServer();
                    String host = serverInstance.getHost();
                    return TcpClient.create()
                            .host(host)
                            .port(9896)
                            .runOn(LoopResources.create("rexcel-req-client"))
                            .doOnChannelInit((connectionObserver, channel, remoteAddress) -> initPipeline(channel.pipeline()))
                            .doOnConnected(this::initConnection)
                            .wiretap(true)
                            .connect()
                            .doOnSuccess(connection -> {
                                log.info("connect success!!!");
                            })
                            .flatMap(connection -> {
                                if (connection.isDisposed()) {
                                    log.error("connect error");
                                    return Mono.empty();
                                }
                                setupOutbound(connection, dataMessage);
                                return setupInbound(connection);
                            });
                });
    }


    public Mono<byte[]> setupInbound(Connection connection) {
        return connection.inbound()
                .receiveObject()
                .take(1)
                .last()
                .doOnNext(value -> {
                    log.info("inbound next value is {}", value);
                })
                .onErrorResume(t -> {
                    log.info("client connect error:", t);
                    return Mono.empty();
                })
                .cast(DataMessage.class)
                .flatMap(dataMessage -> {
                    log.info("receive message :[{}]", JSON.toJSONString(dataMessage));
                    return dispatch(dataMessage);
                }).doFinally(signalType -> {
                    connection.dispose();
                });
    }

    public void setupOutbound(Connection connection, Object dataMessage) {
        connection.outbound()
                .sendObject(dataMessage)
                .then().subscribe();
    }

    private Mono<byte[]> dispatch(DataMessage msg) {
        switch (msg.getCommand()) {
            case CommandType.Req -> {
                // TODO
                return Mono.empty();
            }
            case CommandType.Resp -> {
                // 1. check whether success
                if (!msg.whetherSuccess()) {
                    return Mono.error(new RuntimeException("export request fail"));
                }
                // 2. start to check
                return Mono.just(msg.getData());
            }
        }
        log.info("no any message");
        return Mono.empty();
    }

    @SneakyThrows
    public static void main(String[] args) {

        DataChannelManager dataChannelManager = new DataChannelManager();
        DataProcessClientHandler dataProcessClientHandler = new DataProcessClientHandler();
        DataProcessServiceHandler dataProcessServiceHandler = new DataProcessServiceHandler();

        DataMessage dataMessage = DataMessage.buildReqData(JSONB.toBytes("测试啊"));
        DisposableServer disposableServer = TcpServer.create()
                .host("127.0.0.1")
                .port(9494)
                .wiretap(true)
                .doOnConnection(conn -> {
                    conn.addHandlerLast(new DataDecoder(NettyCoreConfig.maxFrameLength,
                            NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                            NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip));
                    conn.addHandlerLast(new DataEncoder());
                    conn.addHandlerLast(dataChannelManager);
                    conn.addHandlerLast(new IdleStateHandler(0, 0,
                            NettyCoreConfig.maxIdleTime));
                    conn.addHandlerLast(dataProcessServiceHandler);
                })
                // .doOnChannelInit((connectionObserver, channel, remoteAddress) -> {
                //     ChannelPipeline pipeline = channel.pipeline();
                //     pipeline.addLast(EventExecutorPoll.DEFAULT_EVENT_EXECUTOR,
                //             new DataDecoder(NettyCoreConfig.maxFrameLength,
                //                     NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                //                     NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip),
                //             new DataEncoder(),
                //             dataChannelManager,
                //             new IdleStateHandler(0, 0,
                //                     NettyCoreConfig.maxIdleTime),
                //             dataProcessServiceHandler);
                // })
                .handle((in, out) -> in.receiveObject()
                        .ofType(DataMessage.class)
                        .doOnNext(d -> {
                            log.info("接收到数据:{}", d);
                        }).then())
                .bindNow();
        // TcpClient
        Connection connection = TcpClient.create()
                .host("127.0.0.1")
                .port(9494)
                //.doOnConnected(conn -> {
                //    conn.addHandlerLast(new DataDecoder(NettyCoreConfig.maxFrameLength,
                //            NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                //            NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip));
                //    conn.addHandlerLast(new DataEncoder());
                //    conn.addHandlerLast(dataChannelManager);
                //    conn.addHandlerLast(new IdleStateHandler(0, 0,
                //            NettyCoreConfig.maxIdleTime));
                //    conn.addHandlerLast(dataProcessClientHandler);
                //})
                .doOnChannelInit((connectionObserver, channel, remoteAddress) -> {
                    if (channel.parent() != null) {
                        ChannelPipeline pipeline = channel.parent().pipeline();
                        initPipeline(pipeline, dataChannelManager, dataProcessClientHandler);
                    }
                    ChannelPipeline pipeline = channel.pipeline();
                    initPipeline(pipeline, dataChannelManager, dataProcessClientHandler);
                })
                .connectNow();

        connection.outbound()
                .sendObject(dataMessage).then().subscribe();
        connection.inbound()
                .receiveObject()
                .ofType(DataMessage.class)
                .doOnNext(d -> {
                    log.info("msg is:{}", d);
                }).subscribe();


        Thread.sleep(1000000L);
    }

    private static void initPipeline(ChannelPipeline pipeline,
                                     DataChannelManager dataChannelManager, DataProcessClientHandler dataProcessClientHandler) {
        pipeline.addLast(EventExecutorPoll.DEFAULT_EVENT_EXECUTOR,
                new DataDecoder(NettyCoreConfig.maxFrameLength,
                        NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                        NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip),
                new DataEncoder(),
                dataChannelManager,
                new IdleStateHandler(0, 0,
                        NettyCoreConfig.maxIdleTime),
                dataProcessClientHandler);
    }

    private void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(EventExecutorPoll.DEFAULT_EVENT_EXECUTOR,
                new DataDecoder(NettyCoreConfig.maxFrameLength,
                        NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                        NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip),
                new DataEncoder(),
                dataChannelManager,
                new IdleStateHandler(0, 0,
                        NettyCoreConfig.maxIdleTime));
    }

    private void initConnection(Connection conn) {
        conn.addHandlerLast(new DataDecoder(NettyCoreConfig.maxFrameLength,
                NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip));
        conn.addHandlerLast(new DataEncoder());
        conn.addHandlerLast(dataChannelManager);
        conn.addHandlerLast(new IdleStateHandler(0, 0,
                NettyCoreConfig.maxIdleTime));
        //conn.addHandlerLast(dataProcessClientHandler);
    }

}
