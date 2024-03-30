package pers.nanahci.reactor.datacenter.core.netty;

import lombok.extern.slf4j.Slf4j;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcResponse;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class RequestSinkPoll {


    // msg Id -> msgId Sink
    private final static Map<Long, Sinks.One<RpcResponse<?>>> pendingSinksMap = new ConcurrentHashMap<>();

    public RequestSinkPoll() {

    }

    public static long alloc() {
        Sinks.One<RpcResponse<?>> sink = Sinks.one();
        return add(sink);
    }


    public static long add(Sinks.One<RpcResponse<?>> requestId) {
        long sinksId = generateRequestId();
        pendingSinksMap.put(sinksId, requestId);
        return sinksId;
    }

    public static Sinks.One<RpcResponse<?>> remove(long requestId) {
        return pendingSinksMap.remove(requestId);
    }

    public static Sinks.One<RpcResponse<?>> get(long requestId) {
        return pendingSinksMap.get(requestId);
    }

    public static long generateRequestId() {
        long id;
        do {
            id = ThreadLocalRandom.current()
                    .nextLong(1, Long.MAX_VALUE);
        } while (pendingSinksMap.containsKey(id));
        return id;
    }


}
