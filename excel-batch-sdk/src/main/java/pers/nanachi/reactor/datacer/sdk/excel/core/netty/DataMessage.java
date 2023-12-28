package pers.nanachi.reactor.datacer.sdk.excel.core.netty;


import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Getter
@Builder
@ToString
@Accessors(chain = true)
public class DataMessage {
    // request or response
    private byte command;
    // represent whether the message is success
    private Integer code;

    private Long msgId;
    // additional messages, example pageNo, taskName
    private Attach attach;
    // message body
    private byte[] data;


    @Data
    public static class Attach {

        // task type: export or import
        private Integer taskType;


        private String batchNo;

        // task identification
        private String taskName;

        // export pageNo
        private Integer pageNo;

        // task stage
        /**
         * @See pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage
         */
        private Integer stage;


    }

    public static DataMessage buildRespData(Object data, Attach attach, Integer code) {
        return DataMessage.builder()
                .command(CommandType.Resp)
                .attach(attach)
                .data(dataConvert(data))
                .code(code).build();
    }

    public static DataMessage buildRespData(Object data, Integer code) {
        return buildRespData(data, null, code);
    }

    public static DataMessage buildRespData(Object data) {
        return buildRespData(data, null, RespCode.SUCCESS);
    }


    public static DataMessage buildReqData(Object data, Attach attach) {
        return DataMessage.builder()
                .command(CommandType.Req)
                .attach(attach)
                .code(RespCode.REQ_DEFAULT)
                .data(dataConvert(data)).build();
    }

    public static DataMessage buildReqData(Object data) {
        return buildReqData(data, null);
    }


    public static byte[] dataConvert(Object data) {
        byte[] dataBytes;
        if (data instanceof byte[] bytes) {
            dataBytes = bytes;
        } else if (data instanceof String dataStr) {
            dataBytes = dataStr.getBytes(StandardCharsets.UTF_8);
        } else {
            dataBytes = JSON.toJSONBytes(data);
        }
        return dataBytes;
    }

    public static class RespCode {

        public static final Integer REQ_DEFAULT = 0;

        public static final Integer SUCCESS = 200;

        public static final Integer FAIL = 500;

    }

    public boolean whetherSuccess() {
        if (Objects.equals(command, CommandType.Req)) {
            return true;
        }
        return Objects.equals(code, RespCode.SUCCESS);
    }

    public DataMessage nextPage() {
        if (attach != null) {
            nextPage(attach.getPageNo() + 1);
        } else {
            throw new RuntimeException("page info is null");
        }
        return this;
    }

    public DataMessage nextPage(int pageNo) {
        if (attach != null) {
            attach.setPageNo(pageNo);
        } else {
            throw new RuntimeException("page info is null");
        }
        return this;
    }

}
