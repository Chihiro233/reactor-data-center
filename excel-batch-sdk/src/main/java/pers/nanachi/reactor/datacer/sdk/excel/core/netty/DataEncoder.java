package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeEnum;
import pers.nanachi.reactor.datacer.sdk.excel.core.seralize.SerializeFactory;

@Slf4j
public class DataEncoder extends MessageToByteEncoder<MessageProtocol> {


    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) throws Exception {
        int messageLength = NettyCoreConfig.typeLength + NettyCoreConfig.headerLength;
        byte[] dataBytes = null;

        if (msg.getData() != null) {
            dataBytes = SerializeFactory.serialize(SerializeEnum.FASTJSON2, msg.getData());
            messageLength += dataBytes.length;
        }

        byte type = msg.getCommand();
        out.writeInt(messageLength);
        out.writeByte(type);

        out.writeLong(msg.getHeader().getMsgId());
        out.writeInt(msg.getHeader().getTaskType());

        if (dataBytes != null) {
            out.writeBytes(dataBytes);
        }
    }
}
