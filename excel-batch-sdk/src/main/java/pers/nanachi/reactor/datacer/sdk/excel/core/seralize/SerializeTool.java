package pers.nanachi.reactor.datacer.sdk.excel.core.seralize;

public interface SerializeTool {


    byte[] serialize(Object obj);

    <T> T deserialize(byte[] dataBytes, Class<T> type);

}
