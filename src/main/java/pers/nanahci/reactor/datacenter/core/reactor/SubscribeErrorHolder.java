package pers.nanahci.reactor.datacenter.core.reactor;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import pers.nanahci.reactor.datacenter.config.BatchTaskConfig;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.service.task.TemplateService;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class SubscribeErrorHolder {


    private ExcelOperatorHolder excelOperatorHolder;


    public static SubscribeErrorHolder build() {
        return new SubscribeErrorHolder();
    }

    private SubscribeErrorHolder() {

    }

    public void subscribeError(Flux<Pair<Map<String, Object>, Throwable>> flux, String fileName, Long taskId) {
        flux.buffer(1000)
                .doOnNext(d -> {
                    log.info("收到错误消息");
                })
                .publishOn(Schedulers.fromExecutor(ReactorExecutorConstant.DEFAULT_ERROR_EXECUTOR))
                .flatMap(data -> Mono.defer(() -> {
                    if (excelOperatorHolder == null) {
                        BatchTaskConfig config = SpringContextUtil.getBean(BatchTaskConfig.class);
                        excelOperatorHolder = ExcelFileUtils.createOperatorHolder(config.getTempPath(), fileName, config.getErrPath(), config.getBucket());
                    }
                    excelOperatorHolder.writeError(data);
                    return Mono.empty();
                })).concatWith(Mono.defer(() -> {
                    if (Objects.nonNull(excelOperatorHolder)) {
                        excelOperatorHolder.finish();
                        String errUrl = excelOperatorHolder.upload(FileStoreType.S3);
                        log.info("errUrl:[{}]", errUrl);
                        TemplateService ts = SpringContextUtil.getBean(TemplateService.class);
                        return ts.saveErrFileUrl(taskId, errUrl);
                    }
                    return Mono.empty();
                })).doFinally(signalType -> {
                    excelOperatorHolder.finish();
                }).subscribe();
    }


}
