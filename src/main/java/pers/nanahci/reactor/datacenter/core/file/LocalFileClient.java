package pers.nanahci.reactor.datacenter.core.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
    public void upload(byte[] data, long position, String url) {
        try {
            File file = new File(url);
            FileUtils.writeByteArrayToFile(file, data, true);
        } catch (Exception e) {
            log.error("file upload failed", e);
        }
    }

    

    public void upload(InputStream ins, String url) {
        try {
            File file = new File(url);
            FileUtils.copyInputStreamToFile(ins, file);
        } catch (Exception e) {
            log.error("file upload failed", e);
        }
    }

    @Override
    public String uploadLocalFile(String tempPath, String path, String type) {
        return null;
    }

    @Override
    public FileStoreType type() {
        return FileStoreType.LOCAL;
    }
}
