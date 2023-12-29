package pers.nanachi.reactor.datacer.sdk.excel.core;

import java.util.List;
import java.util.Map;

public interface ExcelExportHandler<R, T> extends ExcelBaseHandler {


    List<List<String>> getExcelHeaders(T param);

    List<R> getExportData(Integer pageNo, T param);

    Map<String, String> templateFill(T param);


}
