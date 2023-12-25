package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LarkPictureMessage extends AbstractLarkMessage{

    {
        this.msg_type = "image";
    }

    @Data
    public static class Builder{

        private String imageKey;



    }

}
