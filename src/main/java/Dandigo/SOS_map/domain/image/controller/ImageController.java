package Dandigo.SOS_map.domain.image.controller;

import Dandigo.SOS_map.domain.image.dto.response.ImageUploadResponse;
import Dandigo.SOS_map.domain.image.service.ImageService;
import Dandigo.SOS_map.global.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 민원 사진 API (14, 15번)
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // #14.민원 사진 업로드 (multipart/form-data, 파일 필드 이름: image)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(@AuthenticationPrincipal AuthUser authUser,
                                                      @RequestPart("image") MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imageService.upload(authUser.userId(), image));
    }

    // #15.민원 사진 삭제
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long imageId) {
        imageService.delete(authUser.userId(), imageId);
        return ResponseEntity.noContent().build();
    }
}
