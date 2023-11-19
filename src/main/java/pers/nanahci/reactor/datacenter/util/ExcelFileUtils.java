package pers.nanahci.reactor.datacenter.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import lombok.extern.slf4j.Slf4j;
import pers.nanahci.reactor.datacenter.core.file.FileClient;
import pers.nanahci.reactor.datacenter.core.file.FileClientFactory;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ExcelFileUtils {

    public static Flux<Map<String, Object>> getExcelFile(String fileUrl, FileStoreType type) {

        // 从oss上拉取文件
        return Flux.create(sink -> {
            FileClient fileClient = FileClientFactory.get(type);
            InputStream ins = fileClient.getInputStream(fileUrl);
            EasyExcel.read(ins, new AnalysisEventListener<Map<Integer, Object>>() {
                private Map<Integer, String> head;

                @Override
                public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                    head = ConverterUtils.convertToStringMap(headMap, context);
                }

                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    Map<String, Object> convertData = new LinkedHashMap<>();
                    data.forEach((index, content) -> {
                        String key = head.get(index);
                        convertData.put(key, content);
                    });
                    log.info("解析行:{}", context.readRowHolder().getRowIndex());
                    sink.next(convertData);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("file read complete");
                    sink.complete();
                }
            }).doReadAll();

        });



    }


}
