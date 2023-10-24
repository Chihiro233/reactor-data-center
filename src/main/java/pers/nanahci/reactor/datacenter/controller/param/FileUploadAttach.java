package pers.nanahci.reactor.datacenter.controller.param;

import lombok.Data;
import pers.nanahci.reactor.datacenter.enums.RpcEnum;

@Data
public class FileUploadAttach {

    // 调用类型
    private RpcEnum type;

    // 是否保持有序
    private boolean staySort;

    // 是否开启批处理模式
    private boolean batchModel;

    // 上传配置模板id
    private String templateSid;



}
