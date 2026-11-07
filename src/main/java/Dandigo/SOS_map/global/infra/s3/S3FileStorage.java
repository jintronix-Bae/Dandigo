package Dandigo.SOS_map.global.infra.s3;

import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

// S3 업로드/다운로드/삭제. DB에는 URL만 저장하고, 키는 URL에서 다시 계산한다.
@Slf4j
@Component
public class S3FileStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String baseUrl;

    public S3FileStorage(S3Client s3Client,
                         @Value("${aws.s3.bucket}") String bucket,
                         @Value("${aws.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.baseUrl = "https://%s.s3.%s.amazonaws.com/".formatted(bucket, region);
    }

    // 업로드 후 공개 URL 반환 (버킷이 공개 읽기로 설정되어 있어야 앱에서 열린다)
    public String upload(String key, byte[] bytes, String contentType) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(bytes));
            return baseUrl + key;
        } catch (SdkException e) {
            log.error("S3 업로드 실패 key={}", key, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public byte[] download(String url) {
        try {
            return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(toKey(url))
                            .build())
                    .asByteArray();
        } catch (SdkException e) {
            log.error("S3 다운로드 실패 url={}", url, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // 삭제 실패는 로그만 남긴다 (DB 정리가 우선)
    public void delete(String url) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(toKey(url))
                    .build());
        } catch (SdkException e) {
            log.warn("S3 삭제 실패 url={}", url, e);
        }
    }

    private String toKey(String url) {
        if (url == null || !url.startsWith(baseUrl)) {
            throw new IllegalArgumentException("이 버킷의 URL이 아닙니다: " + url);
        }
        return url.substring(baseUrl.length());
    }
}
