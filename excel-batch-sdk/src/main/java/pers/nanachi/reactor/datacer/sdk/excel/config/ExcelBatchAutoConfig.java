package pers.nanachi.reactor.datacer.sdk.excel.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelBaseHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.NettyEndpointService;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.ExcelExportTaskProcessor;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.ExcelImportTaskProcessor;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskDispatcher;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskProcessor;

import java.util.List;

@AutoConfiguration
public class ExcelBatchAutoConfig {

    @Bean
    //@ConditionalOnBean(value = ExcelBaseHandler.class)
    //@ConditionalOnProperty(name = "rexcel.batch.role", havingValue = "provider")
    public ExcelHandlerFactory excelExportHandlerFactory(@Nullable List<ExcelBaseHandler> excelExportHandlers) {
        return new ExcelHandlerFactory(excelExportHandlers);
    }

    @Bean
    public ExcelImportTaskProcessor excelImportTaskProcessor(){
        return new ExcelImportTaskProcessor();
    }

    @Bean
    public ExcelExportTaskProcessor excelExportTaskProcessor(){
        return new ExcelExportTaskProcessor();
    }


    @Bean
    public TaskDispatcher taskDispatcher(List<TaskProcessor> taskProcessors) {
        return new TaskDispatcher(taskProcessors);
    }

    @Bean
    public NettyEndpointService excelExportService(TaskDispatcher taskDispatcher) {
        return new NettyEndpointService(taskDispatcher);
    }



}
