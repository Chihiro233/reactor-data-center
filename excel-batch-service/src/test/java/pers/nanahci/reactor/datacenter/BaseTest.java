package pers.nanahci.reactor.datacenter;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;
import pers.nanahci.reactor.datacenter.core.netty.RpcClient;
import pers.nanahci.reactor.datacenter.job.TemplateTaskJobHandler;
import pers.nanahci.reactor.datacenter.service.task.TemplateService;
import reactor.core.publisher.Sinks;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MainApplication.class})
@Slf4j
public class BaseTest {


    @Resource
    private TemplateService templateService;

    @Resource
    private TemplateTaskJobHandler templateTaskJobHandler;


    @Resource
    private RpcClient rpcClient;


    @Test
    @SneakyThrows
    public void testXxlJob() {

        templateTaskJobHandler.taskExecutorJob();
        Thread.sleep(1000000000L);
    }

    @SneakyThrows
    public static void main(String[] args) {

        Sinks.Many<Integer> sinks = Sinks.many().multicast().onBackpressureBuffer(16,false);
        // Sinks.Many<Integer> sinks = Sinks.many().multicast().directAllOrNothing();
        sinks.tryEmitNext(1);
        sinks.tryEmitNext(2);
        sinks.tryEmitNext(3);
        sinks.tryEmitNext(4);
        sinks.asFlux().subscribe(x->{
            log.info("sub:"+x);
        });
        sinks.asFlux().subscribe(x->{
            log.info("sub_2:"+x);
        });
        sinks.tryEmitNext(5);
        Thread.sleep(1000000L);
    }


}
