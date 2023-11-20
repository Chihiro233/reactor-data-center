package pers.nanahci.reactor.datacenter.core.file;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
public class S3FileClient extends AbstractFileClient {

    private static final String ALI_ENDPOINT = "aliyuncs.com";
    private static final String TX_ENDPOINT = "myqcloud.com";


    private S3ClientConfig config;

    private MinioClient minioClient;

    public S3FileClient(S3ClientConfig config) {
        this.config = config;
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
            return StringUtils.substringAfter(endPoint, ".cos.")
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
    public String uploadLocalFile(String tempPath, String path, String type) {
        File file = getFile(tempPath);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("路径不存在或者不是文件");
        }
        long fileSize = file.length();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            // 执行上传
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket()) // bucket 必须传递
                    .contentType(type)
                    .object(path) // 相对路径作为 key
                    .stream(inputStream, fileSize, -1) // 文件内容
                    .build());
            // 拼接返回路径
            if (StringUtils.isBlank(config.getDomain())) {
                return buildEndPoint(config) + "/" + path;
            }
            return config.getDomain() + "/" + path;
        } catch (Exception e) {
            log.info("[上传文件异常]", e);
        }
        return "";

    }

    @Override
    public void upload(InputStream ins, String url) {

    }

    @Override
    public FileStoreType type() {
        return FileStoreType.S3;
    }
}
