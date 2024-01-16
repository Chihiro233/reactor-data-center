package pers.nanahci.reactor.datacenter.core.netty;


import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.util.concurrent.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import reactor.netty.Connection;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConnectionPool {


    private static final Map<String, ArrayDeque<RConnection>> pool = new ConcurrentHashMap<>();

    void add(RConnection conn) {
        pool.computeIfAbsent(conn.getRemoteHost(), key -> new ArrayDeque<>())
                .add(conn);
    }

    public RConnection poll(String host) {
        RConnection conn;
        ArrayDeque<RConnection> connes = pool.get(host);
        do {
            if (connes.isEmpty()) {
                return null;
            }
            conn = connes.poll();

        } while (conn.isDisposed());

        return conn;
    }

}
