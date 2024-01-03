package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class RpcRequest<T> {


    private Attach attach;

    private T data;

    @Data
    @Accessors(chain = true)
    public static class Attach {

        private String serviceId;

        private Integer taskType;

        private int retryNum;

        private int timeout;

    }

    public static <T> RpcRequest<T> get(String serviceId, Integer taskType) {
        Attach attach = new Attach()
                .setTaskType(taskType)
                .setServiceId(serviceId)
                .setRetryNum(3)
                .setTimeout(3000);
        RpcRequest<T> request = new RpcRequest<>();
        request.setAttach(attach);
        return request;
    }


}
