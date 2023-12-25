package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
public class LarkRichTextMessage extends AbstractLarkMessage{


    LarkRichTextMessage(String content){
        this.content = content;
    }

    @Data
    @Accessors(chain = true)
    public static class Builder{
        // zh_cn or en_us
        private String lang;

        private String title;

        private String content;

        private List<RichTextNode> nodeList;

        public LarkRichTextMessage build(){
            JSONObject richText = new JSONObject();
            richText.put("title",title);
            JSONArray innerContent = new JSONArray();
            nodeList.forEach(node->{
                innerContent.add(node.toJSONObj());
            });
            // inner content level 3
            richText.put("content",innerContent);
            JSONObject content = new JSONObject();
            JSONObject post = new JSONObject();
            // rich text level 2
            post.put(lang, richText);
            // post level 1
            content.put("post",post);

            return new LarkRichTextMessage(content.toJSONString());
        }

    }

}
