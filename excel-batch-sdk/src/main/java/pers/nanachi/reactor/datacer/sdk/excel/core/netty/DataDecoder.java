package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

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
    protected MessageProtocol decode(ChannelHandlerContext ctx, ByteBuf inEx) throws Exception {
        // 长度
        ByteBuf in = (ByteBuf) super.decode(ctx, inEx);
        if (in == null) {
            return null;
        }
        if (in.readableBytes() < NettyCoreConfig.headSize) {
            return null;
        }
        int frameLength = in.readInt();
        if (in.readableBytes() < frameLength) {
            return null;
        }
        MessageProtocol.MessageProtocolBuilder builder = MessageProtocol.builder();

        byte type = in.readByte();
        builder.command(type);
        builder.header(readHeader(in));

        byte[] dataBytes = new byte[frameLength - NettyCoreConfig.headerLength - NettyCoreConfig.typeLength];
        in.readBytes(dataBytes);

        builder.data(dataBytes);
        return builder.build();
    }

    private MessageProtocol.ProtocolHeader readHeader(ByteBuf in) {
        return new MessageProtocol.ProtocolHeader()
                .setMsgId(in.readLong())
                .setTaskType(in.readInt());
    }


}
