package pers.nanahci.reactor.datacenter.core.netty;

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
import pers.nanahci.reactor.datacenter.util.ThrowableUtil;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class RpcClient {


    @Resource
    private LoadBalancerClientFactory loadBalancerClientFactory;

    private final DataChannelManager dataChannelManager = new DataChannelManager();


    public Mono<RpcResponse<?>> execute(RpcRequest<?> request) {
        ReactiveLoadBalancer<ServiceInstance> instance =
                loadBalancerClientFactory.getInstance(request.getAttach().getServiceId());
        // 将消息转换成
        return Mono.from(instance.choose())
                .flatMap(server -> {
                    ServiceInstance serverInstance = server.getServer();
                    return TcpClient.create()
                            .host(serverInstance.getHost())
                            .port(9896)
                            .runOn(LoopResources.create("rexcel-req-client"))
                            .doOnChannelInit((connectionObserver, channel, remoteAddress) -> initPipeline(channel.pipeline()))
                            .doOnConnected(this::initConnection)
                            .wiretap(true)
                            .connect()
                            .log()
                            .retryWhen(Retry.backoff(request.getAttach().getRetryNum(), Duration.ofMillis(request.getAttach().getTimeout()))
                                    .filter(ThrowableUtil::isDisconnectedClientError))
                            .doOnSuccess(connection -> {
                                log.info("connect success!!!");
                            })
                            .doOnError(err -> {
                                log.info("connect error:", err);
                            })
                            .map(connection -> new RConnection(EndPointSinkPoll.alloc(), connection))
                            .flatMap(connection -> {
                                if (connection.isDisposed()) {
                                    log.error("connect error");
                                    return Mono.empty();
                                }
                                connection.openInbound();
                                connection.handleRequest(request);
                                Sinks.One<RpcResponse<?>> sink = EndPointSinkPoll.get(connection.getSinkId());
                                return sink.asMono();
                            });
                });
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
