package Dandigo.SOS_map.domain.complaint.dto.request;

import jakarta.validation.constraints.NotNull;

// 11·17번 개인정보 점검 체크
public record PrivacyCheckRequest(
        @NotNull(message = "얼굴 블러 확인 여부를 선택해 주세요.") Boolean faceBlurConfirmed,
        @NotNull(message = "동물 사체 포함 여부를 선택해 주세요.") Boolean containsAnimalCarcass,
        @NotNull(message = "동물 블러 확인 여부를 선택해 주세요.") Boolean animalBlurConfirmed
) {
}
