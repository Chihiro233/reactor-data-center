import groovy.json.JsonSlurper
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO
import pers.nanahci.reactor.datacenter.intergration.webhook.IdentifyToken
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO

class LarkText {

    /**
     *
     * @param opt
     * @param String
     * @return
     */
    def CommonWebHookDTO transform(TemplateDO.Config config, TemplateTaskDO task) {
        // 组装对象
        CommonWebHookDTO webHookDTO = new CommonWebHookDTO();
        webHookDTO.text = task.title;
        webHookDTO.msgType = 'text';
        webHookDTO.phone = config.phone
        webHookDTO.identifyToken = new IdentifyToken();
        webHookDTO.identifyToken.id = config.getWebHook();
        return webHookDTO;
    }

}
