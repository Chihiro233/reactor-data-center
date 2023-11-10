package pers.nanahci.reactor.datacenter.core.reactor;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public interface ExecutorConstant {

    Executor DEFAULT_SUBSCRIBE_EXECUTOR =
            new ThreadPoolExecutor(1, 1, 20,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), (t) -> new Thread(t, "reactor-subscribe-thread"));

    //Executor WORK_EXECUTOR =
           // new ThreadPoolExecutor(100,120,);

}
