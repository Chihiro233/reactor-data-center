package pers.nanahci.reactor.datacenter.service.task.constant;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    UN_START_RETRY(-1, "未开始, 需要重试"),

    UN_START(0, "未开始"),

    WORKING(1, "进行中"),

    COMPLETE(2, "完成"),

    TERMINAL(3, "结束"),

    FAIL(4, "执行异常");

    private final Integer value;

    private final String desc;

    TaskStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


}
