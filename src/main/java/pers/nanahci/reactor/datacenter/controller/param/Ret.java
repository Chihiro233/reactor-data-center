package pers.nanahci.reactor.datacenter.controller.param;

import lombok.Data;
import reactor.core.publisher.Mono;

@Data
public class Ret<T> {

    private Integer code;

    private String msg;

    private T data;

    public static class CodeConstant {

        public static final Integer SUCCESS = 0;

        public static final Integer FAIL = 500;

        public static final String SUCCESS_MSG = "success";


    }

    public static final Ret<Void> SUCCESS = new Ret<>();

    public static <T> Mono<Ret<T>> success(Mono<T> mono) {
        return mono.map(t -> new Ret<T>(CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG, t));
    }

    public static Ret<Void> fail(Throwable t) {
        return new Ret<>(CodeConstant.FAIL, t.getMessage(), null);
    }

    public static <T> Ret<T> fail(Throwable t, T data) {
        return new Ret<>(CodeConstant.FAIL, t.getMessage(), data);
    }

    private Ret(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private Ret() {
        this.code = CodeConstant.SUCCESS;
        this.msg = CodeConstant.SUCCESS_MSG;
    }

}
