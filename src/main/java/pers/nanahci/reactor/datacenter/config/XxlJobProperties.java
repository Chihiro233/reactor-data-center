package pers.nanahci.reactor.datacenter.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "xxl.job")
@Component
@Data
public class XxlJobProperties {

    private String adminAddresses;

    private String appname;

    private String accessToken;
}
