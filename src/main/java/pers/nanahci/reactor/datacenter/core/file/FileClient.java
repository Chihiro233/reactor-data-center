package pers.nanahci.reactor.datacenter.core.file;

import java.io.InputStream;

public interface FileClient {


    InputStream getInputStream(String url);

    byte[] get(String url);

    FileStoreType type();

}
