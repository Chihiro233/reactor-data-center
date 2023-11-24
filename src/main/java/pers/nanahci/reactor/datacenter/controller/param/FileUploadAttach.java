package pers.nanahci.reactor.datacenter.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;
import pers.nanahci.reactor.datacenter.enums.RpcEnum;

@Data
@Accessors(chain = true)
public class FileUploadAttach {


    // 上传配置模板id
    private String batchNo;

    private String title;

    private String bizInfo;

    private Long contentLength;



}
