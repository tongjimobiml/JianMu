package services;

import com.google.inject.ImplementedBy;
import dto.DatasetDescription;
import play.libs.Files;
import play.mvc.Http;
import services.impl.DatasetServiceImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@ImplementedBy(DatasetServiceImpl.class)
public interface DatasetService {

    Path getUploadDirectory() throws IOException;

    List<DatasetDescription> listDataSet();

    String getToken();

    boolean saveDataset(Http.MultipartFormData.FilePart<Files.TemporaryFile> dataset);

    Path getDatasetPath(String datasetName);

}
