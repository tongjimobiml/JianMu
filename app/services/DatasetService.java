package services;

import com.google.inject.ImplementedBy;
import dto.DatasetDescription;
import play.libs.Files;
import play.mvc.Http;
import services.impl.DatasetServiceImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 处理数据集上传相关的方法
 */
@ImplementedBy(DatasetServiceImpl.class)
public interface DatasetService {

    /**
     * 从配置文件获取数据文件目录，不存在则创建
     *
     * @return 目录
     * @throws IOException 创建目录异常时抛出 IOException
     */
    Path getUploadDirectory() throws IOException;

    /**
     * 按上传日期列出所有数据文件，上传日期为文件系统中该数据文件的创建时间，因此诸如复制一份文件并删除原文件的操作会改变上传日期
     *
     * @param ascending 按升序还是降序排列
     * @return 排好序的列表，列表的每个元素包含了关于数据文件的必要信息
     */
    List<DatasetDescription> listDataSet(boolean ascending);

    /**
     * 按指定的属性列出所有数据文件
     *
     * @param sortAttr  属性名
     * @param ascending 按升序还是降序排列
     * @return 排好序的列表
     */
    List<DatasetDescription> listSortedDataSet(String sortAttr, boolean ascending);

    /**
     * 根据数据集名字获取一个数据文件的路径
     *
     * @param datasetName 数据集名
     * @return 该数据集的路径
     */
    Path getDatasetPath(String datasetName);

    /**
     * 保存上传的数据文件
     *
     * @param dataset 上传的数据集
     * @return 是否保存成功
     */
    boolean saveDataset(Http.MultipartFormData.FilePart<Files.TemporaryFile> dataset);

    /**
     * 获取从网页上传数据文件需要的 token
     *
     * @return
     */
    String getToken();
}
