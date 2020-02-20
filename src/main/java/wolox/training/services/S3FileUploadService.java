package wolox.training.services;

public interface S3FileUploadService {

    String preSignPutObjectUrl(String objectKey, String mimeType);

    String preSignGetObjectUrl(String objectKey);

}
