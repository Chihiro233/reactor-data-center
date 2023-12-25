package pers.nanachi.reactor.datacer.sdk.excel.core;

import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class EventExecutorPoll {


    public static final DefaultEventExecutorGroup DEFAULT_EVENT_EXECUTOR = new DefaultEventExecutorGroup(10, new ThreadFactory() {

        private final AtomicInteger thread_seq = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "NettyServerThread_" + thread_seq.incrementAndGet());
        }
    });

    public static final DefaultEventExecutorGroup DEFAULT_EVENT_CLIENT_EXECUTOR = new DefaultEventExecutorGroup(10, new ThreadFactory() {

        private final AtomicInteger thread_seq = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "NettyClientThread_" + thread_seq.incrementAndGet());
        }
    });

}
