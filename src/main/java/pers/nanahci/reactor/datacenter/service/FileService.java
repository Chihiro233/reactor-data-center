package pers.nanahci.reactor.datacenter.service;

import reactor.core.publisher.Flux;

import java.util.Map;

public interface FileService {

    Flux<Map<String,Object>> getExcelFile(String fileUrl);

}
