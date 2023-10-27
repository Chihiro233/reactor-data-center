package pers.nanahci.reactor.datacenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlatformTypeEnum {

    Lark(1, "飞书"),

    DingDing(2,"钉钉")

    ;


    private final Integer value;

    private final String desc;


    }
