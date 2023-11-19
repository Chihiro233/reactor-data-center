package pers.nanahci.reactor.datacenter.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import pers.nanahci.reactor.datacenter.controller.param.FileUploadAttach;
import pers.nanahci.reactor.datacenter.domain.template.TemplateModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface TemplateService {

    Flux<TemplateModel> getUnComplete();

    void execute(TemplateModel templateModel);

    Mono<String> commit(FilePart file, FileUploadAttach attachMono);


}
