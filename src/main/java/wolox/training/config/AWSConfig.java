package wolox.training.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Component
public class AWSConfig {

    @Bean
    public S3Presigner preSigner() {
        return S3Presigner
            .builder()
            .credentialsProvider(awsProfileFile())
            .region(Region.US_EAST_2)
            .build();
    }

    @Bean
    public AwsCredentialsProvider awsProfileFile() {
        return ProfileCredentialsProvider
            .builder()
            .profileFile(ProfileFile.defaultProfileFile())
            .build();
    }
}
