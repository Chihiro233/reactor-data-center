package pers.nanahci.reactor.datacenter.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import pers.nanahci.reactor.datacenter.controller.param.Ret;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    /**
     * Create a new {@code AbstractErrorWebExceptionHandler}.
     *
     * @param errorAttributes    the error attributes
     * @param properties          the resources configuration properties
     * @param applicationContext the application context
     * @since 2.4.0
     */
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties properties,
                                          ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, properties.getResources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::resolveErrorResponse);
    }

    private Mono<ServerResponse> resolveErrorResponse(ServerRequest request) {
        Ret<Void> ret = resolveThrowable(request);
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ret));
    }

    private Ret<Void> resolveThrowable(ServerRequest request) {
        Throwable error = getError(request);
        return Ret.fail(error);
    }


}
