package pers.nanahci.reactor.datacenter.core.file;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileClientFactory {

    private final static Map<FileStoreType,FileClient> mapping = new ConcurrentHashMap<>();

    static {
        mapping.put(FileStoreType.LOCAL,new LocalFileClient());
    }

    public static FileClient get(FileStoreType type){
        return mapping.get(type);
    }

}
