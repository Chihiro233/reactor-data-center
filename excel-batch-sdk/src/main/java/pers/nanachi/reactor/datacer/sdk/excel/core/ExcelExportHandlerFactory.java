package pers.nanachi.reactor.datacer.sdk.excel.core;

import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacenter.common.util.AssertUtil;
import pers.nanachi.reactor.datacer.sdk.excel.annotation.ExcelExport;
import pers.nanachi.reactor.datacer.sdk.excel.annotation.ExcelImport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExcelExportHandlerFactory {


    private final Map<String, ExcelExportHandler<?, ?>> EXCEL_EXPORT_HANDLE_MAP = new ConcurrentHashMap<>();

    private final Map<String, ExcelImportHandler<?>> EXCEL_IMPORT_HANDLE_MAP = new ConcurrentHashMap<>();


    public ExcelExportHandlerFactory(List<ExcelBaseHandler> excelHandlers) {

        for (ExcelBaseHandler excelHandler : excelHandlers) {

            if (excelHandler instanceof ExcelExportHandler<?, ?> exportHandler) {

                ExcelExport annotation = excelHandler.getClass().getAnnotation(ExcelExport.class);
                AssertUtil.isTrue(() -> (annotation != null && annotation.value() != null), "ExcelExport can't be null");
                String exportTaskName = annotation.value();
                EXCEL_EXPORT_HANDLE_MAP.put(exportTaskName, exportHandler);

            } else if (excelHandler instanceof ExcelImportHandler<?> importHandler) {

                ExcelImport annotation = excelHandler.getClass().getAnnotation(ExcelImport.class);
                AssertUtil.isTrue(() -> (annotation != null && annotation.value() != null), "ExcelImport can't be null");
                String exportTaskName = annotation.value();
                EXCEL_IMPORT_HANDLE_MAP.put(exportTaskName, importHandler);

            }


        }
    }

    public ExcelExportHandler<?, ?> getExportHandler(String exportTaskName) {
        return EXCEL_EXPORT_HANDLE_MAP.get(exportTaskName);
    }

    public ExcelImportHandler<?> getImportHandler(String exportTaskName) {
        return EXCEL_IMPORT_HANDLE_MAP.get(exportTaskName);
    }


}
