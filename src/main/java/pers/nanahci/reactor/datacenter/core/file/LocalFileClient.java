package pers.nanahci.reactor.datacenter.core.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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


    public void upload(InputStream ins, String url) {
        try {
            File file = new File(url);
            FileUtils.copyInputStreamToFile(ins, file);
        } catch (Exception e) {
            log.error("file upload failed", e);
        }
    }

    @Override
    public String upload(InputStream ins, AccessSetting setting) {
        upload(ins, setting.getPath());
        return setting.getPath();
    }

    @Override
    public String uploadLocalFile(String tempPath, String path, String type) {
        FileChannel readChannel = null;
        FileChannel writeChanel = null;
        try {
            readChannel = FileChannel.open(Paths.get(tempPath), StandardOpenOption.READ);
            long size = readChannel.size();
            long position = readChannel.position();

            writeChanel = FileChannel.open(Paths.get(path), StandardOpenOption.WRITE);
            readChannel.transferTo(position, size, writeChanel);

        } catch (Exception e) {
            log.error("0拷贝通道异常:", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (readChannel != null) {
                    readChannel.close();
                }

            } catch (Exception e) {
                log.error("关闭channel异常", e);
            }
            try {
                if (writeChanel != null) {
                    writeChanel.close();
                }
            } catch (Exception e) {
                log.error("关闭write channel异常", e);
            }
        }
        return path;
    }

    @Override
    public FileStoreType type() {
        return FileStoreType.LOCAL;
    }
}
