package pers.nanahci.reactor.datacenter.core.netty;

import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;
import pers.nanachi.reactor.datacer.sdk.excel.core.netty.RpcResponse;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class EndPointSinkPoll {


    private final static Map<Integer, Sinks.One<RpcResponse<?>>> pendingSinksMap = new ConcurrentHashMap<>();

    public EndPointSinkPoll() {

    }

    public static int alloc(){
        Sinks.One<RpcResponse<?>> sink = Sinks.one();
        return add(sink);
    }


    public static int add(Sinks.One<RpcResponse<?>> sink) {
        int sinksId = generateRandomId();
        pendingSinksMap.put(sinksId, sink);
        return sinksId;
    }

    public static Sinks.One<RpcResponse<?>> remove(int sinkId) {
        return pendingSinksMap.remove(sinkId);
    }

    public static Sinks.One<RpcResponse<?>> get(int sinkId){
        return pendingSinksMap.get(sinkId);
    }

    public static int generateRandomId() {
        int id;
        do {
            id = ThreadLocalRandom.current()
                    .nextInt(1, Integer.MAX_VALUE);
        } while (pendingSinksMap.containsKey(id));
        return id;
    }


}
