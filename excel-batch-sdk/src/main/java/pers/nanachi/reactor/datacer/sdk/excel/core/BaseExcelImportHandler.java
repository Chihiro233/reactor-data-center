package pers.nanachi.reactor.datacer.sdk.excel.core;

import com.alibaba.fastjson2.JSONB;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
public abstract class BaseExcelImportHandler<T> implements ExcelImportHandler<T> {

    private final Type type;

    public BaseExcelImportHandler() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if ((genericSuperclass instanceof ParameterizedType parameterizedType)) {
            type = parameterizedType.getActualTypeArguments()[0];
        }else{
            type = Object.class;
        }
    }

    public abstract void importExecute(T param);


    public final void importExecute(byte[] param) {
        T paramT = JSONB.parseObject(param, type);
        importExecute(paramT);
    }


}
