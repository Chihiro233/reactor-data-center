package pers.nanahci.reactor.datacenter.core.dubbo;

import lombok.Builder;

@Builder
public class ApplicationAttributes {

    private String applicationName;

    private String referenceInterface;

    private boolean whetherAsync;


}
