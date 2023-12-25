package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@EqualsAndHashCode(callSuper = true)
public class LarkTextMessage extends AbstractLarkMessage {

    {
        this.msg_type = "text";
    }

    LarkTextMessage(String content) {
        super(content);
    }

    @Data
    @Accessors(chain = true)
    public static class Builder {

        private String text;

        public LarkTextMessage build() {
            JSONObject contentJson = new JSONObject();
            contentJson.put("text", text);
            return new LarkTextMessage(contentJson.toJSONString());
        }


    }
}
