package pers.nanahci.reactor.datacenter.intergration.webhook;

import lombok.Data;

@Data
public class IdentifyToken {

    private String id;

    private String secret;

    private String appId;

    private String token;

}
