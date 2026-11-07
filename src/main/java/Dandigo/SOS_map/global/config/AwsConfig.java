package Dandigo.SOS_map.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;

// S3(사진 저장), Rekognition(얼굴 감지) 클라이언트
@Configuration
public class AwsConfig {

    private final Region region;
    private final AwsCredentialsProvider credentialsProvider;

    public AwsConfig(@Value("${aws.region}") String region,
                     @Value("${aws.access-key:}") String accessKey,
                     @Value("${aws.secret-key:}") String secretKey) {
        this.region = Region.of(region);
        // 로컬: .env 의 IAM 키 사용 / 배포(EC2): 키를 비워두면 EC2 역할(Role) 권한을 자동 사용
        this.credentialsProvider = StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                : DefaultCredentialsProvider.builder().build();
    }

    @Bean(destroyMethod = "close")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean(destroyMethod = "close")
    public RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
