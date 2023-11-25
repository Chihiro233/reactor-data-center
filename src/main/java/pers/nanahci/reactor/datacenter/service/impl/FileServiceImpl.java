package pers.nanahci.reactor.datacenter.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.file.FileClient;
import pers.nanahci.reactor.datacenter.core.file.FileClientFactory;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.S3Setting;

import java.io.InputStream;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Override
    public String upload(InputStream ins, S3Setting setting, FileStoreType type) {
        FileClient fileClient = FileClientFactory.get(type);
        return fileClient.upload(ins, setting);
    }

    @Override
    public String uploadLocalFile(String path, FileStoreType type) {

        return path;
    }

    @Override
    public String optUpload(FilePart filePart, FileStoreType fileStoreType) {
        return null;
    }
}
