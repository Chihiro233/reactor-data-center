package pers.nanahci.reactor.datacenter.util;

import org.apache.commons.lang3.StringUtils;

public class FileUtils {


    public static String getFileName(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        return StringUtils.substringAfterLast(url, "/");
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
