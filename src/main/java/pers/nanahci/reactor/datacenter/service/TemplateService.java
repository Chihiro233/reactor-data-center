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

    /**
     * 获取未开始的的任务
     *
     * @return
     */
    Flux<TemplateModel> getUnComplete();

    /**
     * 执行任务
     *
     * @param templateModel
     */
    void execute(TemplateModel templateModel);

    /**
     * 更新任务状态
     *
     * @param task
     * @param taskStatus
     * @return
     */
    Mono<Long> updateTaskStatus(TemplateTaskDO task, TaskStatusEnum taskStatus);

    /**
     * 提交任务
     *
     * @param file       上传的任务文件
     * @param attachMono 附加信息
     * @return 返回的文件url
     */
    Mono<String> commit(FilePart file, FileUploadAttach attachMono);

    /**
     * 保存的错误任务文件
     *
     * @param taskId     任务id
     * @param errFileUrl 错误的文件url
     * @return 更新条目数量
     */
    Mono<Long> saveErrFileUrl(Long taskId, String errFileUrl);

    /**
     * 查找超时的任务
     *
     * @return
     */
    Flux<TemplateModel> getTimeoutTask();

    /**
     * 处理超时的任务
     */
    void resolveTimeoutTask(TemplateModel model);

}
