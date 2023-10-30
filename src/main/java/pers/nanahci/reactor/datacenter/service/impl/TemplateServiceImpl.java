package pers.nanahci.reactor.datacenter.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import groovy.json.StringEscapeUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.reactor.ExecutorConstant;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateRepository;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateTaskRepository;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.enums.PlatformTypeEnum;
import pers.nanahci.reactor.datacenter.intergration.webhook.AbstractWebHookHandler;
import pers.nanahci.reactor.datacenter.intergration.webhook.WebHookFactory;
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;
import pers.nanahci.reactor.datacenter.util.GroovyUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
        Mono.zip(temMono, taskMono)
                //.subscribeOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR))
                .flatMapMany(tuple -> {
                    TemplateDO templateDO = tuple.getT1();
                    TemplateTaskDO taskDO = tuple.getT2();
                    TemplateModel model = TemplateModel.builder()
                            .taskList(List.of(taskDO))
                            .templateDO(templateDO).build();
                    ref.set(model);

                    log.info("当前线程名称:{}", Thread.currentThread().getName());
                    Flux<Map<String, Object>> excelFile = fileService.getExcelFile(taskDO.getFileUrl(), FileStoreType.LOCAL);

                    return excelFile.flatMap(rowData ->
                            reactorWebClient.post(templateDO.getServerName(), templateDO.getUri(),
                                    JSON.toJSONString(rowData), String.class));
                })
                .publishOn(Schedulers.fromExecutor(ExecutorConstant.DEFAULT_SUBSCRIBE_EXECUTOR))
                .doFinally(signalType -> {
                    // 可不可以配置groovy脚本呢
                    TemplateModel templateModel = ref.get();
                    afterTaskComplete(templateModel);
                })
                .subscribe(response -> {
                    log.info("收到的回复:[{}]", response);
                });
    }

    /**
     * 考虑groovy脚本
     *
     * @param model
     */
    @SneakyThrows
    private void afterTaskComplete(TemplateModel model) {
        TemplateDO templateDO = model.getTemplateDO();
        List<TemplateTaskDO> taskList = model.getTaskList();
        TemplateDO.Config config = JSON.parseObject(templateDO.getConfig(), TemplateDO.Config.class);
        PlatformTypeEnum type = PlatformTypeEnum.of(config.getType());
        AbstractWebHookHandler webHookHandler = webHookFactory.get(type);
        for (TemplateTaskDO taskDO : taskList) {
            // 执行定义的groovy 脚本
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("config", config);
            bindings.put("task", taskDO);
            String script = StringEscapeUtils.unescapeJava(config.getScript());
            CommonWebHookDTO dto = (CommonWebHookDTO) scriptEngine.eval(script, bindings);

            webHookHandler.execute(dto).block();
        }
    }
}
