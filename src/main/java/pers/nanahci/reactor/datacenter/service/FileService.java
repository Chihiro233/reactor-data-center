package pers.nanahci.reactor.datacenter.service;

import javafx.util.Pair;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface FileService {

    Flux<Map<String, Object>> getExcelFile(String fileUrl, FileStoreType type);

    void createExcelFile(List<Pair<Map<String, Object>, Throwable>> errData, FileStoreType type);


    String upload(InputStream ins, String path, FileStoreType fileStoreType);


}
