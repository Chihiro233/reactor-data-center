import groovy.json.JsonSlurper
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO
import pers.nanahci.reactor.datacenter.intergration.webhook.IdentifyToken
import pers.nanahci.reactor.datacenter.intergration.webhook.param.lark.CommonWebHookDTO

class LarkText {

    static void main(String[] args) {

    }

    /**
     *
     * @param opt
     * @param String
     * @return
     */
    def CommonWebHookDTO transform(TemplateDO.Config config, TemplateTaskDO task) {
        // 组装对象
        CommonWebHookDTO webHookDTO = new CommonWebHookDTO();
        webHookDTO.text = '你好groovy';
        webHookDTO.msgType = 'text';
        webHookDTO.phone = '18557522311'
        webHookDTO.identifyToken = new IdentifyToken();
        webHookDTO.identifyToken.id = config.getWebHook();
        return webHookDTO;
    }

}
