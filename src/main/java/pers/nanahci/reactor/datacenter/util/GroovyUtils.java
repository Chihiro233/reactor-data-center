package pers.nanahci.reactor.datacenter.util;

import groovy.lang.GroovyShell;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class GroovyUtils {

    public final static Map<String, GroovyShell> map = new ConcurrentHashMap<>();

    public static void add(String key, GroovyShell shell) {
        map.put(key, shell);
    }

    public static GroovyShell computeIfAbsent(String key, Function<String, GroovyShell> fun) {
        return map.computeIfAbsent(key, fun);
    }


}
