package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用于 API 上传的返回
 */
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    @Getter
    @Setter
    private boolean success;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String message;
}
