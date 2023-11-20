package pers.nanahci.reactor.datacenter.core.file;

import com.qcloud.cos.model.PutObjectRequest;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.channels.FileChannel;

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

    @Override
    public String uploadLocalFile(String path) {

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
    public FileStoreType type() {
        return FileStoreType.LOCAL;
    }
}
