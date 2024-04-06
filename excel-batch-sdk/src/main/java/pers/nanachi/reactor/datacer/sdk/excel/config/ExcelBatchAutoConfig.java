package pers.nanachi.reactor.datacer.sdk.excel.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelBaseHandler;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExcelHandlerFactory;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.NettyEndpointService;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.ExcelExportTaskTypeProcessor;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.ExcelImportTaskTypeProcessor;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskDispatcher;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskTypeProcessor;

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
    public ExcelImportTaskTypeProcessor excelImportTaskProcessor(){
        return new ExcelImportTaskTypeProcessor();
    }

    @Bean
    public ExcelExportTaskTypeProcessor excelExportTaskProcessor(){
        return new ExcelExportTaskTypeProcessor();
    }


    @Bean
    public TaskDispatcher taskDispatcher(List<TaskTypeProcessor> taskTypeProcessors) {
        return new TaskDispatcher(taskTypeProcessors);
    }

    @Bean
    public NettyEndpointService excelExportService(TaskDispatcher taskDispatcher) {
        return new NettyEndpointService(taskDispatcher);
    }



}
