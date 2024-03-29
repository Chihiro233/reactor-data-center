package pers.nanahci.reactor.datacenter.intergration.webhook;

import lombok.extern.slf4j.Slf4j;
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractWebHookHandler implements WebHookHandler {


    @Override
    public Mono<?> execute(CommonWebHookDTO param) {
        log.info("platform type:[{}],origin msg:[{}]", this.type(), param);
        Object platformMsg = parseParam(param);
        log.info("request msg:{}", platformMsg);
        try {
            return handle(platformMsg, param.getIdentifyToken());
        } catch (Exception e) {
            log.error("request failed ", e);
            throw e;
        }
    }


    public abstract Mono<?> handle(Object platformMsg, IdentifyToken identifyToken);
}
