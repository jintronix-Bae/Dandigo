package Dandigo.SOS_map.global.error;

import org.springframework.http.ResponseEntity;

import java.util.List;

public record ErrorResponse(
        Integer status,
        String code,
        String message,
        List<FieldErrorDetail> errors
) {

    // #0.입력값 검증 실패 항목
    public record FieldErrorDetail(String field, String reason) {}

    // #1.ErrorCode를 받아서 자신 객체를 만드는 팩토리 메서드
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                List.of() // 일반 에러는 빈 배열 반환
        );
    }

    // #2.ResponseEntity까지 한 번에 묶어서 반환해 주는 팩토리 메서드
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // #3.@Valid 에서 커스텀 메세지를 전달받는 팩토리 메서드
    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldErrorDetail> errors) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.name(),
                message,
                errors
        );
    }
}
