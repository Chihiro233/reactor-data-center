package pers.nanahci.reactor.datacenter.core.netty;

import pers.nanachi.reactor.datacer.sdk.excel.core.netty.DataMessage;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class EndPointSinkPoll {


    private final static Map<Integer, Sinks.One<DataMessage>> pendingSinksMap = new ConcurrentHashMap<>();

    public EndPointSinkPoll() {

    }

    public static int alloc(){
        Sinks.One<DataMessage> sink = Sinks.one();
        return add(sink);
    }


    public static int add(Sinks.One<DataMessage> sink) {
        int sinksId = generateRandomId();
        pendingSinksMap.put(sinksId, sink);
        return sinksId;
    }

    public static Sinks.One<DataMessage> remove(int sinkId) {
        return pendingSinksMap.remove(sinkId);
    }

    public static Sinks.One<DataMessage> get(int sinkId){
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
