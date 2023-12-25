package pers.nanachi.reactor.datacer.sdk.excel.core.netty;

public interface NettyCoreConfig {

    Integer maxFrameLength = Integer.MAX_VALUE;
    Integer lengthFieldOffset = 0;
    Integer lengthFieldLength = 4;

    Integer lengthAdjustment = 0;
    Integer initialBytesToStrip = 0;

    Integer headSize = 4;

    Integer typeLength = 1;

    Integer codeLength = 4;

    Integer payLoadLength = 4;
    Integer msgIdLength = 8;

    Integer PROXY_BIND_PORT = 18081;


    /*-------------------------eventLoop thread config-----------------------*/

    Integer selectorThreadsNum = 3;

    Integer maxIdleTime = 3;







}
