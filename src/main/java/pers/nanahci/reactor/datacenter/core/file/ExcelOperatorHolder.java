package pers.nanahci.reactor.datacenter.core.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import javafx.util.Pair;
import pers.nanahci.reactor.datacenter.core.common.ContentTypes;
import pers.nanahci.reactor.datacenter.core.common.EasyURL;
import pers.nanahci.reactor.datacenter.util.PathUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelOperatorHolder {


    private final String tempPath;

    private final String path;

    private final String bucket;

    private final String fileName;

    private ExcelWriterBuilder excelWriterBuilder;

    private ExcelWriter excelWriter;

    private WriteSheet writeSheet;


    public ExcelOperatorHolder(String tempPath, String fileName,
                               String bucketPath, String bucket) {
        this.tempPath = tempPath;
        this.fileName = fileName;
        this.path = bucketPath;
        this.bucket = bucket;
    }


    public void write(List<Pair<Map<String, Object>, Throwable>> errData) {
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

    public String upload(FileStoreType type) {
        if (Objects.equals(type, FileStoreType.LOCAL)) {
            return "";
        }
        FileClient fileClient = FileClientFactory.get(type);
        return fileClient.uploadLocalFile(PathUtils.concat(tempPath, fileName),
                PathUtils.concat(path,fileName), ContentTypes.EXCEL);
    }

    public void finish() {
        excelWriter.finish();
    }

    private void init(List<List<String>> head) {
        excelWriterBuilder = EasyExcel.write(new File(tempPath + fileName))
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
