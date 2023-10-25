package pers.nanahci.reactor.datacenter.core.file;

import lombok.Data;

import java.io.File;

@Data
public abstract class AbstractFileClient implements FileClient{

    private Long clientId;

    public File getFile(String url){
        File file = new File(url);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("target url is not exist or is not a file");
        }
        return file;
    }


}
