package Dandigo.SOS_map.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // #1.CustomException 발생시 해당 메서드가 가로채서 처리한다.
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException : {}", e.getErrorCode().getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    // #2.dto의 @Valid 검증 실패 시 에러 처리(항목별 오류를 errors에 담는다)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldErrorDetail> errorList = e.getBindingResult().getFieldErrors().stream().map(fe -> new ErrorResponse.FieldErrorDetail(fe.getField(), fe.getDefaultMessage())).toList();
        String message = errorList.isEmpty() ? ErrorCode.INVALID_INPUT.getMessage() : errorList.get(0).reason();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message, errorList));
    }

    // #3.login 실패 처리
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.LOGIN_FAILED);
    }

    // #4.JSON 형식 오류 및 Body 누락
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT);
    }

    // #5.동시 가입 등으로 인한 DB 유니크 제약조건 위반(uk_member_userid)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String detail = String.valueOf(e.getMostSpecificCause().getMessage());
        if (detail.contains("uk_users_login_id")){
            return ErrorResponse.toResponseEntity(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (detail.contains("uk_users_email")){
            return ErrorResponse.toResponseEntity(ErrorCode.DUPLICATE_EMAIL);
        }
        // 여기까지 오면 서버 에러
        log.error("DataIntegrityViolationException : {}", e);
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_ERROR);
    }

    // #6.지원하지 않는 HTTP 메서드
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // #7.존재하지 않는 API 주소
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceException(NoResourceFoundException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.RESOURCE_NOT_FOUND);
    }

    // #8.필수 쿼리 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT);
    }

    // #9.경로, 쿼리 값의 타입 오류
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT);
    }

    // #10.사진 용량 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ErrorResponse.toResponseEntity(ErrorCode.FILE_TOO_LARGE);
    }

    // #11.예기치 못한 예외 처리
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception : {}", e);
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_ERROR);
    }



}
