package pers.nanahci.reactor.datacenter.service.impl;


import com.alibaba.fastjson2.JSON;
import com.xxl.job.core.context.XxlJobContext;
import groovy.json.StringEscapeUtils;
import groovy.lang.GroovyShell;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.controller.param.FileUploadAttach;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.reactor.ExecutorConstant;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateRepository;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateTaskRepository;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.enums.PlatformTypeEnum;
import pers.nanahci.reactor.datacenter.enums.TaskStatusEnum;
import pers.nanahci.reactor.datacenter.intergration.webhook.AbstractWebHookHandler;
import pers.nanahci.reactor.datacenter.intergration.webhook.WebHookFactory;
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final TemplateTaskRepository templateTaskRepository;

    private final FileService fileService;

    private final ReactorWebClient reactorWebClient;

    private final WebHookFactory webHookFactory;
    private final ScriptEngine scriptEngine = new GroovyScriptEngineImpl();
    private static final GroovyShell groovyShell = new GroovyShell();

    @Override
    public void execute(Long id, String batchNo) {
        // 查询模板
        Mono<TemplateDO> temMono = templateRepository.findById(id);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("batchNo", matcher -> matcher.ignoreCase().contains());
        TemplateTaskDO templateTaskDO = new TemplateTaskDO().setBatchNo(batchNo);
        // 查询任务
        Example<TemplateTaskDO> example = Example.of(templateTaskDO, exampleMatcher);
        Mono<TemplateTaskDO> taskMono = templateTaskRepository.findOne(example);
        // 组合两个操作，最后打印出调用的数据
        final AtomicReference<TemplateModel> ref = new AtomicReference<>();
        final AtomicReference<List<Pair<Map<String, Object>, Throwable>>> errRef =
                new AtomicReference<>(new ArrayList<>());
        Mono.zip(temMono, taskMono)
                .flatMapMany(tuple -> {
                    TemplateDO templateDO = tuple.getT1();
                    TemplateTaskDO taskDO = tuple.getT2();
                    TemplateModel model = TemplateModel.builder()
                            .taskList(List.of(taskDO))
                            .templateDO(templateDO).build();
                    ref.set(model);

                    log.info("当前线程名称:{}", Thread.currentThread().getName());
                    Flux<Map<String, Object>> excelFile = fileService.getExcelFile(taskDO.getFileUrl(), FileStoreType.LOCAL);

                    return excelFile.map(rowData ->
                            reactorWebClient.post(templateDO.getServerName(), templateDO.getUri(),
                                    JSON.toJSONString(rowData), String.class));
                })
                .onErrorContinue((err, rowData) -> {
                    errRef.get().add(new Pair<>((Map<String, Object>) rowData, err));
                })
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR))
                .doFinally(signalType -> {
                    TemplateModel templateModel = ref.get();
                    // 可不可以配置groovy脚本呢
                    if (Objects.equals(signalType, SignalType.ON_COMPLETE)) {
                        afterTaskComplete(templateModel, templateTaskDO);
                    }
                    // 处理异常
                    List<Pair<Map<String, Object>, Throwable>> pairs = errRef.get();
                    if (pairs.isEmpty()) {
                        return;
                    }
                    // 生成失败的excel文件
                    String configStr = templateModel.getTemplateDO().getConfig();
                    TemplateDO.Config config = JSON.parseObject(configStr, TemplateDO.Config.class);
                    String headList = config.getHeadList();
                    List<String> head = JSON.parseArray(headList, String.class);

                }).subscribe(response -> {
                    log.info("收到的回复:[{}]", response);
                });

    }

    @Override
    public Flux<TemplateModel> getUnComplete() {
        int shardTotal = XxlJobContext.getXxlJobContext().getShardTotal();
        int shardIndex = XxlJobContext.getXxlJobContext().getShardIndex();
        return r2dbcEntityTemplate.select(TemplateTaskDO.class)
                .matching(query(where("status")
                        .in(TaskStatusEnum.UN_STARTER.getValue()))
                        .limit(100))
                .all()
                .filter(taskDO -> shardIndex == (taskDO.getId() % shardTotal))
                .groupBy(TemplateTaskDO::getBatchNo)
                .flatMap(group -> {
                    String batchNo = group.key();
                    return r2dbcEntityTemplate.select(TemplateDO.class)
                            .matching(query(where("batchNo").is(batchNo)))
                            .one()
                            .publishOn(Schedulers.boundedElastic()).map(template ->
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
        TemplateDO templateDO = templateModel.getTemplateDO();
        Flux.fromIterable(templateModel.getTaskList())
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR))
                .subscribe(task -> {
                    List<Pair<Map<String, Object>, Throwable>> errMap = new ArrayList<>();
                    Flux<Map<String, Object>> excelFile = fileService.getExcelFile(task.getFileUrl(), FileStoreType.LOCAL);

                    excelFile.doOnNext(rowData -> {
                                log.info("当前数据:[{}]", JSON.toJSONString(rowData));
                            }).
                            map(rowData ->
                                    reactorWebClient.post(templateDO.getServerName(), templateDO.getUri(),
                                            JSON.toJSONString(rowData), String.class))

                            .onErrorContinue((err, rowData) -> {
                                errMap.add(new Pair<>((Map<String, Object>) rowData, err));
                            })
                            .publishOn(Schedulers.boundedElastic())
                            .doFinally(signalType -> {
                                // 更新任务状态并执行
                                updateTaskStatus(task)
                                        .map(k -> {
                                            if (k == 0) {
                                                return Mono.error(new RuntimeException("更新失败"));
                                            }
                                            return k;
                                        })
                                        .then(afterTaskComplete(templateModel, task))
                                        .subscribe();

                                if (errMap.isEmpty()) {
                                    return;
                                }
                                // 生成失败的excel文件
                                ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR.execute(() -> {
                                    fileService.createExcelFile(errMap, FileStoreType.LOCAL);
                                });
                            }).subscribe(resp -> {
                                log.info("收到返回消息:{}", resp);
                            });
                });

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
                                .flatMap(ins -> Mono.just(fileService.upload(ins,
                                        "D:/code/proj/learn/reactor-data-center/src/main/resources/upload/" + file.filename(),
                                        FileStoreType.LOCAL)));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("上传发生异常", e));
                    }
                }).flatMap(url -> {
                    TemplateTaskDO task = new TemplateTaskDO();
                    task.setStatus(TaskStatusEnum.UN_STARTER.getValue())
                            .setTitle(attach.getTitle())
                            .setBizInfo(attach.getBizInfo())
                            .setFileUrl(url)
                            .setBatchNo(batchNo);
                    return templateTaskRepository
                            .save(task).thenReturn(url);
                });
    }


    private Mono<Long> updateTaskStatus(TemplateTaskDO task) {
        TemplateTaskDO updateCondition = new TemplateTaskDO().setStatus(TaskStatusEnum.COMPLETE.getValue())
                .setId(task.getId());
        return r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(task.getId())),
                Update.update("status", TaskStatusEnum.COMPLETE.getValue())
                , TemplateTaskDO.class);

        // return r2dbcEntityTemplate.update(updateCondition);

    }

}
