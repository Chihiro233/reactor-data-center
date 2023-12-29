package pers.nanachi.reactor.datacer.sdk.excel.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelBaseHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.NettyEndpointService;

import java.util.List;

@AutoConfiguration
public class ExcelBatchAutoConfig {

    @Bean
    @ConditionalOnBean(value = ExcelBaseHandler.class)
    public ExcelHandlerFactory excelExportHandlerFactory(List<ExcelBaseHandler> excelExportHandlers) {
        return new ExcelHandlerFactory(excelExportHandlers);
    }

    @Bean
    @ConditionalOnBean(value = ExcelHandlerFactory.class)
    public NettyEndpointService excelExportService(ExcelHandlerFactory excelHandlerFactory) {
        return new NettyEndpointService(excelHandlerFactory);
    }



}
