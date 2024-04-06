package pers.nanachi.web.demo.core.excel.export;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.checkerframework.checker.units.qual.N;

@Data
public class TestResp {

    @JSONField(name = "姓名")
    private String yuwen;

    @JSONField(name = "八字")
    private String math;

    @JSONField(name = "英语")
    private String english;





}
