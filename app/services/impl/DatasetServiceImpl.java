package services.impl;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import dto.DatasetDescription;
import lombok.extern.slf4j.Slf4j;
import play.libs.exception.ExceptionUtils;
import play.mvc.Http;
import services.DatasetService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
    public List<DatasetDescription> listDataSet(boolean ascending) {
        try {
            Comparator<DatasetDescription> c = Comparator.comparing(DatasetDescription::getUploadDate);
            if (!ascending) {
                c = c.reversed();
            }
            Path uploadDir = getUploadDirectory();
            try (Stream<Path> paths = Files.walk(Paths.get(uploadDir.toString()))) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(this::getFileDesc)
                        .sorted(c)
                        .collect(Collectors.toList());
            }
        } catch (IOException e1) {
            log.error("列出所有数据集时出错, 报错信息: {}", ExceptionUtils.getStackTrace(e1));
            return Collections.emptyList();
        }
    }

    @Override
    public List<DatasetDescription> listSortedDataSet(String sortAttr, boolean ascending) {
        Comparator<DatasetDescription> byStringAttr = Comparator.comparing(d -> this.<String>getTattr(d, sortAttr));
        Comparator<DatasetDescription> byIntAttr = Comparator.comparing(d -> this.<Integer>getTattr(d, sortAttr));
        Comparator<DatasetDescription> byLongAttr = Comparator.comparing(d -> this.<Long>getTattr(d, sortAttr));

        Set<String> stringAttrSet = ImmutableSet.of("name", "recordStartDate", "recordEndDate", "uploadDate");
        Set<String> intAttrSet = ImmutableSet.of("recordNum");
        Set<String> longAttrSet = ImmutableSet.of("fileSize");

        Comparator<DatasetDescription> comparator = null;
        // 为不同类型的属性选择不同的 Comparator
        if (stringAttrSet.contains(sortAttr)) {
            comparator = byStringAttr;
        } else if (intAttrSet.contains(sortAttr)) {
            comparator = byIntAttr;
        } else if (longAttrSet.contains(sortAttr)) {
            comparator = byLongAttr;
        } else {
            log.error("sortAttr错误: {}", sortAttr);
            return listDataSet(ascending);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        try {
            Path uploadDir = getUploadDirectory();
            try (Stream<Path> paths = Files.walk(Paths.get(uploadDir.toString()))) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(this::getFileDesc)
                        .sorted(comparator)
                        .collect(Collectors.toList());
            }
        } catch (IOException e1) {
            log.error("按属性列出所有数据集时出错, 报错信息: {}", ExceptionUtils.getStackTrace(e1));
            return Collections.emptyList();
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

    @Override
    public boolean saveDataset(Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile> dataset) {
        // 调用时已保证 dataset 不为 null
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
    public String getToken() {
        return config.getString("upload-dataset.token");
    }


    /* private methods */

    /**
     * 从一个数据文件的路径获取相关信息，数据文件的命名需要遵循 Android APP 导出的文件命名格式，否则会解析出错
     *
     * @param p 路径
     * @return 相关信息的描述
     */
    private DatasetDescription getFileDesc(Path p) {
        String name = p.toFile().getName();

        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        // 详细信息
        int recordNum = -1;
        String startDate = "", endData = "", uploadDate = "";
        long size = -1;
        boolean modified = false;

        /*
         * 从文件名解析部分信息
         * 数据文件全名（name）的格式：{文件名}_{收集的记录条数}_{该次记录收集的开始时间}_{该次记录收集的结束时间}.txt
         * 示例：嘉实-17号楼_211_1711051829_1711051833.txt
         */
        String reversed = new StringBuilder(name).reverse().toString();
        String[] stats = reversed.split("_", 4);
        try {
            recordNum = Integer.parseInt(new StringBuilder(stats[2]).reverse().toString());
        } catch (Exception e1) {
            log.warn("解析recordNum出错: {}", name);
        }
        DateFormat df3 = new SimpleDateFormat("yyMMddHHmm");
        try {
            Date sd = df3.parse(new StringBuilder(stats[1]).reverse().toString());
            startDate = df2.format(sd);
        } catch (Exception e2) {
            log.warn("解析startDate出错: {}", name);
        }
        try {
            Date ed = df3.parse(new StringBuilder(stats[0].substring(4)).reverse().toString());
            endData = df2.format(ed);
        } catch (Exception e3) {
            log.warn("解析endData出错: {}", name);
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

        return new DatasetDescription(name, recordNum, startDate, endData, uploadDate, size);
    }

    /**
     * 根据属性名获取属性值，并根据模板参数进行类型转换
     *
     * @param obj  要获取属性的对象
     * @param attr 属性名
     * @param <T>  属性值的数据类型
     * @return 指定类型的属性值
     */
    private <T> T getTattr(Object obj, String attr) {
        Object value = getAttr(obj, attr);
        @SuppressWarnings("unchecked")
        T t = (T) value;
        return t;
    }

    /**
     * 利用反射根据属性名获得属性值
     *
     * @param obj  要获取属性的对象
     * @param attr 属性名
     * @return Object 类型的属性值
     */
    private Object getAttr(Object obj, String attr) {
        try {
            Field field = obj.getClass().getDeclaredField(attr);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
