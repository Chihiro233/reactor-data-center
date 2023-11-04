package pers.nanahci.reactor.datacenter.service;

import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import reactor.core.publisher.Flux;


public interface TemplateService {

    /**
     * @param id template id
     */

    void execute(Long id, String batchNo);

    Flux<TemplateModel> getUnComplete();

    void execute(TemplateModel templateModel);


}
