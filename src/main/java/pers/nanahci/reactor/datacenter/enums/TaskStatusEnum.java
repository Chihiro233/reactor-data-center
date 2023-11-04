package pers.nanahci.reactor.datacenter.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    UN_STARTER(0, "未开始"),

    WORKING(1, "进行中"),

    COMPLETE(2, "结束");

    private final Integer value;

    private final String desc;

    TaskStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


}
