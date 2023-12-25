package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.Map;

@Data
public class RichTextNode {

    private String tag;

    private String text;

    private String href;

    private Map<String,Object> attrs;

    public JSONObject toJSONObj(){
        JSONObject obj = new JSONObject();
        obj.put("tag",tag);
        obj.put("text",text);
        obj.put(href,href);
        obj.putAll(attrs);
        return obj;
    }

}
