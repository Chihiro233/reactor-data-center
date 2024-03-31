package pers.nanachi.reactor.datacer.sdk.excel.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class BaseExcelExportHandler<R, T> implements ExcelExportHandler<R,T> {

    private final Type R_;

    private final Type T_;

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

    public static void main(String[] args) {

    }


    public abstract List<List<String>> getExcelHeaders(T param);

    public abstract List<R> getExportData(Integer pageNo, T param);

    public abstract Map<String,Object> getExportFill(T param);

    public abstract Map<String, String> templateFill(T param);

    public final List<R> getExportData0(Integer pageNo, String param) {
        T paramT = JSON.parseObject(param, T_);
        return getExportData(pageNo, paramT);
    }

    public final Map<String,Object> getExportFill0(String param) {
        T paramT = JSON.parseObject(param, T_);
        return getExportFill(paramT);
    }

    public final List<List<String>> getExcelHeaders0(String param) {
        T paramT = JSON.parseObject(param, T_);
        return getExcelHeaders(paramT);
    }

    public final Map<String, String> templateFill(byte[] param) {
        T paramT = JSON.parseObject(param, T_);
        return templateFill(paramT);
    }


}
