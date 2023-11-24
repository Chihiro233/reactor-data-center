package pers.nanahci.reactor.datacenter.core.file;

import com.alibaba.fastjson2.JSON;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pers.nanahci.reactor.datacenter.core.common.EasyURL;
import pers.nanahci.reactor.datacenter.service.UploadSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;

@Slf4j
public class S3FileClient extends AbstractFileClient {

    private static final String ALI_ENDPOINT = "aliyuncs.com";
    private static final String TX_ENDPOINT = "myqcloud.com";


    private S3ClientConfig config;

    private MinioClient minioClient;

    public S3FileClient(S3ClientConfig config) {
        this.config = config;
        init();
    }


    private void init() {
        minioClient = MinioClient.builder()
                .endpoint(buildEndPoint(config))
                .region(buildRegion(config))
                .credentials(config.getAccessKey(), config.getAccessSecret())
                .build();
    }

    public void refresh() {
        init();
        log.info("S3配置刷新完成");
    }

    private String buildEndPoint(S3ClientConfig config) {
        String endPoint = config.getEndPoint();
        if (StringUtils.startsWith(endPoint, "http") || StringUtils.startsWith(endPoint, "https")) {
            return endPoint;
        }
        return "https://" + endPoint;
    }

    private String buildRegion(S3ClientConfig config) {
        String endPoint = config.getEndPoint();
        if (StringUtils.contains(endPoint, TX_ENDPOINT)) {
            return StringUtils.substringAfter(endPoint, "cos.")
                    .replaceAll("." + TX_ENDPOINT, ""); // 去除 Endpoint
        }
        if (StringUtils.contains(endPoint, ALI_ENDPOINT)) {

        }
        return "";
    }

    @Override
    public InputStream getInputStream(String url) {
        return null;
    }

    @Override
    public void upload(byte[] data, long position, String url) {

    }

    @Override
    public String uploadLocalFile(String localPath, String path, String type) {
        File file = getFile(localPath);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("路径不存在或者不是文件");
        }
        long fileSize = file.length();
        UploadSetting setting = new UploadSetting()
                .setBucket(config.getBucket())
                .setFileType(type)
                .setFileLength(fileSize)
                .setPath(path);
        try {
            FileInputStream input = new FileInputStream(file);
            return doPut(setting, input);
        } catch (Exception e) {
            throw new RuntimeException("上传文件异常");
        }
    }

    @Override
    public String upload(InputStream ins, UploadSetting setting) {
        return doPut(setting, ins);
    }

    @Override
    public FileStoreType type() {
        return FileStoreType.S3;
    }

    private String buildFileUrl(String path) throws MalformedURLException {
        switch (config.getType()) {
            case S3CloudConstant.TX_CLOUD -> {
                String baseUrl = getTencentCloudBaseUrl();
                return EasyURL.from(baseUrl).concat(path).getEncodeUrl();
            }
            case S3CloudConstant.ALI_CLOUD -> {

            }
        }
        return "";
    }

    public static void main(String[] args) {
        String encode = URLEncoder.encode("https://reactor-batch-1304994440.cos.ap-nanjing.myqcloud.com/test/flux测试.xlsx");
        System.out.println(encode);
    }

    private String doPut(UploadSetting setting, InputStream input) {
        try {
            PutObjectArgs.Builder putArgsBuilder = PutObjectArgs.builder()
                    .bucket(config.getBucket()) // bucket 必须传递
                    .contentType(setting.getFileType())
                    .object(setting.getPath());// 相对路径作为 key
            if (setting.getFileLength() != null) {
                putArgsBuilder.stream(input, setting.getFileLength(), -1); // 文件内容
            } else {
                putArgsBuilder.stream(input, -1, 20 * 1024 * 1024);
            }
            // 执行上传
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putArgsBuilder.build());
            log.info("上传oss收到的参数:[{}]", JSON.toJSONString(objectWriteResponse));
            // 拼接返回路径
            return buildFileUrl(setting.getPath());
        } catch (Exception e) {
            log.info("[上传文件异常]", e);
        }
        return "";
    }

    private String getTencentCloudBaseUrl() {
        if (StringUtils.isBlank(config.getDomain())) {
            String baseUrl = buildEndPoint(config);
            if (StringUtils.contains(baseUrl, "//cos.")) {
                return baseUrl.replace("//cos", "//" + config.getBucket() + ".cos");
            }
        }
        return config.getDomain();
    }
}
