package pers.nanahci.reactor.datacenter.intergration.webhook;

import pers.nanahci.reactor.datacenter.enums.PlatformTypeEnum;
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO;
import reactor.core.publisher.Mono;

public interface WebHookHandler {


    Mono<?> execute(CommonWebHookDTO param);

    Object parseParam(CommonWebHookDTO param);


    PlatformTypeEnum type();

}
