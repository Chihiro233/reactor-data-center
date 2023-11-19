package pers.nanahci.reactor.datacenter.service;

import javafx.util.Pair;
import pers.nanahci.reactor.datacenter.core.file.ExcelOperatorHolder;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileService {



    String upload(InputStream ins, String path, FileStoreType fileStoreType);


}
