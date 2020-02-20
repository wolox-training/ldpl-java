package wolox.training.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wolox.training.models.dtos.S3UploadRequest;
import wolox.training.services.S3FileUploadService;

@RestController
@RequestMapping("/api/files/")
public class FileController {

    private final S3FileUploadService s3FileUploadService;

    @Autowired
    public FileController(S3FileUploadService s3FileUploadService) {
        this.s3FileUploadService = s3FileUploadService;
    }

    @PostMapping("/")
    public String createPreSignedUrl(@Valid @RequestBody S3UploadRequest request,
        @RequestHeader("x-content-type") String contentType) {
        return s3FileUploadService.preSignPutObjectUrl(request.getKey(), contentType);
    }

    @GetMapping("/")
    public String readPreSignedUrl(@Valid @RequestBody S3UploadRequest request,
        @RequestHeader("x-content-type") String contentType) {
        return s3FileUploadService.preSignGetObjectUrl(request.getKey());
    }

}
