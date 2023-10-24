package pers.nanahci.reactor.datacenter.dal.entity.repo;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;

public interface TemplateTaskRepository extends R2dbcRepository<TemplateTaskDO,Long> {
}
