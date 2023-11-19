package pers.nanahci.reactor.datacenter;

import jakarta.annotation.Resource;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.job.XxlJobHandler;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;

import java.util.*;

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
    public void testExcelUploadFile() {
        String errPath = "D:/code/proj/learn/reactor-data-center/src/main/resources/local/";

        List<Pair<Map<String, Object>, Throwable>> errData =
                new ArrayList<>();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("姓名", "俞鸿泰");
        map.put("年纪", 25);
        Pair<Map<String, Object>, Throwable> info = new Pair<>(map, new RuntimeException("模拟异常"));
        errData.add(info);
        //fileService.createExcelFile(errData, true,FileStoreType.LOCAL);
        //fileService.createExcelFile(errData,false,FileStoreType.LOCAL);
        //Thread.sleep(100000000000000L);
        ExcelOperatorHolder excelOperatorHolder = ExcelOperatorHolder.build(errPath);
        excelOperatorHolder.write(errData, null);
        excelOperatorHolder.write(errData, null);
        excelOperatorHolder.finish();

    }


    @Test
    @SneakyThrows
    public void testXxlJob() {
        xxlJobHandler.execute();
        Thread.sleep(1000000000L);
    }

}
