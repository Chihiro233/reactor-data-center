package pers.nanahci.reactor.datacenter.core.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("s3.config")
@Configuration
@Data
public class S3ClientConfig {


    private String endPoint;

    private String domain;

    private String bucket;

    private String accessKey;

    private String accessSecret;



}
