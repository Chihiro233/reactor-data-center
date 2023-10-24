package pers.nanahci.reactor.datacenter.dal.entity.repo;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateDO;



public interface TemplateRepository extends R2dbcRepository<TemplateDO,Long> {


}
