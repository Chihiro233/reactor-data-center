package pers.nanahci.reactor.datacenter.core.reactor;


import com.alibaba.excel.util.StringUtils;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ReactorWebClient {

    private final WebClient webClient;

    private final WebClient directWebClient;

    public ReactorWebClient(ReactorLoadBalancerExchangeFilterFunction lbFun) {
        this.webClient = WebClient.builder().filter(lbFun).build();
        this.directWebClient = WebClient.builder().build();
    }

    public <T> Mono<T> get(String serverName, String uri, String body, Class<T> respType) {
        WebClient client = getClient(serverName);

        return client.get()
                .uri(buildUrl(serverName, uri))
                //.headers(httpHeaders -> WebFrameworkUtils.setTenantIdHeader(tenantId, httpHeaders)) // 设置租户的 Header
                .retrieve()
                .bodyToMono(respType)
                .subscribeOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR));
    }

    public <T> Mono<T> post(String serverName, String uri, String body, Class<T> respType) {
        WebClient client = getClient(serverName);

        return client.post()
                .uri(buildUrl(serverName, uri))
                //.headers(httpHeaders -> WebFrameworkUtils.setTenantIdHeader(tenantId, httpHeaders)) // 设置租户的 Header
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(respType);
    }

    private WebClient getClient(String serverName) {
        return StringUtils.isBlank(serverName) ? directWebClient : webClient;
    }


    private String buildUrl(String serverName, String uri) {
        if (uri.startsWith("http") || StringUtils.isBlank(serverName)) {
            return uri;
        }
        return "http://" + serverName + uri;
    }

}
