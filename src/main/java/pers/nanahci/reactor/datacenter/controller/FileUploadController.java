package pers.nanahci.reactor.datacenter.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pers.nanahci.reactor.datacenter.controller.param.FileUploadAttach;
import pers.nanahci.reactor.datacenter.controller.param.Ret;
import pers.nanahci.reactor.datacenter.service.FileService;
import pers.nanahci.reactor.datacenter.service.TemplateService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequestMapping("file")
@RestController
@AllArgsConstructor
public class FileUploadController {

    private FileService fileService;

    private TemplateService templateService;

    @PostMapping("upload")
    public Mono<Ret<String>> upload(@RequestPart("file") FilePart file,
                                    @RequestPart("title") String title,
                                    @RequestPart("bizInfo") String bizInfo,
                                    @RequestPart("batchNo") String batchNo,
                                    @RequestHeader("Content-length") Long contentLength) {
        // 上传文件
        FileUploadAttach attach = new FileUploadAttach()
                .setTitle(title)
                .setBizInfo(bizInfo)
                .setBatchNo(batchNo)
                .setContentLength(contentLength);
        return Ret.success(templateService.commit(file, attach));
    }

}
