package pers.nanahci.reactor.datacenter.service.impl;


import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.reactor.ExecutorConstant;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateTaskRepository;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;
import pers.nanahci.reactor.datacenter.dal.entity.repo.TemplateRepository;
import pers.nanahci.reactor.datacenter.domain.template.convert.TemplateConvert;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
                .subscribe(response -> {
                    // 可不可以配置groovy脚本呢
                    TemplateModel templateModel = ref.get();
                    afterTaskComplete(templateModel);

                });
    }

    private void afterTaskComplete(TemplateModel model) {
        TemplateDO templateDO = model.getTemplateDO();
        List<TemplateTaskDO> taskList = model.getTaskList();
        for (TemplateTaskDO taskDO : taskList) {

        }
    }
}
