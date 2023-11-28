package pers.nanahci.reactor.datacenter.service.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorExecutorConstant;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.core.reactor.SubscribeErrorHolder;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.service.task.constant.ExecuteTypeEnum;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@AllArgsConstructor
@Slf4j
public class ImportTaskExecutor extends AbstractExecutor{


    private final ReactorWebClient reactorWebClient;


    @Override
    public Mono<Integer> execute(TemplateTaskDO task, TemplateModel templateModel){

        TemplateDO templateDO = templateModel.getTemplateDO();

        Flux<Map<String, Object>> excelFile = ExcelFileUtils.getExcelFile(task.getFileUrl(), FileStoreType.S3)
                .doOnNext(rowData -> {
                    log.info("当前数据:[{}]", JSON.toJSONString(rowData));
                });

        Flux<String> rpcFlux;
        Sinks.Many<Pair<Map<String, Object>, Throwable>> errSink = Sinks.many().multicast().onBackpressureBuffer();
        Flux<Pair<Map<String, Object>, Throwable>> errFlux = errSink.asFlux();

        SubscribeErrorHolder errorHolder = SubscribeErrorHolder.build();
        errorHolder.subscribeError(errFlux, getErrorFileNameFromUrl(task.getFileUrl()), task.getId());
        // 如果是批量的则拆分`
        final AtomicInteger ati = new AtomicInteger();
        if (isBatch(templateDO.getExecuteType())) {
            rpcFlux = excelFile.buffer(templateDO.getBatchSize())
                    .flatMap(rowDataList ->
                            reactorWebClient.post(templateDO.getServerName(), templateDO.getUri(),
                                            JSONArray.toJSONString(rowDataList), String.class)
                                    .publishOn(Schedulers.fromExecutor(ReactorExecutorConstant.SINGLE_ERROR_SINK_EXECUTOR))
                                    .onErrorResume((err) -> {
                                        if (CollectionUtils.isEmpty(rowDataList)) {
                                            return Mono.empty();
                                        }
                                        for (Map<String, Object> rowData : rowDataList) {
                                            log.info("emitNext:[{}]", rowData);
                                            errSink.emitNext(new Pair<>(rowData, err), Sinks.EmitFailureHandler.FAIL_FAST);
                                            ati.incrementAndGet();
                                        }
                                        return Mono.empty();
                                    }))
                    .doFinally((s) -> {
                        log.info("emitComplete");
                        errSink.tryEmitComplete();
                    });

        } else {
            rpcFlux = excelFile.flatMap(rowData ->
                            reactorWebClient.post(templateDO.getServerName(), templateDO.getUri(),
                                    JSON.toJSONString(rowData), String.class))
                    .onErrorContinue((err, rowData) -> {
                        errSink.emitNext(new Pair<>((Map<String, Object>) rowData, err), Sinks.EmitFailureHandler.FAIL_FAST);
                        ati.incrementAndGet();
                    }).doFinally((s) -> {
                        errSink.tryEmitComplete();
                    });
        }
        return Mono.when(rpcFlux, errFlux).then(
                Mono.fromSupplier(ati::get)
        );
    }

    private boolean isBatch(Integer executeType) {
        return Objects.equals(ExecuteTypeEnum.Batch.getValue(), executeType);
    }

}
