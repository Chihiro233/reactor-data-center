package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataDecoder extends LengthFieldBasedFrameDecoder {


    public DataDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
                       int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }


    @Override
    protected DataMessage decode(ChannelHandlerContext ctx, ByteBuf inEx) throws Exception {
        // 长度
        ByteBuf in = (ByteBuf) super.decode(ctx, inEx);
        if (in == null) {
            return null;
        }
        //TODO 疑惑点
        // if readable bytes length less than
        if (in.readableBytes() < NettyCoreConfig.headSize) {
            return null;
        }
        int frameLength = in.readInt();
        if (in.readableBytes() < frameLength) {
            return null;
        }
        DataMessage.DataMessageBuilder builder = DataMessage.builder();

        byte type = in.readByte();
        int code = in.readInt();
        long msgId = in.readLong();
        int attachLength = in.readInt();
        if (attachLength != 0) {
            byte[] attachBytes = new byte[attachLength];
            in.readBytes(attachBytes);
            DataMessage.Attach attach = JSON.parseObject(attachBytes, DataMessage.Attach.class);
            builder.attach(attach);
        }
        byte[] dataBytes = new byte[frameLength - NettyCoreConfig.codeLength - NettyCoreConfig.msgIdLength - NettyCoreConfig.typeLength - NettyCoreConfig.payLoadLength - attachLength];
        in.readBytes(dataBytes);
        builder.command(type);
        builder.msgId(msgId);
        builder.data(dataBytes);
        builder.code(code);
        in.release();
        return builder.build();
    }
}
