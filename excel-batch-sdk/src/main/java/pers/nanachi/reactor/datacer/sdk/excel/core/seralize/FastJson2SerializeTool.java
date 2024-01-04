package pers.nanachi.reactor.datacer.sdk.excel.core.seralize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

public class FastJson2SerializeTool implements SerializeTool{



    @Override
    public byte[] serialize(Object obj) {
        if(obj instanceof byte[] objs){
            return objs;
        }
        return JSON.toJSONBytes(obj);
    }


    @Override
    public <T> T deserialize(byte[] dataBytes, Class<T> type) {
        return JSON.parseObject(dataBytes,type);
    }
}
