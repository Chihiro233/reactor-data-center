package pers.nanachi.web.demo.core.excel.imports;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanachi.reactor.datacer.sdk.excel.annotation.ExcelImport;
import pers.nanachi.reactor.datacer.sdk.excel.core.BaseExcelImportHandler;


@ExcelImport("testExcelImportTask")
@Component
@Slf4j
public class TestExcelImportHandler extends BaseExcelImportHandler<TestImportReq> {

    @Override
    public void importExecute(TestImportReq param) {
        log.info("receive msg:{}", param);
    }
}
