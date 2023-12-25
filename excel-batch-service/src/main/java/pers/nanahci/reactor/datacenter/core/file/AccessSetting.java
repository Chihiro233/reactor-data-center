package pers.nanahci.reactor.datacenter.core.file;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccessSetting {

    private String bucket;

    private String path;

    private String fileType;

    private Long fileLength;


}
