package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import lombok.Data;
import pers.nanahci.reactor.datacenter.intergration.webhook.IdentifyToken;


@Data
public class CommonWebHookDTO {

    /**
     * robot type, such as custom bots、app robot(for lark)
     */
    private Integer robotType;

    private String title;

    private String text;

    private String rich_text_lang;

    private String content;

    private String phone;

    private String userId;

    private String at;

    private String msgType;

    private IdentifyToken identifyToken;


}
