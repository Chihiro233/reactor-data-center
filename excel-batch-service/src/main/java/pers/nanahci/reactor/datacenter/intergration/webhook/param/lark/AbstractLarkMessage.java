package pers.nanahci.reactor.datacenter.intergration.webhook.param.lark;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@NoArgsConstructor
@Slf4j
@Data
public abstract class AbstractLarkMessage {

    protected String timestamp;

    protected String sign;

    protected String msg_type;

    protected String content;

    AbstractLarkMessage(String content) {
        this.content = content;
    }

    public AbstractLarkMessage sign(String secret) {
        if (sign == null) {
            log.debug("sign secret is null");
            return this;
        }
        long epochSecond = Instant.now().getEpochSecond();
        this.timestamp = String.valueOf(epochSecond);

        String stringToSign = epochSecond + "\n" + secret;
        //使用HmacSHA256算法计算签名
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(content.getBytes());
            this.sign = new String(Base64.encodeBase64(signData));
        } catch (Exception e) {
            log.error("generate sign failed!", e);
        }
        return this;
    }


}
