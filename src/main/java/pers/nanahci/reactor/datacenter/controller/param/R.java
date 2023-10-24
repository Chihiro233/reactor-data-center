package pers.nanahci.reactor.datacenter.controller.param;

public class R<T> {

    private Integer code;
    private String msg;
    private T data;

    public static class CodeConstant{

        public static final Integer SUCCESS = 0;

        public static final String SUCCESS_MSG = "success";

    }

    public static final R<Void> SUCCESS = new R<>();

    private R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private R() {
        this.code = CodeConstant.SUCCESS;
        this.msg = CodeConstant.SUCCESS_MSG;
    }

}
