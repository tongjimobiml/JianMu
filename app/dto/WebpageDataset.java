package dto;

import lombok.Getter;
import lombok.Setter;
import play.libs.Files;
import play.mvc.Http;

/**
 * 用于网页上传
 */
public class WebpageDataset {

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private Http.MultipartFormData.FilePart<Files.TemporaryFile> file;

    @Getter
    @Setter
    private String sort;

    @Getter
    @Setter
    private boolean ascending;
}
