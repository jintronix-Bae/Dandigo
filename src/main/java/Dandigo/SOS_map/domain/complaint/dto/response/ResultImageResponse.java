package Dandigo.SOS_map.domain.complaint.dto.response;

// 10번 (description은 카테고리별 고정 문구)
public record ResultImageResponse(Long resultImageId, String resultImageUrl, String description) {
}
