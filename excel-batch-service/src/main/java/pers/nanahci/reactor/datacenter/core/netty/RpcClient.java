package pers.nanahci.reactor.datacenter.core.netty;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcRequest;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcResponse;
import pers.nanahci.reactor.datacenter.util.ThrowableUtil;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class RpcClient {


    @Resource
    private LoadBalancerClientFactory loadBalancerClientFactory;


    public Mono<RpcResponse<?>> execute(RpcRequest<?> request) {
        ReactiveLoadBalancer<ServiceInstance> instance =
                loadBalancerClientFactory.getInstance(request.getAttach().getServiceId());
        // 将消息转换成
        return Mono.from(instance.choose())
                .flatMap(server -> {
                    ServiceInstance serverInstance = server.getServer();
                    if (serverInstance == null) {
                        return Mono.error(new RuntimeException("no serverInstance, serviceId: " + request.getAttach().getServiceId()));
                    }
                    return ConnectionManager.get(serverInstance.getHost())
                            .retryWhen(Retry.backoff(request.getAttach().getRetryNum(), Duration.ofMillis(request.getAttach().getTimeout()))
                                    .filter(ThrowableUtil::isDisconnectedClientError))
                            .doOnError(err -> {
                                log.info("connect error:", err);
                            })
                            .flatMap(connection -> {
                                if (connection.isDisposed()) {
                                    log.error("connect error");
                                    return Mono.empty();
                                }
                                long msgId = connection.handleRequest(request);
                                Sinks.One<RpcResponse<?>> sink = RequestSinkPoll.get(msgId);
                                return sink.asMono()
                                        .doOnNext(x -> connection.recycle());
                            });
                });
    }

}
