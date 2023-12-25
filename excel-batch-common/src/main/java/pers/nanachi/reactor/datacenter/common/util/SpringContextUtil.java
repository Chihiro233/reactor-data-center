package pers.nanachi.reactor.datacenter.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.context = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        return context.getBean(type);
    }

    public static Object getBeanOfName(String beanName) {
        return context.getBean(beanName);
    }

    public static <T> Map<String, T> getBeans(Class<T> type) {
        return context.getBeansOfType(type);
    }

}
