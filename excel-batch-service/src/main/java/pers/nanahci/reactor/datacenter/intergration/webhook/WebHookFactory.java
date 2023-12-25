package pers.nanahci.reactor.datacenter.intergration.webhook;

import pers.nanahci.reactor.datacenter.intergration.webhook.enums.PlatformTypeEnum;

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
