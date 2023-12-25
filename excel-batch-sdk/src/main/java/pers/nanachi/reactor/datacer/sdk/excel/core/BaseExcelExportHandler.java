package pers.nanachi.reactor.datacer.sdk.excel.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class BaseExcelExportHandler<R, T> implements ExcelExportHandler<R, T> {

    private Type R_;

    private Type T_;

    public BaseExcelExportHandler() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if ((genericSuperclass instanceof ParameterizedType parameterizedType)) {
            R_ = parameterizedType.getActualTypeArguments()[0];
            T_ = parameterizedType.getActualTypeArguments()[1];
        } else {
            R_ = Object.class;
            T_ = Object.class;
        }

    }


    @Override
    public abstract List<List<String>> getExcelHeaders(T param);

    @Override
    public abstract List<R> getExportData(Integer pageNo, T param);

    @Override
    public abstract Map<String, String> templateFill(T param);

    public final List<R> getExportData0(Integer pageNo, byte[] param) {
        T paramT = JSON.parseObject(param, T_);
        return getExportData(pageNo, paramT);
    }

    public final List<List<String>> getExcelHeaders(byte[] param) {
        T paramT = JSON.parseObject(param, T_);
        return getExcelHeaders(paramT);
    }

    public final Map<String, String> templateFill(byte[] param) {
        T t = convertParam(param);
        return templateFill(t);
    }


}
