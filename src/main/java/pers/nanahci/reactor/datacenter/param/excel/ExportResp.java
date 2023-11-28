package pers.nanahci.reactor.datacenter.param.excel;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExportResp {

    private Map<String, String> attach;

    private List<JSONObject> rows;

}
