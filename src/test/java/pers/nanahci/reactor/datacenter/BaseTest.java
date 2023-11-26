package pers.nanahci.reactor.datacenter;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pers.nanahci.reactor.datacenter.job.TemplateTaskJobHandler;
import pers.nanahci.reactor.datacenter.service.file.FileService;
import pers.nanahci.reactor.datacenter.service.task.TemplateService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MainApplication.class})
public class BaseTest {


    @Resource
    private TemplateService templateService;

    @Resource
    private TemplateTaskJobHandler templateTaskJobHandler;

    @Resource
    private FileService fileService;


    @Test
    @SneakyThrows
    public void testXxlJob() {
        templateTaskJobHandler.taskExecutorJob();
        Thread.sleep(1000000000L);
    }

}
