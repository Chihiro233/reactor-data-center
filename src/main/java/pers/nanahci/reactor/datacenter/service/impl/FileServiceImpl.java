package pers.nanahci.reactor.datacenter.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ConverterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.file.FileClient;
import pers.nanahci.reactor.datacenter.core.file.FileClientFactory;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.service.FileService;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FileServiceImpl implements FileService {


    @Override
    public Flux<Map<String, Object>> getExcelFile(String fileUrl, FileStoreType type) {
        //return Flux.create(sink->{
        //    HashMap<String, Object> map = new HashMap<>();
        //    map.put("key","sdsd");
        //    sink.next(map);
        //    sink.complete();
        //});
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
                    Map<String, Object> convertData = new HashMap<>();
                    data.forEach((index, content) -> {
                        String key = head.get(index);
//
                        convertData.put(key, content);
                    });
                    log.info("解析行:{}",context.readRowHolder().getRowIndex());
                    sink.next(convertData);
                }
//
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("file read complete");
                    sink.complete();
                }
            }).doReadAll();
//
//
        });

    }

}
