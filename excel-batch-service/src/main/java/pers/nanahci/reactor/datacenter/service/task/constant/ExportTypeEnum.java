package pers.nanahci.reactor.datacenter.service.task.constant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ExportTypeEnum {

    /**
     * represent no value,not isexist
     */
    NONE,
    /**
     * normal model
     */
    NORMAL,

    /**
     * template model
     */
    TEMPLATE;


    public static ExportTypeEnum of(String mode){
        try{
            return ExportTypeEnum.valueOf(mode);
        }catch (Exception e){
            log.error("",e);
            return ExportTypeEnum.NONE;
        }
    }





}
