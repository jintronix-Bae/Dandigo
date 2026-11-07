package Dandigo.SOS_map.domain.complaint.dto.request;

import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

// 17번: 바꾼 항목만 보낸다. null(또는 미포함)인 항목은 그대로 유지
public record ComplaintUpdateRequest(
        @Size(max = 50, message = "제목은 50자 이하로 입력해 주세요.") String title,
        @Size(max = 300, message = "상세 내용은 300자 이하로 입력해 주세요.") String description,
        ComplaintCategory category,
        @Size(max = 255, message = "주소가 너무 깁니다.") String address,
        @DecimalMin(value = "-90", message = "올바른 위도가 아닙니다.")
        @DecimalMax(value = "90", message = "올바른 위도가 아닙니다.") Double latitude,
        @DecimalMin(value = "-180", message = "올바른 경도가 아닙니다.")
        @DecimalMax(value = "180", message = "올바른 경도가 아닙니다.") Double longitude,
        Long imageId,
        Long festivalId,
        @Valid PrivacyCheckRequest privacyCheck
) {
}
