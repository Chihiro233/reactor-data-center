package pers.nanahci.reactor.datacenter.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import pers.nanahci.reactor.datacenter.controller.param.FileUploadAttach;
import pers.nanahci.reactor.datacenter.dal.entity.TemplateTaskDO;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import pers.nanahci.reactor.datacenter.enums.TaskStatusEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface TemplateService {

    Flux<TemplateModel> getUnComplete();

    void execute(TemplateModel templateModel);

    Mono<Long> updateTaskStatus(TemplateTaskDO task, TaskStatusEnum taskStatus);

    Mono<String> commit(FilePart file, FileUploadAttach attachMono);

    Mono<Long> saveErrFileUrl(Long taskId, String errFileUrl);


}
