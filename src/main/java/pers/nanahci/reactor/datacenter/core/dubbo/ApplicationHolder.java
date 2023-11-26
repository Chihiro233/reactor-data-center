package pers.nanahci.reactor.datacenter.core.dubbo;

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.Map;

public class ApplicationHolder {

    // serviceName -> service
    private Map<String, GenericService> genericServiceMap;

    public ApplicationHolder(){

    }

    private void init(){
        // ReferenceConfig
    }

    public void add(){

    }


}
