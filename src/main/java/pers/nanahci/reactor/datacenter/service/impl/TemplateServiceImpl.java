package pers.nanahci.reactor.datacenter.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.xxl.job.core.context.XxlJobContext;
import groovy.json.StringEscapeUtils;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.CollectionUtils;
import pers.nanahci.reactor.datacenter.config.BatchTaskConfig;
import pers.nanahci.reactor.datacenter.controller.param.FileUploadAttach;
import pers.nanahci.reactor.datacenter.core.common.ContentTypes;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.reactor.ExecutorConstant;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.core.reactor.SubscribeErrorHolder;
import pers.nanahci.reactor.datacenter.core.task.TaskOperationEnum;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskInstanceDO;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateRepository;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateTaskInstanceRepository;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateTaskRepository;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.enums.ExecuteTypeEnum;
import pers.nanahci.reactor.datacenter.enums.PlatformTypeEnum;
import pers.nanahci.reactor.datacenter.enums.TaskStatusEnum;
import pers.nanahci.reactor.datacenter.intergration.webhook.AbstractWebHookHandler;
import pers.nanahci.reactor.datacenter.intergration.webhook.WebHookFactory;
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;
import pers.nanahci.reactor.datacenter.service.UploadSetting;
import pers.nanahci.reactor.datacenter.util.ExcelFileUtils;
import pers.nanahci.reactor.datacenter.util.FileUtils;
import pers.nanahci.reactor.datacenter.util.PathUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.SequenceInputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final TemplateTaskRepository templateTaskRepository;

    private final TemplateTaskInstanceRepository templateTaskInstanceRepository;

    private final FileService fileService;

    private final ReactorWebClient reactorWebClient;

    private final RedissonReactiveClient redissonReactiveClient;

    private final WebHookFactory webHookFactory;

    private final BatchTaskConfig batchTaskConfig;

    private final TransactionalOperator transactionalOperator;

    private final ScriptEngine scriptEngine = new GroovyScriptEngineImpl();


    @Override
    public Flux<TemplateModel> getUnComplete() {
        int shardTotal = XxlJobContext.getXxlJobContext().getShardTotal();
        int shardIndex = XxlJobContext.getXxlJobContext().getShardIndex();
        return r2dbcEntityTemplate.select(TemplateTaskDO.class)
                .matching(query(where("status")
                        .in(TaskStatusEnum.UN_START.getValue(), TaskStatusEnum.UN_START_RETRY.getValue()))
                        .limit(100))
                .all()
                .filter(taskDO -> shardIndex == (taskDO.getId() % shardTotal))
                .groupBy(TemplateTaskDO::getBatchNo)
                .flatMap(group -> {
                    String batchNo = group.key();
                    return r2dbcEntityTemplate.select(TemplateDO.class)
                            .matching(query(where("batchNo").is(batchNo)))
                            .one()
                            .publishOn(Schedulers.boundedElastic())
                            .map(template ->
                                    TemplateModel.builder().templateDO(template)
                                            .taskList(group.collectList().block())
                                            .build());

                });
    }


    /**
     * 考虑groovy脚本
     *
     * @param model
     */
    @SneakyThrows
    private Mono<?> afterTaskComplete(TemplateModel model, TemplateTaskDO taskDO) {
        TemplateDO templateDO = model.getTemplateDO();
        TemplateDO.Config config = JSON.parseObject(templateDO.getConfig(), TemplateDO.Config.class);
        PlatformTypeEnum type = PlatformTypeEnum.of(config.getType());
        AbstractWebHookHandler webHookHandler = webHookFactory.get(type);
        return Mono.defer(() -> {
            // 执行定义的groovy 脚本
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("config", config);
            bindings.put("task", taskDO);
            String script = StringEscapeUtils.unescapeJava(config.getScript());
            CommonWebHookDTO dto = null;
            try {
                dto = (CommonWebHookDTO) scriptEngine.eval(script, bindings);
            } catch (ScriptException e) {
                log.error("脚本执行异常,task ID is [{}]", taskDO.getId(), e);
            }
            return webHookHandler.execute(Objects.requireNonNull(dto, "脚本转换对象为空"));

        });

    }

    @Override
    public void execute(TemplateModel templateModel) {
        Flux.fromIterable(templateModel.getTaskList())
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR))
                .subscribe(task -> {
                    // try lock
                    if (templateModel.whetherRetry() && task.getRetryNum() >= templateModel.getMaxRetry()) {
                        return;
                    }
                    String key = "templateTask:batchId:" + task.getId();
                    RLockReactive lock = redissonReactiveClient.getLock(key);
                    long mockTid = ThreadLocalRandom.current().nextLong();
                    lock.tryLock(2, 300, TimeUnit.SECONDS, mockTid)
                            .filterWhen(result -> {
                                log.info("filterWhen线程:[{}]", Thread.currentThread());
                                if (!result) {
                                    log.warn("锁已经被占用,锁key:[{}]", key);
                                } else {
                                    log.debug("加锁成功,锁key:[{}]", key);
                                }
                                return Mono.just(result);
                            })
                            .then(preProcess(task))
                            .then(executeTask(task, templateModel) // test TODO
                                    .onErrorResume(e -> failTask(task, e)
                                            .then(Mono.error(e))))
                            .flatMap(errRows -> postProcess(errRows, task, templateModel))
                            .doFinally((signalType -> {
                                lock.unlock(mockTid).subscribe();
                            }))
                            .subscribe();
                });
    }

    public static void main(String[] args) {
        Flux<Integer> flux = Flux.just(1, 2, 0, 4, 5)
                .flatMap(i -> Mono.just(10 / i))
                .onErrorContinue((error, value) -> {
                    System.err.println("Encountered error: " + error.getMessage());
                    System.err.println("Failed value: " + value);
                });

        flux.subscribe(System.out::println);
    }

    @Override
    public Mono<String> commit(FilePart file, FileUploadAttach attach) {
        String batchNo = attach.getBatchNo();
        TemplateDO templateDO = new TemplateDO();
        templateDO.setBatchNo(batchNo);
        Example<TemplateDO> example = Example.of(templateDO, ExampleMatcher
                .matching().withMatcher("batchNo", ExampleMatcher.GenericPropertyMatcher::contains));
        // 1. 校验模板是否存在
        // 2. 上传获取url
        // 3. 保存任务
        return templateRepository.findOne(example)
                .switchIfEmpty(Mono.error(new RuntimeException("模板不存在")))
                .flatMap(template -> {
                    try {
                        Flux<DataBuffer> content = file.content();
                        return content.map(DataBuffer::asInputStream).reduce(SequenceInputStream::new)
                                .flatMap(ins -> Mono.fromSupplier(() -> {
                                    UploadSetting setting = new UploadSetting()
                                            .setPath(PathUtils.concat(batchTaskConfig.getPath(), file.filename()))
                                            .setFileType(ContentTypes.EXCEL)
                                            .setBucket(batchTaskConfig.getBucket());
                                    return fileService.upload(ins, setting, FileStoreType.S3);

                                }));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("上传发生异常", e));
                    }
                }).flatMap(url -> {
                    TemplateTaskDO task = new TemplateTaskDO();
                    task.setStatus(TaskStatusEnum.UN_START.getValue())
                            .setTitle(attach.getTitle())
                            .setBizInfo(attach.getBizInfo())
                            .setFileUrl(url)
                            .setBatchNo(batchNo);
                    return templateTaskRepository
                            .save(task).thenReturn(url);
                });
    }


    public Mono<Long> updateTaskStatus(TemplateTaskDO task, TaskStatusEnum taskStatus) {
        return r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                        Update.update("status", taskStatus.getValue()), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                });
    }


    @Override
    public Mono<Long> saveErrFileUrl(Long taskId, String errFileUrl) {
        return r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(taskId)),
                        Update.update("errFileUrl", errFileUrl), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                });
    }

    @Override
    public Flux<TemplateModel> getTimeoutTask() {
        int shardTotal = XxlJobContext.getXxlJobContext().getShardTotal();
        int shardIndex = XxlJobContext.getXxlJobContext().getShardIndex();
        return templateTaskRepository.selectTimeoutTask()
                .filter(taskDO -> shardIndex == (taskDO.getId() % shardTotal))
                .groupBy(TemplateTaskDO::getBatchNo)
                .flatMap(group -> {
                    String batchNo = group.key();
                    return r2dbcEntityTemplate.select(TemplateDO.class)
                            .matching(query(where("batchNo").is(batchNo)))
                            .one()
                            .publishOn(Schedulers.boundedElastic())
                            .map(template ->
                                    TemplateModel.builder().templateDO(template)
                                            .taskList(group.collectList().block())
                                            .build());

                });
    }

    @Override
    public void resolveTimeoutTask(TemplateModel model) {
        // 1. reset task status and record the number of retry
        // 2. reset the task
        Flux.fromIterable(model.getTaskList())
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR))
                .subscribe(task -> {
                    // try lock
                    String key = "templateTask:batchId:" + task.getId();
                    RLockReactive lock = redissonReactiveClient.getLock(key);
                    long mockTid = ThreadLocalRandom.current().nextLong();
                    lock.tryLock(2, 300, TimeUnit.SECONDS, mockTid)
                            .filterWhen(result -> {
                                log.info("filterWhen线程:[{}]", Thread.currentThread());
                                if (!result) {
                                    log.warn("锁已经被占用,锁key:[{}]", key);
                                } else {
                                    log.debug("加锁成功,锁key:[{}]", key);
                                }
                                return Mono.just(result);
                            })
                            .then(Mono.defer(() -> {
                                if (model.whetherRetry()) {
                                    if (task.getRetryNum() >= model.getMaxRetry()) {
                                        return terminalTask(task);
                                    }
                                    return resetTask(task);
                                } else {
                                    return terminalTask(task);
                                }
                            }))
                            .doFinally((signalType -> {
                                lock.unlock(mockTid).subscribe();
                            }))
                            .subscribe();
                });
    }

    private Mono<Long> resetTask(TemplateTaskDO task) {
        return r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                        Update.update("status", TaskStatusEnum.UN_START.getValue())
                                .set("retry_num", task.getRetryNum() + 1), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                });
    }

    private Mono<?> failTask(TemplateTaskDO task, Throwable e) {
        return transactionalOperator.transactional(r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                        Update.update("status", TaskStatusEnum.FAIL.getValue())
                                .set("end_time", LocalDateTime.now()), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                }).then(operateTaskInstance(0, e, task, TaskOperationEnum.FAIL)));

    }

    private Mono<?> startTask(TemplateTaskDO task) {
        return transactionalOperator.transactional(r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                        Update.update("status", TaskStatusEnum.WORKING.getValue())
                                .set("last_begin_time", LocalDateTime.now()), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                }).then(operateTaskInstance(0, null, task, TaskOperationEnum.INSERT)));
    }

    private Mono<?> completeTask(TemplateTaskDO task, Integer errRows) {
        return transactionalOperator.transactional(r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                        Update.update("status", TaskStatusEnum.COMPLETE.getValue())
                                .set("end_time", LocalDateTime.now()), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                }).then(operateTaskInstance(errRows, null, task, TaskOperationEnum.COMPLETE)));
    }

    private Mono<Long> terminalTask(TemplateTaskDO task) {
        // 补一下实例的处理
        return transactionalOperator.transactional(r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                        Update.update("status", TaskStatusEnum.TERMINAL.getValue()).set("endTime", LocalDateTime.now()), TemplateTaskDO.class)
                .filterWhen(uc -> {
                    // update count
                    if (uc == 0) {
                        return Mono.error(new RuntimeException("更新失败"));
                    }
                    return Mono.just(true);
                }));
        // warning follow
    }

    private Mono<Integer> executeTask(TemplateTaskDO task, TemplateModel templateModel) {
        TemplateDO templateDO = templateModel.getTemplateDO();
        Flux<Map<String, Object>> excelFile = ExcelFileUtils.getExcelFile(task.getFileUrl(), FileStoreType.LOCAL)
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
                                    .publishOn(Schedulers.fromExecutor(ExecutorConstant.SINGLE_ERROR_SINK_EXECUTOR))
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
        return Mono.when(rpcFlux, errFlux).thenReturn(ati.get());
    }


    private String getErrorFileNameFromUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return "未命名" + System.currentTimeMillis() + ".xlsx";
        }
        String fileName = FileUtils.getFileNameNoExtension(url);
        return fileName + System.currentTimeMillis() + ".xlsx";
    }

    private Mono<?> preProcess(TemplateTaskDO task) {
        return startTask(task);
    }

    private Mono<?> operateTaskInstance(Integer errRows, Throwable e, TemplateTaskDO task, TaskOperationEnum operation) {
        TemplateTaskInstanceDO log = new TemplateTaskInstanceDO();
        log.setTaskId(task.getId());
        // 查出最新的一条, 没有则插入
        switch (operation) {
            case INSERT -> {
                log.setBeginTime(LocalDateTime.now());
                return r2dbcEntityTemplate.insert(log);
            }
            case COMPLETE -> {
                return templateTaskInstanceRepository.lastOne(task.getId())
                        .switchIfEmpty(Mono.error(new RuntimeException("task instance isn't exist")))
                        .flatMap(taskInstance -> templateTaskInstanceRepository.complete(taskInstance.getId(), LocalDateTime.now(), errRows));
            }
            case FAIL -> {
                return templateTaskInstanceRepository.lastOne(task.getId())
                        .switchIfEmpty(Mono.error(new RuntimeException("task instance isn't exist")))
                        .flatMap(taskInstance ->
                                templateTaskInstanceRepository.fail(taskInstance.getId(), LocalDateTime.now(), StringUtils.substring(e.toString(), 0, 500)));
            }
        }
        return Mono.error(new RuntimeException("task operation is err"));
    }

    private Mono<?> postProcess(Integer errRows, TemplateTaskDO task, TemplateModel templateModel) {
        return completeTask(task, errRows)
                .then(afterTaskComplete(templateModel, task));
    }

    private boolean isBatch(Integer executeType) {
        return Objects.equals(ExecuteTypeEnum.Batch.getValue(), executeType);
    }

}
