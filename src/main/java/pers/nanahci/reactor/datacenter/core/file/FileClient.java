package pers.nanahci.reactor.datacenter.core.file;

import java.io.InputStream;

public interface FileClient {


    InputStream getInputStream(String url);


    String uploadLocalFile(String localPath, String path, String type);

    String upload(InputStream ins, AccessSetting setting);


    FileStoreType type();

}
