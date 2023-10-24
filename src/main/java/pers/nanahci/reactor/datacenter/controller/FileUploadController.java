package pers.nanahci.reactor.datacenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pers.nanahci.reactor.datacenter.controller.param.FileUploadAttach;
import pers.nanahci.reactor.datacenter.controller.param.R;

@RestController
@RequestMapping("file")
public class FileUploadController {

    @PostMapping("upload")
    public R<Void> upload(MultipartFile file, @RequestBody FileUploadAttach fileUploadAttach){

        return R.SUCCESS;
    }

}
