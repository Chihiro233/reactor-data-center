package pers.nanahci.reactor.datacenter.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import pers.nanahci.reactor.datacenter.core.file.FileClient;
import pers.nanahci.reactor.datacenter.core.file.FileClientFactory;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public class FileUtils {

    public static InputStream getFileInputStream(String fileUrl, FileStoreType type){
            FileClient fileClient = FileClientFactory.get(type);
            return fileClient.getInputStream(fileUrl);
    }


    public static String getFileName(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        if (!StringUtils.startsWith(url, "http") && (url.contains("\\")) && SystemUtils.IS_OS_WINDOWS) {
            return StringUtils.substringAfterLast(url, "\\");
        } else {
            return StringUtils.substringAfterLast(url, "/");
        }
    }

    public static String getFileNameNoExtension(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        String fileName = getFileName(url);
        return StringUtils.substringBefore(fileName, ".");
    }

    public static String getExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        return StringUtils.substringAfter(fileName, ".");
    }

}
