package pers.nanahci.reactor.datacenter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.nanahci.reactor.datacenter.core.file.S3ClientConfig;
import pers.nanahci.reactor.datacenter.core.file.S3FileClient;

@Configuration
public class ObsConfig {

    @Bean
    @ConditionalOnProperty(prefix = "s3.config", name = "enable")
    public S3FileClient s3FileClient(S3ClientConfig config) {
        return new S3FileClient(config);
    }

}
