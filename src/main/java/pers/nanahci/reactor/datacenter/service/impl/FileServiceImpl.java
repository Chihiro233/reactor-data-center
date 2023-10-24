package pers.nanahci.reactor.datacenter.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.service.FileService;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FileServiceImpl implements FileService {


    @Override
    public Flux<Map<String, Object>> getExcelFile(String fileUrl) {
        //return null;
        Map<String,Object> memOne = new HashMap<>();
        memOne.put("yuhongtai","加油");
        Map<String,Object> memTwo = new HashMap<>();
        memTwo.put("qiqi","加油");
        return Flux.create(sink->{
            sink.next(memOne);
            sink.next(memTwo);
        });
    }
}
