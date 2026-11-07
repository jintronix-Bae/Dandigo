package Dandigo.SOS_map.global.infra.image;

// 사진 크기 대비 비율(0~1)로 표현한 영역. Rekognition BoundingBox와 같은 방식.
public record RelativeBox(float left, float top, float width, float height) {
}
