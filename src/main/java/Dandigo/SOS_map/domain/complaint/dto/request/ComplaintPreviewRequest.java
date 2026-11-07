package Dandigo.SOS_map.domain.complaint.dto.request;

import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 9번 (imageId = 14번에서 받은 원본 사진 번호)
public record ComplaintPreviewRequest(
        @NotNull(message = "사진을 먼저 올려 주세요.") Long imageId,
        @NotNull(message = "카테고리를 선택해 주세요.") ComplaintCategory category,
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 50, message = "제목은 50자 이하로 입력해 주세요.") String title,
        @NotBlank(message = "상세 내용을 입력해 주세요.")
        @Size(max = 300, message = "상세 내용은 300자 이하로 입력해 주세요.") String description,
        @NotBlank(message = "주소를 입력해 주세요.")
        @Size(max = 255, message = "주소가 너무 깁니다.") String address,
        @NotNull(message = "위치(위도)가 필요합니다.")
        @DecimalMin(value = "-90", message = "올바른 위도가 아닙니다.")
        @DecimalMax(value = "90", message = "올바른 위도가 아닙니다.") Double latitude,
        @NotNull(message = "위치(경도)가 필요합니다.")
        @DecimalMin(value = "-180", message = "올바른 경도가 아닙니다.")
        @DecimalMax(value = "180", message = "올바른 경도가 아닙니다.") Double longitude,
        Long festivalId
) {
}
