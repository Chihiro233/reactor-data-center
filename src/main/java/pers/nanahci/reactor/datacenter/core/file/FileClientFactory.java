package pers.nanahci.reactor.datacenter.core.file;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileClientFactory implements InitializingBean, ApplicationContextAware {

    private final static Map<FileStoreType, FileClient> mapping = new ConcurrentHashMap<>();

    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FileClientFactory.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, FileClient> beans = applicationContext.getBeansOfType(FileClient.class);
        beans.forEach((name, bean) -> {
            mapping.put(bean.type(), bean);
        });

    }

    public static FileClient get(FileStoreType type) {
        return mapping.get(type);
    }

}
