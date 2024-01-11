package pers.nanahci.reactor.datacenter.service.task;

import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcRequest;
import pers.nanachi.reactor.datacer.sdk.excel.param.ExcelTaskRequest;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.netty.RpcClient;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorExecutorConstant;
import pers.nanahci.reactor.datacenter.core.reactor.SubscribeErrorHolder;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.service.task.constant.ExecuteTypeEnum;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@AllArgsConstructor
@Slf4j
public class ImportTaskExecutor extends AbstractExecutor {


    private final RpcClient rpcClient;


    @Override
    public Mono<Integer> execute(TemplateTaskDO task, TemplateModel templateModel) {

        TemplateDO templateDO = templateModel.getTemplateDO();

        Flux<Map<String, Object>> excelFile = ExcelFileUtils.getExcelFile(task.getFileUrl(), FileStoreType.S3)
                .doOnNext(rowData -> {
                    log.info("当前数据:[{}]", JSON.toJSONString(rowData));
                });

        Flux<Void> rpcFlux;
        Sinks.Many<Pair<Map<String, Object>, Throwable>> errSink = Sinks.many().multicast().onBackpressureBuffer();
        Flux<Pair<Map<String, Object>, Throwable>> errFlux = errSink.asFlux();

        SubscribeErrorHolder errorHolder = SubscribeErrorHolder.build();
        errorHolder.subscribeError(errFlux, getErrorFileNameFromUrl(task.getFileUrl()), task.getId());
        // 如果是批量的则拆分`
        final AtomicInteger ati = new AtomicInteger();
        RpcRequest<ExcelTaskRequest> request = RpcRequest.get(templateDO.getServerName(), TaskTypeRecord.IMPORT_TASK);
        request.getData()
                .setTaskName(templateDO.getName())
                .setBizInfo(task.getBizInfo());
        if (isBatch(templateDO.getExecuteType())) {
            rpcFlux = excelFile.buffer(templateDO.getBatchSize())
                    .flatMap(rowDataList -> {
                        request.getData().setBizInfo(JSON.toJSONString(rowDataList));
                        return rpcClient.execute(request)
                                .doOnNext(response -> {
                                    if (!response.isSuccess()) {
                                        throw new RuntimeException("import task fail, case:" + response.getMsg());
                                    }
                                })
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
                                }).then();
                    });

        } else {
            rpcFlux = excelFile.flatMap(rowData -> {
                request.getData().setBizInfo(JSON.toJSONString(rowData));
                return rpcClient.execute(request)
                        .handle((response, sink) -> {
                            if (!response.isSuccess()) {
                                sink.error(new RuntimeException("request import error: " + response.getMsg()));
                            }
                        })
                        .onErrorContinue((err, rowData0) -> {
                            errSink.emitNext(new Pair<>(rowData, err), Sinks.EmitFailureHandler.FAIL_FAST);
                            ati.incrementAndGet();
                        })
                        .then();
            });
        }
        rpcFlux = rpcFlux.doFinally(signalType -> {
            if(Objects.equals(signalType, SignalType.ON_ERROR)||Objects.equals(signalType,SignalType.CANCEL)){
                errSink.tryEmitError(new RuntimeException("rpc flux error or canceled"));
            }else{
                errSink.tryEmitComplete();
            }
        });
        return Mono.when(rpcFlux, errFlux).then(
                Mono.fromSupplier(ati::get)
        );
    }

    private boolean isBatch(Integer executeType) {
        return Objects.equals(ExecuteTypeEnum.Batch.getValue(), executeType);
    }

}
