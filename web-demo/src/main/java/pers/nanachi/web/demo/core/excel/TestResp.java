package pers.nanachi.web.demo.core.excel;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class TestResp {

    @JSONField(name = "姓名")
    private String name;

    @JSONField(name = "八字")
    private String bazi;





}
