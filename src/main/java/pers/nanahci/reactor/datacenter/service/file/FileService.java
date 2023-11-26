package pers.nanahci.reactor.datacenter.service.file;

import org.springframework.http.codec.multipart.FilePart;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.file.S3Setting;

import java.io.InputStream;

public interface FileService {


    String upload(InputStream ins, S3Setting setting, FileStoreType fileStoreType);

    String uploadLocalFile(String path, FileStoreType fileStoreType);

    String optUpload(FilePart filePart, FileStoreType fileStoreType);


}
