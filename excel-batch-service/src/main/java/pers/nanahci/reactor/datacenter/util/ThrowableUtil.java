package pers.nanahci.reactor.datacenter.util;

import org.apache.commons.lang3.StringUtils;
import reactor.netty.channel.AbortedException;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.Set;

public class ThrowableUtil {

    private static final Set<Class<?>> DISCONNECTED_CLIENT_EXCEPTIONS =
            Set.of(AbortedException.class,
                    ClosedChannelException.class,
                    EOFException.class,
                    ConnectException.class);

    public static boolean isDisconnectedClientError(Throwable throwable) {
        Class<?> clazz = throwable.getClass();
        if (DISCONNECTED_CLIENT_EXCEPTIONS.contains(clazz)) {
            return true;
        }
        if (throwable instanceof IOException) {
            String message = throwable.getMessage();
            if (message == null) {
                return false;
            }
            return StringUtils.containsIgnoreCase(message,
                    "An existing connection was forcibly closed")
                    || StringUtils.containsIgnoreCase(message, "Connection reset")
                    || StringUtils.containsIgnoreCase(message, "Broken pipe");
        }
        return false;
    }


}
