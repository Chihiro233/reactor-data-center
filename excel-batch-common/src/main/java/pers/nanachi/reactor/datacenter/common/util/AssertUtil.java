package pers.nanachi.reactor.datacenter.common.util;

import java.util.function.Supplier;

public class AssertUtil {


    public static void isTrue(Supplier<Boolean> supplier, String errInfo) {
        if (supplier == null) {
            throw new RuntimeException("supplier can't be null, errInfo is : " + errInfo);
        }
        if (supplier.get() == null || !supplier.get()) {
            throw new RuntimeException(errInfo);
        }
    }

}
