package pers.nanachi.web.demo.core.excel.export;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.annotation.ExcelExport;
import pers.nanachi.reactor.datacer.sdk.excel.core.BaseExcelExportHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExcelExport("testExcelTask")
@Component
@Slf4j
public class TestExcelExportHandler extends BaseExcelExportHandler<TestResp, TestReq> {

    private int index = 0;

    @Override
    public List<List<String>> getExcelHeaders(TestReq param) {
        List<String> names = new ArrayList<>();
        names.add("姓名");
        List<String> bazi = new ArrayList<>();
        bazi.add("八字");


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
        for (int i = 0; i < 1000; i++) {
            TestResp testResp = new TestResp();
            testResp.setName("nanachi233: " + (index++));
            testResp.setBazi("yingyang" + (index));
            testRespList.add(testResp);
        }
        log.info("当前页:[{}]", pageNo);
        return testRespList;
    }

    @Override
    public Map<String, String> templateFill(TestReq param) {
        return null;
    }


}
