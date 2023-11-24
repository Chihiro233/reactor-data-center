package pers.nanahci.reactor.datacenter.service;

import org.springframework.http.codec.multipart.FilePart;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;

import java.io.InputStream;

public interface FileService {


    String upload(InputStream ins, UploadSetting setting, FileStoreType fileStoreType);

    String uploadLocalFile(String path, FileStoreType fileStoreType);

    String optUpload(FilePart filePart, FileStoreType fileStoreType);


}
