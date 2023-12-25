package pers.nanahci.reactor.datacenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.nanahci.reactor.datacenter.intergration.webhook.AbstractWebHookHandler;
import pers.nanahci.reactor.datacenter.intergration.webhook.WebHookFactory;

import java.util.List;

@Configuration
public class PlatformSdkConfig {

    @Bean
    public WebHookFactory webHookFactory(List<AbstractWebHookHandler> webHookHandlers){
        return new WebHookFactory(webHookHandlers);
    }


}
