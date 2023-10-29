package pers.nanahci.reactor.datacenter.config;

import com.lark.oapi.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.nanahci.reactor.datacenter.intergration.webhook.AbstractWebHookHandler;
import pers.nanahci.reactor.datacenter.intergration.webhook.WebHookFactory;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;

import java.util.List;

@Configuration
public class PlatformSdkConfig {

    @Bean
    public WebHookFactory webHookFactory(List<AbstractWebHookHandler> webHookHandlers){
        return new WebHookFactory(webHookHandlers);
    }


}
