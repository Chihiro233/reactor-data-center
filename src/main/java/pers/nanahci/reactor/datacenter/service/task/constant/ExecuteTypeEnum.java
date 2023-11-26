package pers.nanahci.reactor.datacenter.service.task.constant;

import lombok.Getter;

@Getter
public enum ExecuteTypeEnum {

    Single(1, "单个"),

    Batch(2, "批量");

    private final Integer value;

    private final String desc;

    ExecuteTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

}
