package pers.nanahci.reactor.datacenter.intergration.webhook;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.enums.PlatformTypeEnum;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebHookFactory {

    private  final Map<PlatformTypeEnum,AbstractWebHookHandler> webHookHandlerMap
            = new HashMap<>();

    public WebHookFactory(List<AbstractWebHookHandler> handlers){
        handlers.forEach(handler->{
            this.webHookHandlerMap.put(handler.type(), handler);
        });
    }



    public  AbstractWebHookHandler get(PlatformTypeEnum type){
        return webHookHandlerMap.get(type);
    }

}
