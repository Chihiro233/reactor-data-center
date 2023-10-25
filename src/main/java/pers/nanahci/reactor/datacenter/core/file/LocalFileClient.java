package pers.nanahci.reactor.datacenter.core.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Component
@Slf4j
public class LocalFileClient extends AbstractFileClient {


    @Override
    public InputStream getInputStream(String url) {
        File file = getFile(url);
        try {
            return new FileInputStream(file);
        } catch (Exception e) {
            log.error("file read exception, url:[{}]", url, e);
            throw new RuntimeException("file read exception", e);
        }
    }

    @Override
    public byte[] get(String url) {
        File file = getFile(url);
        try {
            return new FileInputStream(file).readAllBytes();
        } catch (Exception e) {
            log.error("file read exception, url:[{}]", url, e);
            throw new RuntimeException("file read exception", e);
        }
    }

    @Override
    public FileStoreType type() {
        return FileStoreType.LOCAL;
    }
}
