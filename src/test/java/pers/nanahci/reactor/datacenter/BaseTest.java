package pers.nanahci.reactor.datacenter;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pers.nanahci.reactor.datacenter.service.TemplateService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MainApplication.class})
//@ContextConfiguration
public class BaseTest {


    @Resource
    private TemplateService templateService;

    @Test
    @SneakyThrows
    public void testReactor(){
        templateService.execute(1L,"b2310240001");
        Thread.sleep(3000L);
    }

}
