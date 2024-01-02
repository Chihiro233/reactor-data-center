package pers.nanachi.reactor.datacer.sdk.excel.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelBaseHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.NettyEndpointService;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskDispatcher;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskProcessor;

import java.util.List;

@AutoConfiguration
public class ExcelBatchAutoConfig {

    @Bean
    @ConditionalOnBean(value = ExcelBaseHandler.class)
    @ConditionalOnProperty(name = "rexcel.batch.role", havingValue = "provider")
    public ExcelHandlerFactory excelExportHandlerFactory(List<ExcelBaseHandler> excelExportHandlers) {
        return new ExcelHandlerFactory(excelExportHandlers);
    }

    @Bean
    @ConditionalOnBean(value = ExcelHandlerFactory.class)
    public NettyEndpointService excelExportService(ExcelHandlerFactory excelHandlerFactory) {
        return new NettyEndpointService(excelHandlerFactory);
    }

    @Bean
    public TaskDispatcher taskDispatcher(List<TaskProcessor> taskProcessors){

    }


}
