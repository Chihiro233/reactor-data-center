package pers.nanahci.reactor.datacenter.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.config.BatchTaskConfig;
import pers.nanahci.reactor.datacenter.controller.param.Ret;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
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
import java.util.concurrent.atomic.AtomicInteger;

@Component
@AllArgsConstructor
@Slf4j
public class ExportTaskExecutor extends AbstractExecutor {


    private final ReactorWebClient reactorWebClient;


    private final BatchTaskConfig batchTaskConfig;

    private final Map<String, ExcelOperatorHolder> excelOperatorHolderMap = new ConcurrentHashMap<>();

    public Mono<Integer> execute(TemplateTaskDO task, TemplateModel templateModel) {

        TemplateDO templateDO = templateModel.getTemplateDO();
        Integer batchSize = templateDO.getBatchSize();
        AtomicInteger pageNo = new AtomicInteger(1);

        return dynamicCall(templateDO.getBatchSize(), pageNo.get(), task, templateDO)
                .expand(list -> {
                    if (CollectionUtils.isEmpty(list) || list.size() < templateDO.getBatchSize()) {
                        return Mono.empty();
                    } else {
                        return dynamicCall(batchSize, pageNo.incrementAndGet(), task, templateDO);
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
                }));
    }

    private Mono<List<?>> dynamicCall(int pageSize, int pageNo, TemplateTaskDO task, TemplateDO template) {
        String bizInfo = task.getBizInfo();
        String serverName = template.getServerName();
        String uri = template.getUri();
        JSONObject.parseObject("");

        // 假如参数
        return reactorWebClient.post(serverName, uri, bizInfo, String.class)
                .handle((resp, sink) -> {
                    Ret<?> ret = JSON.parseObject(resp, Ret.class);
                    if (ret.whetherSuccess()) {
                        Object data = ret.getData();
                        // TODO process data
                        List<JSONObject> realData = Collections.emptyList();
                        if (data instanceof JSONArray jsonArray) {
                            if (jsonArray.isEmpty()) {
                                sink.next(realData);
                                return;
                            }
                            ExcelOperatorHolder operatorHolder = excelOperatorHolderMap.computeIfAbsent(task.getBatchNo(), batchNo -> ExcelFileUtils.createOperatorHolder(batchTaskConfig.getTempPath(),
                                    task.getBatchNo() + "-" + System.currentTimeMillis() + ".xlsx", batchTaskConfig.getPath(), batchTaskConfig.getBucket()));

                            realData = jsonArray.toList(JSONObject.class);
                            operatorHolder.writeExportData(realData);
                        } else {
                            sink.error(new RuntimeException("parse error, data isn't array"));
                        }
                        sink.next(realData);
                    } else {
                        sink.error(new RuntimeException("unexpect error"));
                    }
                });
    }


}
