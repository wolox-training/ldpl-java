package wolox.training.models.dtos;

import javax.validation.constraints.NotEmpty;

public class S3UploadRequest {

    @NotEmpty
    private String key;

    public String getKey() {
        return key;
    }
}
