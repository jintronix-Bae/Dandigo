package Dandigo.SOS_map.domain.complaint.dto.request;

import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 10번 (blurredImageId = 9번 응답의 blurredImageId)
public record ResultImageRequest(
        @NotNull(message = "블러된 사진 번호가 필요합니다.") Long blurredImageId,
        @NotNull(message = "카테고리를 선택해 주세요.") ComplaintCategory category,
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 50, message = "제목은 50자 이하로 입력해 주세요.") String title,
        @NotBlank(message = "상세 내용을 입력해 주세요.")
        @Size(max = 300, message = "상세 내용은 300자 이하로 입력해 주세요.") String description
) {
}
