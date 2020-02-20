package wolox.training.services.impl;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import wolox.training.services.S3FileUploadService;

@Service
public class S3FileUploadServiceImpl implements S3FileUploadService {

    private final S3Presigner preSigner;

    @Value("${aws.s3.presigned.url.duration}")
    private Long s3PresignedDuration;

    @Value("${aws.s3.bucket}")
    private String s3BucketName;

    @Value("${aws.s3.trainee.folder}")
    private String s3TraineeFolder;

    @Autowired
    public S3FileUploadServiceImpl(S3Presigner preSigner) {
        this.preSigner = preSigner;
    }

    @Override
    public String preSignPutObjectUrl(String key, String mimeType) {
        PutObjectRequest putObjectRequest = PutObjectRequest
            .builder()
            .bucket(s3BucketName)
            .key(String.format("%s/%s", s3TraineeFolder, key))
            .contentType(mimeType)
            .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest
            .builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofSeconds(s3PresignedDuration))
            .build();

        PresignedPutObjectRequest presignedPutObjectRequest = preSigner
            .presignPutObject(putObjectPresignRequest);

        return presignedPutObjectRequest.url().toString();
    }

    @Override
    public String preSignGetObjectUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest
            .builder()
            .bucket(s3BucketName)
            .key(String.format("%s/%s", s3TraineeFolder, objectKey))
            .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest
            .builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(Duration.ofSeconds(s3PresignedDuration))
            .build();

        PresignedGetObjectRequest presignedGetObjectRequest = preSigner
            .presignGetObject(getObjectPresignRequest);

        return presignedGetObjectRequest.url().toString();
    }
}
