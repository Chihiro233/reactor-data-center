package pers.nanahci.reactor.datacenter.service;

import pers.nanahci.reactor.datacenter.core.file.FileStoreType;

import java.io.InputStream;

public interface FileService {



    String upload(InputStream ins, String path, FileStoreType fileStoreType);

    String uploadLocalFile(String path,FileStoreType fileStoreType);


}
