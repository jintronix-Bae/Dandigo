package Dandigo.SOS_map.domain.complaint.controller;

import Dandigo.SOS_map.domain.complaint.dto.request.ComplaintCreateRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.ComplaintPreviewRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.ComplaintUpdateRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.ResultImageRequest;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintDetailResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintPageResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintPreviewResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.EmpathyResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.NearbyComplaintsResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ResultImageResponse;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintStatus;
import Dandigo.SOS_map.domain.complaint.service.ComplaintCommandService;
import Dandigo.SOS_map.domain.complaint.service.ComplaintQueryService;
import Dandigo.SOS_map.domain.complaint.service.EmpathyService;
import Dandigo.SOS_map.global.security.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 민원 API (6~13, 17, 18번)
@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintQueryService complaintQueryService;
    private final ComplaintCommandService complaintCommandService;
    private final EmpathyService empathyService;

    // #6.사용자 주변(지도 화면 범위) 민원 조회
    @GetMapping("/nearby")
    public ResponseEntity<NearbyComplaintsResponse> nearby(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam double minLat, @RequestParam double maxLat,
            @RequestParam double minLng, @RequestParam double maxLng,
            @RequestParam(required = false) ComplaintCategory category) {
        return ResponseEntity.ok(complaintQueryService.findNearby(
                authUser.userId(), minLat, maxLat, minLng, maxLng, category));
    }

    // #7.전체 민원 목록 조회 (지역·카테고리·축제·상태 필터, 페이지)
    @GetMapping
    public ResponseEntity<ComplaintPageResponse> list(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) Long festivalId,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = ComplaintQueryService.SORT_LATEST) String sort,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1~100 사이여야 합니다.")
            @Max(value = 100, message = "size는 1~100 사이여야 합니다.") int size) {
        return ResponseEntity.ok(complaintQueryService.search(
                authUser.userId(), area, category, festivalId, status, sort, page, size));
    }

    // #8.민원 상세 보기
    @GetMapping("/{complaintId}")
    public ResponseEntity<ComplaintDetailResponse> detail(@AuthenticationPrincipal AuthUser authUser,
                                                          @PathVariable Long complaintId) {
        return ResponseEntity.ok(complaintQueryService.getDetail(authUser.userId(), complaintId));
    }

    // #9.민원 등록 점검 (얼굴 블러 + 담당 부서·지역 계산, DB에 민원 저장 안 함)
    @PostMapping("/preview")
    public ResponseEntity<ComplaintPreviewResponse> preview(@AuthenticationPrincipal AuthUser authUser,
                                                            @Valid @RequestBody ComplaintPreviewRequest request) {
        return ResponseEntity.ok(complaintCommandService.preview(authUser.userId(), request));
    }

    // #10.민원 처리 결과 미리보기 (AI 결과 이미지 생성)
    @PostMapping("/preview/result-image")
    public ResponseEntity<ResultImageResponse> resultImage(@AuthenticationPrincipal AuthUser authUser,
                                                           @Valid @RequestBody ResultImageRequest request) {
        return ResponseEntity.ok(complaintCommandService.createResultImage(authUser.userId(), request));
    }

    // #11.민원 등록
    @PostMapping
    public ResponseEntity<ComplaintDetailResponse> create(@AuthenticationPrincipal AuthUser authUser,
                                                          @Valid @RequestBody ComplaintCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintCommandService.create(authUser.userId(), request));
    }

    // #12.민원 공감
    @PostMapping("/{complaintId}/empathy")
    public ResponseEntity<EmpathyResponse> like(@AuthenticationPrincipal AuthUser authUser,
                                                @PathVariable Long complaintId) {
        return ResponseEntity.ok(empathyService.like(authUser.userId(), complaintId));
    }

    // #13.공감 취소
    @DeleteMapping("/{complaintId}/empathy")
    public ResponseEntity<EmpathyResponse> unlike(@AuthenticationPrincipal AuthUser authUser,
                                                  @PathVariable Long complaintId) {
        return ResponseEntity.ok(empathyService.unlike(authUser.userId(), complaintId));
    }

    // #17.민원 수정 (보낸 항목만 변경)
    @PatchMapping("/{complaintId}")
    public ResponseEntity<ComplaintDetailResponse> update(@AuthenticationPrincipal AuthUser authUser,
                                                          @PathVariable Long complaintId,
                                                          @Valid @RequestBody ComplaintUpdateRequest request) {
        return ResponseEntity.ok(complaintCommandService.update(authUser.userId(), complaintId, request));
    }

    // #18.민원 삭제
    @DeleteMapping("/{complaintId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthUser authUser,
                                       @PathVariable Long complaintId) {
        complaintCommandService.delete(authUser.userId(), complaintId);
        return ResponseEntity.noContent().build();
    }
}
