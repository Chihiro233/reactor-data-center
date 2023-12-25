package pers.nanahci.reactor.datacenter.dal.repo;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskInstanceDO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


public interface TemplateTaskInstanceRepository extends R2dbcRepository<TemplateTaskInstanceDO, Long> {

    @Query("select * from template_task_instance where task_id = :taskId order by start_time desc limit 1")
    Mono<TemplateTaskInstanceDO> lastOne(Long taskId);

    @Modifying
    @Query("update template_task_instance set end_time = :time, err_rows = :errRows where id = :id")
    Mono<Long> complete(Long id, LocalDateTime time,Integer errRows);


    @Modifying
    @Query("update template_task_instance set end_time = :time, err_info = :errInfo where id = :id")
    Mono<Long> fail(Long id, LocalDateTime time, String errInfo);


}
