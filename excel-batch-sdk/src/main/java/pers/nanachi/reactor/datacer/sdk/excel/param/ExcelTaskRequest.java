package pers.nanachi.reactor.datacer.sdk.excel.param;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExcelTaskRequest {


    private String batchNo;
    // task identification
    private String taskName;
    // export pageNo
    private Integer pageNo;
    // task stage
    /**
     * @see  pers.nanachi.reactor.datacer.sdk.excel.core.ExportExecuteStage
     */
    private Integer stage;

    private String bizInfo;

    public ExcelTaskRequest nextPage(){
        this.pageNo++;
        return this;
    }

}
