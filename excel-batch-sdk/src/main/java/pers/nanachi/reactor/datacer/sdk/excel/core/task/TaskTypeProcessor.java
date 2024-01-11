package pers.nanachi.reactor.datacer.sdk.excel.core.task;

import pers.nanachi.reactor.datacer.sdk.excel.core.netty.MessageProtocol;

public interface TaskTypeProcessor {

    Object handle(MessageProtocol messageProtocol);

    Integer type();

}
