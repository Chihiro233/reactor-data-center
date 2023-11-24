package pers.nanahci.reactor.datacenter.core.file;

import pers.nanahci.reactor.datacenter.service.UploadSetting;

import java.io.InputStream;

public interface FileClient {


    InputStream getInputStream(String url);

    void upload(byte[] data, long position, String url);

    String uploadLocalFile(String localPath, String path, String type);

    String upload(InputStream ins, UploadSetting setting);


    FileStoreType type();

}
