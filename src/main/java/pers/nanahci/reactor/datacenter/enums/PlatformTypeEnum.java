package pers.nanahci.reactor.datacenter.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum PlatformTypeEnum {


    Lark(1, "飞书"),

    DingDing(2, "钉钉");

    PlatformTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    private final Integer value;

    private final String desc;

    public static PlatformTypeEnum of(Integer value) {
        return Arrays.stream(PlatformTypeEnum.values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }

}
