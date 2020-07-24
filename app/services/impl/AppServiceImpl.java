package services.impl;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import play.libs.exception.ExceptionUtils;
import services.AppService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Singleton
public class AppServiceImpl implements AppService {
    private final Config config;

    @Inject
    public AppServiceImpl(Config config) {
        this.config = config;
    }

    @Override
    public Path getAppDirectory() throws IOException {
        String downloadDir = config.getString("download-app.directory-name");
        Path p = Paths.get(downloadDir).toAbsolutePath();
        if (Files.notExists(p)) {
            Files.createDirectories(p);
        }
        return p;
    }

    @Override
    public String getAppName() {
        return config.getString("download-app.app-file-name");
    }

    @Override
    public Path getAppPath() {
        try {
            Path downloadDir = getAppDirectory();
            return Paths.get(downloadDir.toString(), getAppName());
        } catch (IOException e) {
            log.error("获取APK路径出错, 报错信息: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
