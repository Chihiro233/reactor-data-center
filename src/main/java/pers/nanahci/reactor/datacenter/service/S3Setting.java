package pers.nanahci.reactor.datacenter.service;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class S3Setting {

    private String bucket;

    private String path;

    private String fileType;

    private Long fileLength;


}