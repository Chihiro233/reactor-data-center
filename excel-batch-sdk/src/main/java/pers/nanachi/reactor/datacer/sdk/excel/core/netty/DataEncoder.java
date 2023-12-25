package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataEncoder extends MessageToByteEncoder<DataMessage> {


    @Override
    protected void encode(ChannelHandlerContext ctx, DataMessage msg, ByteBuf out) throws Exception {
        int messageLength = NettyCoreConfig.typeLength + NettyCoreConfig.codeLength + NettyCoreConfig.msgIdLength + NettyCoreConfig.payLoadLength;
        byte[] dataBytes = null;
        byte[] attachLength = null;
        if (msg.getAttach() != null) {
            attachLength = JSON.toJSONBytes(msg.getAttach());
            messageLength += attachLength.length;
        }
        if (msg.getData() != null) {
            dataBytes = msg.getData();
            messageLength += dataBytes.length;
        }

        byte type = msg.getCommand();
        out.writeInt(messageLength);
        out.writeByte(type);
        out.writeInt(msg.getCode());
        out.writeLong(msg.getMsgId() == null ? 1L : msg.getMsgId());

        if (attachLength != null) {
            out.writeInt(attachLength.length);
            out.writeBytes(attachLength);
        } else {
            out.writeInt(0);
        }
        if (dataBytes != null) {
            out.writeBytes(dataBytes);
        }
    }
}
