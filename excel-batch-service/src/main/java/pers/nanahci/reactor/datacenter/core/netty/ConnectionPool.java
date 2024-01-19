package pers.nanahci.reactor.datacenter.core.netty;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConnectionPool {


    private static final Map<String, ArrayDeque<RConnectionHolder>> pool = new ConcurrentHashMap<>();

    void add(RConnectionHolder conn) {
        pool.computeIfAbsent(conn.getRemoteHost(), key -> new ArrayDeque<>())
                .add(conn);
    }

    public RConnectionHolder poll(String host) {
        RConnectionHolder conn;
        ArrayDeque<RConnectionHolder> connes = pool.get(host);
        do {
            if (CollectionUtils.isEmpty(connes)) {
                return null;
            }
            conn = connes.poll();

        } while (conn != null && conn.isDisposed());

        return conn;
    }

}
