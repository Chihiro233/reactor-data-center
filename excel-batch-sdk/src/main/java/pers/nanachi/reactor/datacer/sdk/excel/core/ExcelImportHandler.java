package pers.nanachi.reactor.datacer.sdk.excel.core;

public interface ExcelImportHandler<T> extends ExcelBaseHandler {

    void importExecute(T param);

}
