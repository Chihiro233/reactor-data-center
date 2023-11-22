package pers.nanahci.reactor.datacenter.util;

import pers.nanahci.reactor.datacenter.core.common.EasyURL;

public class PathUtils {

    public static String concat(String baseUrl, String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

}
