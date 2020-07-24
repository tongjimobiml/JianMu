package services;

import com.google.inject.ImplementedBy;
import services.impl.AppServiceImpl;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 处理 Android 安装程序下载相关的方法
 */
@ImplementedBy(AppServiceImpl.class)
public interface AppService {

    /**
     * 从配置文件获取 Android 安装程序目录，不存在则创建
     *
     * @return 目录
     * @throws IOException 创建目录异常时抛出 IOException
     */
    Path getAppDirectory() throws IOException;

    /**
     * 从配置文件获取 Android 安装程序的文件名
     *
     * @return 文件名
     */
    String getAppName();

    /**
     * 获取 Android 安装程序的路径
     *
     * @return 路径，失败时返回 null
     */
    Path getAppPath();
}
