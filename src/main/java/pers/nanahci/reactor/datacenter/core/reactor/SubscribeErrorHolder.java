package pers.nanahci.reactor.datacenter.core.reactor;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
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

    public void subscribeError(Flux<Pair<Map<String, Object>, Throwable>> flux) {
        flux.buffer(2)
                .doOnNext(d -> {
                    log.info("收到错误消息");
                })
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_ERROR_EXECUTOR))
                .doFinally(signalType -> {
                    log.info("consumer error success");
                    if (Objects.nonNull(excelOperatorHolder)) {
                        excelOperatorHolder.finish();
                    }
                })
                .subscribe(data -> {
                    if (excelOperatorHolder == null) {
                        excelOperatorHolder = ExcelOperatorHolder.build(tempPath);
                    }
                    excelOperatorHolder.write(data, FileStoreType.LOCAL);
                });
    }


}
