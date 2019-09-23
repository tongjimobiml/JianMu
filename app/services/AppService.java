package services;

import com.google.inject.ImplementedBy;
import services.impl.AppServiceImpl;

import java.io.IOException;
import java.nio.file.Path;

@ImplementedBy(AppServiceImpl.class)
public interface AppService {

    Path getAppDirectory() throws IOException;

    Path getAppPath();

    String getAppName();
}
