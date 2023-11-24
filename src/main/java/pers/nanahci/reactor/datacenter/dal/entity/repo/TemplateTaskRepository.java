package pers.nanahci.reactor.datacenter.dal.entity.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import reactor.core.publisher.Flux;

public interface TemplateTaskRepository extends R2dbcRepository<TemplateTaskDO, Long> {


    @Query("select ts.* from template_task ts\n" +
            "join template tem on ts.batch_no = tem.batch_no\n" +
            "where ts.`status` in (1) and TIMESTAMPDIFF(SECOND,ts.last_start_time,NOW()) > tem.timeout_limit")
    Flux<TemplateTaskDO> selectTimeoutTask();

    @Query("select ts.* from template_task ts\n" +
            "join template tem on ts.batch_no = tem.batch_no\n" +
            "where ts.`status` = 4")
    Flux<TemplateTaskDO> selectFailTask();
}
