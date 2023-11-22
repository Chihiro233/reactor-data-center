package pers.nanahci.reactor.datacenter.core.common;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
public class EasyURL {

    private String url;

    private String domain;

    private String path;

    private boolean valid;

    public static EasyURL from(String url, boolean valid) {
        return new EasyURL(url, valid);
    }

    public static EasyURL from(String url) {
        return new EasyURL(url, true);
    }

    private EasyURL(String url, boolean valid) {
        parse(url);
    }

    public EasyURL concat(String path) {
        String tempUrl = url;
        if (url.endsWith("/") && path.startsWith("/")) {
            tempUrl = url + path.substring(1);
        } else if (!url.endsWith("/") && !path.startsWith("/")) {
            tempUrl = url + "/" + path;
        }
        parse(tempUrl);
        return this;
    }


    private void parse(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url为空");
        }
        if (valid && !url.contains("http://") && !url.contains("https://")) {
            throw new IllegalArgumentException("url不合法");
        }
        String[] split = url.split("//");
        String protocol = split[0];
        String urlWithoutProtocol = split[1].replaceAll("//", "/");
        this.domain = split[0] + "//" + StringUtils.substringBefore(urlWithoutProtocol, "/");
        this.path = StringUtils.substringAfter(urlWithoutProtocol, "/");
        this.url = url;
    }

    public String getEncodeUrl() {
        String curPath = this.path;
        String[] parts = StringUtils.split(curPath, "/");
        StringBuilder encodePathBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String encode = URLEncoder.encode(parts[i], StandardCharsets.UTF_8);
            encodePathBuilder.append(encode);
            if (i < parts.length - 1) {
                encodePathBuilder.append("/");
            }
        }
        return domain + "/" + encodePathBuilder.toString();
    }

}
