package pers.nanahci.reactor.datacenter;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pers.nanahci.reactor.datacenter.job.XxlJobHandler;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MainApplication.class})
public class BaseTest {


    @Resource
    private TemplateService templateService;

    @Resource
    private XxlJobHandler xxlJobHandler;

    @Resource
    private FileService fileService;


    @Test
    @SneakyThrows
    public void testXxlJob() {
        xxlJobHandler.taskExecutorJob();
        Thread.sleep(1000000000L);
    }

}
