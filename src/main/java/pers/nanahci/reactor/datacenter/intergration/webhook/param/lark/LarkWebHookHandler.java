package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pers.nanahci.reactor.datacenter.core.reactor.ReactorWebClient;
import pers.nanahci.reactor.datacenter.intergration.webhook.AbstractWebHookHandler;
import pers.nanahci.reactor.datacenter.intergration.webhook.IdentifyToken;
import pers.nanahci.reactor.datacenter.intergration.webhook.enums.PlatformTypeEnum;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class LarkWebHookHandler extends AbstractWebHookHandler {

    private ReactorWebClient reactorWebClient;

    private static final String webhookUrl = "https://open.feishu.cn/open-apis/bot/v2/hook/";

    @Override
    public Mono<?> handle(Object platformMsg, IdentifyToken identifyToken) {
        if (!(platformMsg instanceof AbstractLarkMessage larkMessage)) {
            return Mono.error(new RuntimeException("message type isn't lark!"));
        }
        larkMessage.sign(identifyToken.getSecret());
        return reactorWebClient.post(webhookUrl + identifyToken.getId(),
                JSON.toJSONString(larkMessage),
                String.class);
    }

    @Override
    public Object parseParam(CommonWebHookDTO param) {
        switch (param.getMsgType()) {
            case "text":
                return buildTextMessage(param);
            case "post":
                return buildRichTextMessage(param);
            default:

        }
        throw new RuntimeException("msg type is not exist: [" + param.getMsgType() + "]");
    }

    @Override
    public PlatformTypeEnum type() {
        return PlatformTypeEnum.Lark;
    }

    private LarkTextMessage buildTextMessage(CommonWebHookDTO param) {
        LarkTextMessage.Builder builder = new LarkTextMessage.Builder()
                .setText(param.getText());
        return builder.build();

    }

    private LarkRichTextMessage buildRichTextMessage(CommonWebHookDTO param) {
        LarkRichTextMessage.Builder builder = new LarkRichTextMessage.Builder()
                .setTitle(param.getTitle())
                .setLang(param.getRich_text_lang())
                .setTitle(param.getTitle())
                .setNodeList(JSONArray.parseArray(param.getContent(), RichTextNode.class));
        return builder.build();

    }
}
