package pers.nanahci.reactor.datacenter.service.file;

import pers.nanahci.reactor.datacenter.core.file.FileStoreType;
import pers.nanahci.reactor.datacenter.core.file.AccessSetting;

import java.io.InputStream;

public interface FileService {


    String upload(InputStream ins, AccessSetting setting, FileStoreType fileStoreType);



}
