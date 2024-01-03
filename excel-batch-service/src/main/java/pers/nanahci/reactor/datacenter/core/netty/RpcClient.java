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


    public Mono<RpcResponse<?>> execute(RpcRequest<?> request) {
        ReactiveLoadBalancer<ServiceInstance> instance =
                loadBalancerClientFactory.getInstance(request.getAttach().getServiceId());
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
