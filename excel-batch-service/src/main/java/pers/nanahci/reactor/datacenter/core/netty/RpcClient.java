package pers.nanahci.reactor.datacenter.core.netty;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.core.EventExecutorPoll;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;

@Slf4j
@Component
public class RpcClient {


    @Resource
    private LoadBalancerClientFactory loadBalancerClientFactory;

    private final DataChannelManager dataChannelManager = new DataChannelManager();


    public Mono<DataMessage> execute(String serviceId, DataMessage dataMessage) {
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
                            .map(connection -> new RConnection(EndPointSinkPoll.alloc(), connection))
                            .doOnSuccess(connection -> {
                                log.info("connect success!!!");
                            })
                            .flatMap(connection -> {
                                if (connection.isDisposed()) {
                                    log.error("connect error");
                                    return Mono.empty();
                                }
                                connection.openInbound();
                                connection.handleRequest(dataMessage);
                                Sinks.One<DataMessage> sink = EndPointSinkPoll.get(connection.getSinkId());
                                return sink.asMono();
                            })
                            .log()
                            .doOnNext(dataMessage1 -> {
                                log.info("请求：{}",JSON.toJSONString(dataMessage1));
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
    }

}
