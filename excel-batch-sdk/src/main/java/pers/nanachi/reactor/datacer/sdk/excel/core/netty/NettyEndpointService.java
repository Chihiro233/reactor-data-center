package pers.nanachi.reactor.datacer.sdk.excel.core.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.EventExecutorPoll;
import pers.nanachi.reactor.datacer.sdk.excel.core.task.TaskDispatcher;
import pers.nanachi.reactor.datacer.sdk.excel.utils.SysUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyEndpointService {

    private final ServerBootstrap serverBootstrap;

    private final EventLoopGroup eventLoopGroupSelector;

    private final EventLoopGroup eventLoopGroupBoss;

    private final DataChannelManager dataChannelManager;

    private final MessageProtocolServiceHandler messageProtocolServiceHandler;

    private ChannelFuture serverInstance;


    public NettyEndpointService(TaskDispatcher taskDispatcher) {
        this.serverBootstrap = new ServerBootstrap();
        this.eventLoopGroupSelector = buildEventLoopGroup();
        this.eventLoopGroupBoss = buildEventLoopBoss();
        this.dataChannelManager = new DataChannelManager();
        this.messageProtocolServiceHandler = new MessageProtocolServiceHandler(taskDispatcher);
    }


    @PostConstruct
    public void start() {

        this.serverInstance = this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupSelector)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addFirst(new IdleStateHandler(0, 0,
                                        NettyCoreConfig.maxIdleTime),dataChannelManager)
                                .addLast(EventExecutorPoll.DEFAULT_EVENT_EXECUTOR,
                                        new DataDecoder(NettyCoreConfig.maxFrameLength,
                                                NettyCoreConfig.lengthFieldOffset, NettyCoreConfig.lengthFieldLength,
                                                NettyCoreConfig.lengthAdjustment, NettyCoreConfig.initialBytesToStrip),
                                        new DataEncoder(),
                                        messageProtocolServiceHandler
                                );
                    }
                }).bind(9896);
    }


    @PreDestroy
    public void shutdown() throws InterruptedException {
        this.eventLoopGroupBoss.shutdownGracefully().sync();
        this.eventLoopGroupSelector.shutdownGracefully().sync();
        this.serverInstance.channel().closeFuture().sync();
    }

    private EventLoopGroup buildEventLoopGroup() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(3, new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);
                private final int threadTotal = NettyCoreConfig.selectorThreadsNum;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            return new NioEventLoopGroup(3, new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);
                private final int threadTotal = NettyCoreConfig.selectorThreadsNum;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        }
    }

    private EventLoopGroup buildEventLoopBoss() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(1, new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerEPOLLBoss_%d_%d", 1, this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            return new NioEventLoopGroup(1, new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerNIOBoss_%d_%d", 1, this.threadIndex.incrementAndGet()));
                }
            });
        }
    }

    private boolean useEpoll() {
        return SysUtils.isLinuxPlatform()
                && Epoll.isAvailable();
    }
}
