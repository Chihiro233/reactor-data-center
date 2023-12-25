package pers.nanahci.reactor.datacenter.util;

import io.netty.handler.codec.AsciiHeadersEncoder;
import org.springframework.beans.propertyeditors.URLEditor;

import java.beans.Encoder;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLUtils {




    public static String encodeUrlIgnoringSlash(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();

            if (path != null) {
                String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                return url.replace(path, encodedPath);
            }
        } catch (Exception e) {
            // Handle exception
            throw new RuntimeException("解析url失败");
        }

        return url;
    }

    public static void main(String[] args) {
        String encode = URLEncoder.encode("https://nmsl.com/dawdwa?name=3&kite=俞鸿泰");
        System.out.println(encode);

    }


}
