package Dandigo.SOS_map.global.infra.rekognition;

import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import Dandigo.SOS_map.global.infra.image.RelativeBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.util.List;

// AWS Rekognition으로 사진 속 얼굴 위치를 찾는다 (9번)
@Slf4j
@Component
@RequiredArgsConstructor
public class FaceDetector {

    private final RekognitionClient rekognitionClient;

    public List<RelativeBox> detectFaces(byte[] imageBytes) {
        try {
            return rekognitionClient.detectFaces(DetectFacesRequest.builder()
                            .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
                            .build())
                    .faceDetails().stream()
                    .map(face -> toBox(face.boundingBox()))
                    .toList();
        } catch (SdkException e) {
            log.error("Rekognition 얼굴 감지 실패", e);
            throw new CustomException(ErrorCode.AI_UNAVAILABLE);
        }
    }

    private RelativeBox toBox(BoundingBox box) {
        return new RelativeBox(box.left(), box.top(), box.width(), box.height());
    }
}
