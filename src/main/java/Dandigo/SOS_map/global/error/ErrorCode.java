package Dandigo.SOS_map.global.error;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 다시 확인해 주세요"),
    FESTIVAL_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "종료된 축제에는 불편을 신고할 수 없습니다."),
    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 요청을 처리할 권한이 없습니다."),
    // 404
    COMPLAINT_NOT_FOUND(HttpStatus.NOT_FOUND, "민원을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,"사진을 찾을 수 없습니다."),
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "축제를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
    // 405
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    // 409
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    IMAGE_IN_USE(HttpStatus.CONFLICT, "이미 민원에 등록된 사진은 삭제할 수 없습니다."),
    COMPLAINT_NOT_EDITABLE(HttpStatus.CONFLICT, "처리가 시작된 민원은 수정할 수 없습니다."),
    COMPLAINT_NOT_DELETABLE(HttpStatus.CONFLICT, "처리가 시작된 민원은 삭제할 수 없습니다."),
    // 413
    FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "사진은 3MB 이하로 올려주세요."),
    // 415
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "jpg, png, heic 형식의 사진만 올릴 수 있습니다."),
    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요");

    private final HttpStatus status;
    private final String message;
}

