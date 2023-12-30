package pers.nanahci.reactor.datacenter.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataMessage;
import pers.nanahci.reactor.datacenter.config.BatchTaskConfig;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.netty.RpcClient;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j
public class ExportTaskExecutor extends AbstractExecutor {


    private final ReactorWebClient reactorWebClient;

    private final RpcClient rpcClient;


    private final BatchTaskConfig batchTaskConfig;

    private final Map<String, ExcelOperatorHolder> excelOperatorHolderMap = new ConcurrentHashMap<>();

    public Mono<Integer> execute(TemplateTaskDO task, TemplateModel templateModel) {

        TemplateDO template = templateModel.getTemplateDO();
        String bizInfo = task.getBizInfo();
        String serverName = template.getServerName();

        DataMessage.Attach attach = new DataMessage.Attach();
        // 1. init request data
        attach.setTaskType(TaskTypeRecord.EXPORT_TASK);
        attach.setTaskName(template.getName());
        attach.setPageNo(1);
        DataMessage dataMessage = DataMessage.buildReqData(bizInfo, attach);

        // 2. execute
        return requestHead(serverName, dataMessage)
                .flatMap(headData -> Mono.fromRunnable(() -> {
                    ExcelOperatorHolder operatorHolder = excelOperatorHolderMap.computeIfAbsent(task.getBatchNo(),
                            batchNo -> ExcelFileUtils.createOperatorHolder(batchTaskConfig.getTempPath(),
                                    task.getBatchNo() + "-" + System.currentTimeMillis() + ".xlsx", batchTaskConfig.getPath(), batchTaskConfig.getBucket()));
                    operatorHolder.init(headData);
                }))
                .then(requestData(template, task, dataMessage));

    }


    private Mono<List<List<String>>> requestHead(String serverName, DataMessage dataMessage) {
        return Mono.fromRunnable(() -> dataMessage.getAttach().setStage(ExportExecuteStage._getHead))
                .then(rpcClient.execute(serverName, dataMessage))
                .handle((data, sink) -> {
                    if(!dataMessage.whetherSuccess()) {
                        sink.error(new RuntimeException("request head fail"));
                        return;
                    }
                    TypeReference<List<List<String>>> ltr = new TypeReference<>() {
                    };
                    sink.next(JSON.parseObject(new String(data.getData()), ltr));
                });
    }

    private Mono<Integer> requestData(TemplateDO template, TemplateTaskDO task, DataMessage dataMessage) {

        String serverName = template.getServerName();
        return Mono.fromRunnable(() -> dataMessage.getAttach().setStage(ExportExecuteStage._getData))
                .then(
                        dynamicCall(task, serverName, dataMessage)
                                .expand(list -> {
                                    if (CollectionUtils.isEmpty(list) || list.size() < template.getBatchSize()) {
                                        return Mono.empty();
                                    } else {
                                        // next page
                                        return dynamicCall(task, serverName, dataMessage.nextPage());
                                    }
                                }).doFinally(signalType -> {
                                    if (excelOperatorHolderMap.containsKey(task.getBatchNo())) {
                                        ExcelOperatorHolder excelOperatorHolder = excelOperatorHolderMap.get(task.getBatchNo());
                                        excelOperatorHolder.finish();
                                        excelOperatorHolder.upload(FileStoreType.S3);
                                    }
                                }).then(Mono.fromSupplier(() -> {
                                    log.info("access this position");
                                    return 0;
                                }))
                );

    }

    private Mono<List<?>> dynamicCall(TemplateTaskDO task, String serverName, DataMessage dataMessage) {
        // 假如参数
        return rpcClient.execute(serverName, dataMessage)
                .log()
                .handle((data, sink) -> {
                    if(!data.whetherSuccess()){
                        sink.error(new RuntimeException("request data fail"));
                        return;
                    }
                    List<JSONObject> realData = Collections.emptyList();
                    try {
                        realData = JSON.parseArray(data.getData()).toList(JSONObject.class);

                        ExcelOperatorHolder operatorHolder = excelOperatorHolderMap.computeIfAbsent(task.getBatchNo(), batchNo -> ExcelFileUtils.createOperatorHolder(batchTaskConfig.getTempPath(),
                                task.getBatchNo() + "-" + System.currentTimeMillis() + ".xlsx", batchTaskConfig.getPath(), batchTaskConfig.getBucket()));

                        operatorHolder.writeExportData(realData);
                    } catch (Exception e) {
                        log.error("resolve export data error,batchNo is [{}]", task.getBatchNo(), e);
                        sink.error(new RuntimeException("export data resolution error"));
                    } finally {
                        sink.next(realData);
                    }
                });
    }


}
