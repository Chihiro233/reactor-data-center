package pers.nanahci.reactor.datacenter.service;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadSetting {

    private String bucket;

    private String path;

    private String fileType;

    private Long fileLength;


}
