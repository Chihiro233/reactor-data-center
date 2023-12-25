package pers.nanachi.reactor.datacer.sdk.excel.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelBaseHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelExportHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataProcessClientHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.NettyEndpointService;

import java.util.List;

@AutoConfiguration
public class ExcelBatchAutoConfig {

    @Bean
    //@ConditionalOnBean(value = ExcelExportHandler.class)
    public ExcelExportHandlerFactory excelExportHandlerFactory(List<ExcelBaseHandler> excelExportHandlers) {
        return new ExcelExportHandlerFactory(excelExportHandlers);
    }

    @Bean
    public NettyEndpointService excelExportService(ExcelExportHandlerFactory excelExportHandlerFactory) {
        return new NettyEndpointService(excelExportHandlerFactory);
    }

    @Bean
    public DataProcessClientHandler dataProcessClientHandler(){
        return new DataProcessClientHandler();
    }
}
