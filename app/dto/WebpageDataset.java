package dto;

import lombok.Getter;
import lombok.Setter;
import play.libs.Files;
import play.mvc.Http;


public class WebpageDataset {

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private Http.MultipartFormData.FilePart<Files.TemporaryFile> file;

}
