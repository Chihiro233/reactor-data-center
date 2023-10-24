package pers.nanahci.reactor.datacenter.domain.template;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;
import reactor.core.publisher.Mono;

@Slf4j
@Data
public class TemplateModel {


    private final ReactorWebClient reactorWebClient;

    private TemplateDO templateDO;


    public TemplateModel(TemplateDO templateDO) {
        this.reactorWebClient = SpringContextUtil.getBean(ReactorWebClient.class);
        this.templateDO = templateDO;
    }


    /**
     * 请求对象url，返回对应的值容器
     * @param body
     * @param respType
     * @return
     * @param <T>
     */
    public <T> Mono<T> post(String body, Class<T> respType) {
        String serverName = templateDO.getServerName();
        String uri = templateDO.getUri();
        return reactorWebClient.post(serverName, uri, body, respType);

    }

}
