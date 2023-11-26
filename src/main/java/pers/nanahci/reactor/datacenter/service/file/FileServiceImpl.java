package pers.nanahci.reactor.datacenter.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pers.nanahci.reactor.datacenter.core.file.FileClient;
import pers.nanahci.reactor.datacenter.core.file.FileClientFactory;
import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.file.AccessSetting;

import java.io.InputStream;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Override
    public String upload(InputStream ins, AccessSetting setting, FileStoreType type) {
        FileClient fileClient = FileClientFactory.get(type);
        return fileClient.upload(ins, setting);
    }


}
