package pers.nanachi.reactor.datacer.sdk.excel.core.seralize;

import java.util.HashMap;
import java.util.Map;

public class SerializeFactory {

    private static final Map<SerializeEnum, SerializeTool> serializerMap = new HashMap<>();

    static {
        serializerMap.put(SerializeEnum.FASTJSON2, new FastJson2SerializeTool());
    }

    public static byte[] serialize(SerializeEnum serializeEnum, Object data) {
        return serializerMap.get(serializeEnum).serialize(data);
    }

    public static <T> T deserialize(SerializeEnum serializeEnum, byte[] data, Class<T> type) {
        return serializerMap.get(serializeEnum).deserialize(data, type);
    }


}
