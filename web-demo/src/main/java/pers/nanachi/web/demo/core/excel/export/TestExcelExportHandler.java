package pers.nanachi.web.demo.core.excel.export;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.annotation.ExcelExport;
import pers.nanachi.reactor.datacer.sdk.excel.core.BaseExcelExportHandler;

import java.util.*;

@ExcelExport("testExcelTask")
@Component
@Slf4j
public class TestExcelExportHandler extends BaseExcelExportHandler<TestResp, TestReq> {

    private int index = 0;

    @Override
    public List<List<String>> getExcelHeaders(TestReq param) {
        List<String> names = new ArrayList<>();
        names.add("name");
        List<String> bazi = new ArrayList<>();
        bazi.add("bazi");


        //---------------------------------------------
        List<List<String>> res = new ArrayList<>();
        res.add(names);
        res.add(bazi);
        return res;
        //return null;
    }

    @Override
    public List<TestResp> getExportData(Integer pageNo, TestReq param) {

        List<@Nullable TestResp> testRespList = Lists.newArrayList();
        if (pageNo > 1000) {
            return testRespList;
        }
        for (int i = 0; i < 100; i++) {
            TestResp testResp = new TestResp();
            testResp.setYuwen("yu-wen: " + (index++));
            testResp.setEnglish("english:" + (index));
            testResp.setMath("数学:"+(index));
            testRespList.add(testResp);
        }
        log.info("current page :[{}]", pageNo);
        return testRespList;
    }


    @Override
    public Map<String, Object> getExportFill(TestReq param) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> templateFill(TestReq param) {
        Map<String,String> resMap = new HashMap<>();
        resMap.put("name","俞鸿泰");
        resMap.put("birthDay","2024-04-04");
        resMap.put("sign","兄弟你好香");
        return resMap;
    }


}
