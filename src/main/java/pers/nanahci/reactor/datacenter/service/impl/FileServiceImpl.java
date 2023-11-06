package pers.nanahci.reactor.datacenter.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ConverterUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.file.FileClient;
import pers.nanahci.reactor.datacenter.core.file.FileClientFactory;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.service.FileService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private static final String errPath = "D:/code/proj/learn/reactor-data-center/src/main/resources/local/";


    @Override
    public Flux<Map<String, Object>> getExcelFile(String fileUrl, FileStoreType type) {

        // 从oss上拉取文件
        return Flux.create(sink -> {
            FileClient fileClient = FileClientFactory.get(type);
            InputStream ins = fileClient.getInputStream(fileUrl);
            EasyExcel.read(ins, new AnalysisEventListener<Map<Integer, Object>>() {
                //
                private Map<Integer, String> head;

                //
                @Override
                public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                    head = ConverterUtils.convertToStringMap(headMap, context);
                }

                //
                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
//
                    Map<String, Object> convertData = new LinkedHashMap<>();
                    data.forEach((index, content) -> {
                        String key = head.get(index);
//
                        convertData.put(key, content);
                    });
                    log.info("解析行:{}", context.readRowHolder().getRowIndex());
                    sink.next(convertData);
                }

                //
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("file read complete");
                    sink.complete();
                }
            }).doReadAll();
        });

    }

    @Override
    public void createExcelFile(List<Pair<Map<String, Object>, Throwable>> errData, FileStoreType type) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        EasyExcel.write(bao)
                .head(buildHead(errData.get(0).getKey()))
                .sheet("失败结果")
                .doWrite(() -> errData.stream().map(pair -> {
                    Map<String, Object> rowData = pair.getKey();
                    Throwable err = pair.getValue();
                    Collection<Object> rowValue = rowData.values();
                    Collection<Object> values = new ArrayList<>(rowValue);
                    values.add(err.getMessage());
                    return values;
                }).collect(Collectors.toList()));
        FileClient fileClient = FileClientFactory.get(type);
        fileClient.upload(bao.toByteArray(), 0, errPath + "err.xlsx");
    }

    @Override
    public String upload(InputStream ins, String path, FileStoreType type) {
        FileClient fileClient = FileClientFactory.get(type);
        fileClient.upload(ins,path);
        return path;
    }

    private List<List<String>> buildHead(List<String> head) {
        return head.stream().map(headValue -> {
            List<String> columnValue = new ArrayList<>();
            columnValue.add(headValue);
            return columnValue;
        }).collect(Collectors.toList());
    }

    private List<List<String>> buildHead(Map<String, Object> rowData) {
        List<String> head = new ArrayList<>();
        for (Map.Entry<String, Object> entry : rowData.entrySet()) {
            String headName = entry.getKey();
            head.add(headName);
        }
        return buildHead(head);
    }

}
