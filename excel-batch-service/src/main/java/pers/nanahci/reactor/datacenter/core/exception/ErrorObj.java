package pers.nanahci.reactor.datacenter.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorObj {

    private Object rowData;

    private Throwable e;

}
