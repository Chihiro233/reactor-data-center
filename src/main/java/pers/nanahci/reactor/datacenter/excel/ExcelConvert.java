package pers.nanahci.reactor.datacenter.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelConvert {


    public static List<Map<String, Object>> excelToRowMap(InputStream inputStream) {
        List<Map<String, Object>> excelPropertyIndexModelList = new ArrayList<>();
        //监听器
        AnalysisEventListener<Map<Integer, Object>> listener = new AnalysisEventListener<Map<Integer, Object>>() {
            private Map<Integer, Object> head = new LinkedHashMap<>();
            private int row = 1;

            //读取每一行的数据
            @Override
            public void invoke(Map<Integer, Object> excelPropertyIndexModel, AnalysisContext analysisContext) {
                if (row == 1) {
                    // 第一行的数据是表头，作为key保存起来
                    head = excelPropertyIndexModel;
                    row++;
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                excelPropertyIndexModel.forEach((k, v) -> {
                    if (head.get(k) == null) {
                        return;
                    }
                    data.put(head.get(k).toString(), v);
                });
                excelPropertyIndexModelList.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                //读取之后的操作
            }
        };
        ExcelReader excelReader = EasyExcel.read(inputStream, listener).build();
        ReadSheet sheet0 = EasyExcel.readSheet(0).headRowNumber(0).build();
        excelReader.read(sheet0);
        excelReader.finish();
        return excelPropertyIndexModelList;
    }

}
