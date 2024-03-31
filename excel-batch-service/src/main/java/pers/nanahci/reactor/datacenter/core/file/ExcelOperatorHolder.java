package pers.nanahci.reactor.datacenter.core.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import pers.nanahci.reactor.datacenter.core.common.ContentTypes;
import pers.nanahci.reactor.datacenter.util.FileUtils;
import pers.nanahci.reactor.datacenter.util.PathUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ExcelOperatorHolder {


    private final String tempPath;

    private final String path;

    private final String bucket;

    private final String fileName;

    private ExcelWriterBuilder excelWriterBuilder;

    private ExcelWriter excelWriter;

    private WriteSheet writeSheet;

    private List<List<String>> headList;

    private String templateUrl;

    private volatile boolean initialize;

    public ExcelOperatorHolder(String tempPath, String fileName,
                               String bucketPath, String bucket) {
        this.tempPath = tempPath;
        this.fileName = fileName;
        this.path = bucketPath;
        this.bucket = bucket;
    }

    public ExcelOperatorHolder(String tempPath, String fileName,
                               String bucketPath, String bucket,String templateUrl) {
        this.tempPath = tempPath;
        this.fileName = fileName;
        this.path = bucketPath;
        this.bucket = bucket;
        this.templateUrl = templateUrl;
    }

    public ExcelOperatorHolder writeFillData(Map<?,?> fillData ) {
        if (!initialize) {
            initTemplate();
        }
        excelWriter.fill(fillData,writeSheet);
        return this;
    }

    public ExcelOperatorHolder writeFillDataList(List<JSONObject> fillDataList) {
        if (!initialize) {
            initTemplate();
        }
        excelWriter.fill(fillDataList,writeSheet);
        return this;
    }


    public ExcelOperatorHolder writeError(List<Pair<Map<String, Object>, Throwable>> errData) {
        if (!initialize) {
            initHead(buildHeadByRowData(errData.get(0).getKey()));
        }
        excelWriter.write(() -> errData.stream().map(pair -> {
            Map<String, Object> rowData = pair.getKey();
            Throwable err = pair.getValue();
            Collection<Object> rowValue = rowData.values();
            Collection<Object> values = new ArrayList<>(rowValue);
            values.add(err.getMessage());
            return values;
        }).collect(Collectors.toList()), writeSheet);
        return this;
    }

    public ExcelOperatorHolder writeExportData(List<JSONObject> exportData) {
        if (CollectionUtils.isEmpty(exportData)) {
            return this;
        }
        if (!initialize) {
            JSONObject jsonObject = exportData.get(0);
            TypeReference<LinkedHashMap<String,Object>> ltr = new TypeReference<>(LinkedHashMap.class,String.class,Object.class) {
            };
            LinkedHashMap<String, Object> exportData0 = jsonObject.to(ltr);
            initHead(buildHeadByRowData(exportData0));
        }
        excelWriter.write(() -> exportData.stream().map(jsonObject -> {
            Collection<Object> values = new ArrayList<>();
            for (List<String> head : headList) {
                if(CollectionUtils.isEmpty(head)){
                    values.add(null);
                }else{
                    String headKey = head.get(head.size()-1);
                    Object v = jsonObject.get(headKey);
                    values.add(v);
                }

            }
            return values;
        }).collect(Collectors.toList()), writeSheet);
        return this;
    }

    public void clear() {
        File file = new File(tempPath + fileName);
        if (file.exists()) {
            if (!file.delete()) {
                log.error("temp file delete fail");
            }
        }
        log.info("temp file delete success");
    }

    public String upload(FileStoreType type, boolean clear) {
        if (Objects.equals(type, FileStoreType.LOCAL)) {
            return "";
        }
        FileClient fileClient = FileClientFactory.get(type);
        String url = fileClient.uploadLocalFile(PathUtils.concat(tempPath, fileName),
                PathUtils.concat(path, fileName), ContentTypes.EXCEL);
        if (clear && !Objects.equals(type, FileStoreType.LOCAL)) {
            clear();
        }
        return url;
    }

    public ExcelOperatorHolder finish() {
        excelWriter.finish();
        return this;
    }

    public synchronized ExcelOperatorHolder initHead(List<List<String>> head) {
        excelWriterBuilder = EasyExcel.write(new File(tempPath + fileName))
                .head(head);
        writeSheet = EasyExcel.writerSheet("失败结果").build();
        excelWriter = excelWriterBuilder.build();
        headList = head;
        initialize = true;
        return this;
    }

    public synchronized ExcelOperatorHolder initTemplate() {
        InputStream templateInputStream = FileUtils.getFileInputStream(templateUrl, FileStoreType.S3);
        excelWriterBuilder = EasyExcel.write(new File(tempPath + fileName))
                .withTemplate(templateInputStream);
        initialize = true;
        return this;
    }

    private List<List<String>> buildHeadByRowData(Map<String, Object> rowData) {
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
