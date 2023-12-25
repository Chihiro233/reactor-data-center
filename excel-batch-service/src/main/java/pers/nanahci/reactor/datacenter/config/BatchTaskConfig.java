package pers.nanahci.reactor.datacenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("task.oss")
@Data
public class BatchTaskConfig {

    private String bucket;

    private String path;

    private String errPath;

    private String tempPath;

}
