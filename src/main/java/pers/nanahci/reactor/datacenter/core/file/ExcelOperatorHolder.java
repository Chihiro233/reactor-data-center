package pers.nanahci.reactor.datacenter.core.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelOperatorHolder {


    private final String path;

    private ExcelWriterBuilder excelWriterBuilder;

    private ExcelWriter excelWriter;

    private WriteSheet writeSheet;


    private ExcelOperatorHolder(String path) {
        this.path = path;
    }

    public static ExcelOperatorHolder build(String path) {
        return new ExcelOperatorHolder(path);
    }

    public void write(List<Pair<Map<String, Object>, Throwable>> errData,
                      FileStoreType type) {
        if (Objects.isNull(excelWriterBuilder)) {
            init(buildHead(errData.get(0).getKey()));
        }
        excelWriter.write(() -> errData.stream().map(pair -> {
            Map<String, Object> rowData = pair.getKey();
            Throwable err = pair.getValue();
            Collection<Object> rowValue = rowData.values();
            Collection<Object> values = new ArrayList<>(rowValue);
            values.add(err.getMessage());
            return values;
        }).collect(Collectors.toList()), writeSheet);
    }

    public void finish() {
        excelWriter.finish();
    }

    private void init(List<List<String>> head) {
        excelWriterBuilder = EasyExcel.write(new File(path + "err.xlsx"))
                .head(head);
        writeSheet = EasyExcel.writerSheet("失败结果").build();
        excelWriter = excelWriterBuilder.build();
    }

    private List<List<String>> buildHead(Map<String, Object> rowData) {
        List<String> head = new ArrayList<>();
        for (Map.Entry<String, Object> entry : rowData.entrySet()) {
            String headName = entry.getKey();
            head.add(headName);
        }
        return buildHead(head);
    }

    private List<List<String>> buildHead(List<String> head) {
        return head.stream().map(headValue -> {
            List<String> columnValue = new ArrayList<>();
            columnValue.add(headValue);
            return columnValue;
        }).collect(Collectors.toList());
    }

}
