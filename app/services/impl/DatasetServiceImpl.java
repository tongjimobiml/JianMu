package services.impl;

import com.typesafe.config.Config;
import dto.DatasetDescription;
import lombok.extern.slf4j.Slf4j;
import play.libs.exception.ExceptionUtils;
import play.mvc.Http;
import services.DatasetService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class DatasetServiceImpl implements DatasetService {
    private final Config config;

    @Inject
    public DatasetServiceImpl(Config config) {
        this.config = config;
    }

    @Override
    public Path getUploadDirectory() throws IOException {
        String uploadDir = config.getString("upload-dataset.directory-name");
        Path p = Paths.get(uploadDir).toAbsolutePath();
        if (Files.notExists(p)) {
            Files.createDirectories(p);
        }
        return p;
    }

    @Override
    public List<DatasetDescription> listDataSet() {
        try {
            Path uploadDir = getUploadDirectory();
            try (Stream<Path> paths = Files.walk(Paths.get(uploadDir.toString()))) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(this::getFileDesc)
                        .sorted(Comparator.comparing(DatasetDescription::getUploadDate).reversed())
                        .collect(Collectors.toList());
            }
        } catch (IOException e1) {
            log.error("列出所有数据集时出错, 报错信息: {}", ExceptionUtils.getStackTrace(e1));
            return Collections.emptyList();
        }
    }

    @Override
    public String getToken() {
        return config.getString("upload-dataset.token");
    }

    @Override
    public boolean saveDataset(Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile> dataset) {
        // 调用时保证dataset不为空
        try {
            String fileName = dataset.getFilename();
            play.libs.Files.TemporaryFile file = dataset.getRef();
            Path uploadDir = getUploadDirectory();
            Path p = Paths.get(uploadDir.toString(), fileName);
            file.copyTo(p, true);
            return true;
        } catch (IOException e1) {
            log.error("无法创建上传文件的目录, 报错信息: {}", ExceptionUtils.getStackTrace(e1));
            return false;
        } catch (Exception e2) {
            log.error("存储文件发生未知错误, 报错信息: {}", ExceptionUtils.getStackTrace(e2));
            return false;
        }
    }

    @Override
    public Path getDatasetPath(String datasetName) {
        try {
            Path uploadDir = getUploadDirectory();
            return Paths.get(uploadDir.toString(), datasetName);
        } catch (IOException e) {
            log.error("获取dataset路径出错, 报错信息: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private DatasetDescription getFileDesc(Path p) {
        String name = p.toFile().getName();

        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        // 详细信息
        int recordNum = -1;
        String startDate = "", endData = "", uploadDate = "";
        long size = -1;
        boolean modified = false;

        // name格式示例: 嘉实-17号楼廖山河_211_1711051829_1711051833.txt
        String reversed = new StringBuilder(name).reverse().toString();
        String[] stats = reversed.split("_", 4);
        try {
            recordNum = Integer.parseInt(new StringBuilder(stats[2]).reverse().toString());
        } catch (Exception e1) {
            log.error("解析recordNum出错: {}", ExceptionUtils.getStackTrace(e1));
        }
        DateFormat df3 = new SimpleDateFormat("yyMMddHHmm");
        try {
            Date sd = df3.parse(new StringBuilder(stats[1]).reverse().toString());
            startDate = df2.format(sd);
        } catch (Exception e2) {
            log.error("解析startDate出错: {}", ExceptionUtils.getStackTrace(e2));
        }
        try {
            Date ed = df3.parse(new StringBuilder(stats[0].substring(4)).reverse().toString());
            endData = df2.format(ed);
        } catch (Exception e3) {
            log.error("解析endData出错: {}", ExceptionUtils.getStackTrace(e3));
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
            size = attrs.size();
            FileTime createTime = attrs.creationTime();
            FileTime modifyTime = attrs.lastModifiedTime();
            modified = createTime.compareTo(modifyTime) != 0;
            uploadDate = df1.format(createTime.toMillis());
        } catch (Exception e4) {
            log.error("获取数据集基本属性时出错, 报错信息: {}", ExceptionUtils.getStackTrace(e4));
        }

        return new DatasetDescription(name, recordNum, startDate, endData, uploadDate,
                DatasetDescription.formatReadableByteCount(size, true), modified);
    }
}
