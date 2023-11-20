package pers.nanahci.reactor.datacenter.core.file;

import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface FileClient {


    InputStream getInputStream(String url);

    void upload(byte[] data, long position, String url);

    String uploadLocalFile(String tempPath, String path, String type);

    void upload(InputStream ins, String url);


    FileStoreType type();

}
