package pers.nanahci.reactor.datacenter.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;

@Configuration
@AutoConfigureAfter(ReactorLoadBalancerClientAutoConfiguration.class)
public class ReactorConfig {

    @Bean
    public ReactorWebClient reactorWebClient(ReactorLoadBalancerExchangeFilterFunction lbFun){
        return new ReactorWebClient(lbFun);
    }

}
