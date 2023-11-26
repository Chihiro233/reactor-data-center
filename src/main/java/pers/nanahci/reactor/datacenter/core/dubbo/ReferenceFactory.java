package pers.nanahci.reactor.datacenter.core.dubbo;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ReferenceFactory {

    //1. RegistryConfig. singleton pattern
    //2. ReferenceConfig<GenericService> 需要一个interface一个，ref的配置需要从数据库里读取


    // applicationName, application
    private Map<String, ApplicationHolder> applicationHolderMap = new ConcurrentHashMap<>();

    @Resource
    private RegistryConfig registryConfig;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        log.info("start initializing the reference metadata");

    }

    public void refreshApplication(String application){

    }


}
