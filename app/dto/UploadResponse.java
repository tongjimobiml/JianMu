package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
