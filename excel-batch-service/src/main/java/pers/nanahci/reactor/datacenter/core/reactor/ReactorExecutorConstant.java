package pers.nanahci.reactor.datacenter.core.reactor;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public interface ReactorExecutorConstant {

    Executor DEFAULT_SUBSCRIBE_EXECUTOR =
            new ThreadPoolExecutor(10, 20, 20,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), (t) -> new Thread(t, "reactor-subscribe-thread"));

    Executor DEFAULT_ERROR_EXECUTOR =
            new ThreadPoolExecutor(10, 10, 20,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), (t) -> new Thread(t, "error-handle-thread"));

    Executor SINGLE_ERROR_SINK_EXECUTOR =
            new ThreadPoolExecutor(1, 1, 0,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), (t) -> new Thread(t, "error-sink-thread"));

}
