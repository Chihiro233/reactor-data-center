package pers.nanachi.reactor.datacer.sdk.excel.core.netty;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Accessors(chain = true)
@Builder
public class MessageProtocol {
    // request or response
    private byte command;

    private ProtocolHeader header;
    // message body
    private byte[] data;


    @Data
    @Accessors(chain = true)
    public static class ProtocolHeader {


        private long msgId;


        private int taskType;


    }


}
