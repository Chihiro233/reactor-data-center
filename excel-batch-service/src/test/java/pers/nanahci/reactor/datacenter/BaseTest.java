package pers.nanahci.reactor.datacenter;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pers.nanachi.reactor.datacenter.common.task.constant.TaskTypeRecord;
import pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataMessage;
import pers.nanahci.reactor.datacenter.core.netty.ReactorNettyEndpointClient;
import pers.nanahci.reactor.datacenter.job.TemplateTaskJobHandler;
import pers.nanahci.reactor.datacenter.service.task.TemplateService;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MainApplication.class})
public class BaseTest {


    @Resource
    private TemplateService templateService;

    @Resource
    private TemplateTaskJobHandler templateTaskJobHandler;


    @Resource
    private ReactorNettyEndpointClient reactorNettyEndpointClient;


    @Test
    @SneakyThrows
    public void testXxlJob() {
        templateTaskJobHandler.taskExecutorJob();
        Thread.sleep(1000000000L);
    }

    @Test
    @SneakyThrows
    public void testReactorClient() {
        String data = "{\"code\":123,\"msg\":\"dwadawd123\"}";
        DataMessage.Attach attach = new DataMessage.Attach();
        attach.setTaskName("testExcelTask");
        attach.setTaskType(TaskTypeRecord.EXPORT_TASK);
        attach.setStage(ExportExecuteStage._getHead);
        //StringUtils.
        DataMessage dataMessage = DataMessage.buildReqData(data, attach);
        reactorNettyEndpointClient.execute("web-demo", dataMessage).subscribe();
        Thread.sleep(1000000L);
    }

}
