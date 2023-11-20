package pers.nanahci.reactor.datacenter.core.reactor;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.config.BatchTaskConfig;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import pers.nanahci.reactor.datacenter.util.SpringContextUtil;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class SubscribeErrorHolder {


    private final static String tempPath = "D:/code/proj/learn/reactor-data-center/src/main/resources/local/";


    private ExcelOperatorHolder excelOperatorHolder;


    public static SubscribeErrorHolder build() {
        return new SubscribeErrorHolder();
    }

    private SubscribeErrorHolder() {

    }

    public void subscribeError(Flux<Pair<Map<String, Object>, Throwable>> flux, String fileName) {
        flux.buffer(2)
                .doOnNext(d -> {
                    log.info("收到错误消息");
                })
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_ERROR_EXECUTOR))
                .doFinally(signalType -> {
                    log.info("consumer error success");
                    // need to judge signalType
                    if (Objects.nonNull(excelOperatorHolder)) {
                        excelOperatorHolder.finish();
                        excelOperatorHolder.upload(FileStoreType.S3);
                    }
                })
                .subscribe(data -> {
                    if (excelOperatorHolder == null) {
                        BatchTaskConfig config = SpringContextUtil.getBean(BatchTaskConfig.class);
                        excelOperatorHolder = ExcelFileUtils.createOperatorHolder(config.getTempPath(), fileName, config.getPath(), config.getBucket());
                    }
                    excelOperatorHolder.write(data);
                });
    }


}
