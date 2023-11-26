package pers.nanahci.reactor.datacenter.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.nanahci.reactor.datacenter.job.XxlJobProperties;

@Configuration
public class XxlJobConfig {

    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor(XxlJobProperties xxlJobProperties) {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(xxlJobProperties.getAppname());
       //xxlJobSpringExecutor.setIp(ip);
       //xxlJobSpringExecutor.setPort(port);
       xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
       //xxlJobSpringExecutor.setLogPath(logPath);
       //xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }

}
