package pers.nanahci.reactor.datacenter.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcRequest;
import pers.nanachi.reactor.datacer.sdk.excel.param.ExcelTaskRequest;
import pers.nanahci.reactor.datacenter.config.BatchTaskConfig;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.netty.RpcClient;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j
public class ExportTaskExecutor extends AbstractExecutor {


    private final RpcClient rpcClient;


    private final BatchTaskConfig batchTaskConfig;

    private final Map<Long, ExcelOperatorHolder> excelOperatorHolderMap = new ConcurrentHashMap<>();

    public Mono<Integer> execute(TemplateTaskDO task, TemplateModel templateModel) {

        TemplateDO template = templateModel.getTemplateDO();
        String bizInfo = task.getBizInfo();
        String serverName = template.getServerName();
        ExcelTaskRequest excelTaskRequest = new ExcelTaskRequest();

        excelTaskRequest.setTaskName(template.getName())
                .setBizInfo(bizInfo)
                .setPageNo(1)
                .setBatchNo(task.getBatchNo());

        RpcRequest<ExcelTaskRequest> request = RpcRequest.get(serverName, TaskTypeRecord.EXPORT_TASK);
        request.getAttach().setTimeout(200);
        request.getAttach().setRetryNum(3);
        request.setData(excelTaskRequest);

        // 2. execute
        return requestHead(request)
                .flatMap(headData -> Mono.fromRunnable(() -> {
                    ExcelOperatorHolder operatorHolder = excelOperatorHolderMap.computeIfAbsent(task.getId(),
                            taskId -> ExcelFileUtils.createOperatorHolder(batchTaskConfig.getTempPath(),
                                    buildFileName(task.getBatchNo() + "-" + taskId), batchTaskConfig.getPath(), batchTaskConfig.getBucket()));
                    operatorHolder.init(headData);
                }))
                .then(requestData(template, task, request));

    }


    private Mono<List<List<String>>> requestHead(RpcRequest<ExcelTaskRequest> request) {
        return Mono.fromRunnable(() -> request.getData().setStage(ExportExecuteStage._getHead))
                .then(rpcClient.execute(request))
                .handle((data, sink) -> {
                    if (!data.isSuccess()) {
                        sink.error(new RuntimeException("request head fail"));
                        return;
                    }
                    TypeReference<List<List<String>>> ltr = new TypeReference<>() {
                    };
                    if (data.getData() instanceof JSONArray headArray) {
                        List<List<String>> res = JSON.parseObject(headArray.toString(), ltr);
                        sink.next(res);
                    } else {
                        sink.error(new RuntimeException("data format isn't array"));
                    }
                });
    }

    private Mono<Integer> requestData(TemplateDO template, TemplateTaskDO task, RpcRequest<ExcelTaskRequest> request) {

        return Mono.fromRunnable(() -> request.getData().setStage(ExportExecuteStage._getData))
                .then(
                        dynamicCall(task, request)
                                .expand(list -> {
                                    if (CollectionUtils.isEmpty(list) || list.size() < template.getBatchSize()) {
                                        return Mono.empty();
                                    } else {
                                        // next page
                                        request.getData().nextPage();
                                        return dynamicCall(task, request);
                                    }
                                }).doFinally(signalType -> {
                                    if (excelOperatorHolderMap.containsKey(task.getId())) {
                                        ExcelOperatorHolder excelOperatorHolder = excelOperatorHolderMap.get(task.getId());
                                        excelOperatorHolder.finish().upload(FileStoreType.S3, true);
                                        excelOperatorHolder.clear();
                                    }
                                }).then(Mono.fromSupplier(() -> {
                                    log.info("access this position");
                                    return 0;
                                }))
                );

    }

    private Mono<List<?>> dynamicCall(TemplateTaskDO task, RpcRequest<?> request) {
        // 假如参数
        return rpcClient.execute(request)
                .handle((response, sink) -> {
                    try {
                        if (!response.isSuccess()) {
                            throw new RuntimeException("request data fail" + response.getMsg());
                        }
                        List<JSONObject> realData;
                        Object data = response.getData();
                        if (data instanceof JSONArray dataArray) {
                            realData = dataArray.toList(JSONObject.class);
                        } else {
                            throw new RuntimeException("request data isn't array");
                        }
                        ExcelOperatorHolder operatorHolder = excelOperatorHolderMap.computeIfAbsent(task.getId(),
                                taskId -> ExcelFileUtils.createOperatorHolder(batchTaskConfig.getTempPath(),
                                        buildFileName(task.getBatchNo()) + "-" + taskId, batchTaskConfig.getPath(), batchTaskConfig.getBucket()));

                        operatorHolder.writeExportData(realData);

                        sink.next(realData);
                    } catch (Throwable e) {
                        log.error("request export data error", e);
                        sink.error(e);
                    }
                });
    }


    private String buildFileName(String batchNO) {
        return batchNO + "-" + System.currentTimeMillis() + ".xlsx";
    }


}
