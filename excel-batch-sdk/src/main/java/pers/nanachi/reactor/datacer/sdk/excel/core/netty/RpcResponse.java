package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class RpcResponse<T> {

    private Integer code;

    private T data;

    private String msg;


    public static class RespCode {

        public static final Integer REQ_DEFAULT = 0;

        public static final Integer SUCCESS = 200;

        public static final Integer FAIL = 500;

    }

    public boolean isSuccess() {
        return Objects.equals(RespCode.SUCCESS, code);
    }


}
