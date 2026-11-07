package Dandigo.SOS_map.domain.complaint.dto.response;

// 9번 (contentCheck 없음: 통과 여부는 사용자가 확인 화면에서 직접 판단)
public record ComplaintPreviewResponse(
        String agency,
        String estimatedPeriod,
        String area,
        PreviewImage image
) {
    public record PreviewImage(Long originalImageId, Long blurredImageId, String blurredImageUrl, int faceCount) {
    }
}
