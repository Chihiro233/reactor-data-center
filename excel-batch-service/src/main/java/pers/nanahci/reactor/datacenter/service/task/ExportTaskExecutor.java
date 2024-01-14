package pers.nanahci.reactor.datacenter.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j
public class ExportTaskExecutor extends AbstractExecutor {


    private final RpcClient rpcClient;


    private final BatchTaskConfig batchTaskConfig;

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
                .map(headData -> {
                    // create ExcelOperatorHolder obj
                    ExcelOperatorHolder operatorHolder = ExcelFileUtils.createOperatorHolder(batchTaskConfig.getTempPath(),
                            buildFileName(task.getBatchNo() + "-" + task.getId()), batchTaskConfig.getPath(), batchTaskConfig.getBucket());
                    operatorHolder.init(headData);
                    return operatorHolder;
                })
                .flatMap(operator -> requestData(operator, task, request));

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


    private Mono<Integer> requestData(ExcelOperatorHolder excelOperatorHolder, TemplateTaskDO task, RpcRequest<ExcelTaskRequest> request) {

        return Mono.fromRunnable(() -> request.getData().setStage(ExportExecuteStage._getData))
                .then(
                        dynamicCall(excelOperatorHolder, task, request)
                                .expand(list -> {
                                    if (CollectionUtils.isEmpty(list)) {
                                        return Mono.empty();
                                    } else {
                                        // next page
                                        request.getData().nextPage();
                                        return dynamicCall(excelOperatorHolder, task, request);
                                    }
                                }).doFinally(signalType -> {
                                    log.info("task publisher signal:[{}]",signalType);
                                    if (signalType.compareTo(SignalType.ON_ERROR) == 0 || signalType.compareTo(SignalType.CANCEL) == 0) {
                                        excelOperatorHolder.finish().clear();
                                    }else{
                                        excelOperatorHolder.finish().upload(FileStoreType.S3, true);
                                    }
                                }).then(Mono.fromSupplier(() -> {
                                    log.info("access this position");
                                    return 0;
                                }))
                );

    }

    private Mono<List<?>> dynamicCall(ExcelOperatorHolder excelOperatorHolder, TemplateTaskDO task, RpcRequest<?> request) {
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
                        excelOperatorHolder.writeExportData(realData);
                        sink.next(realData);
                    } catch (Throwable e) {
                        log.error("request export data error", e);
                        sink.error(e);
                    }
                });
    }


    private String buildFileName(String batchNO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String timestamp = formatter.format(LocalDateTime.now());
        return batchNO + "-" + timestamp + ".xlsx";
    }


}
