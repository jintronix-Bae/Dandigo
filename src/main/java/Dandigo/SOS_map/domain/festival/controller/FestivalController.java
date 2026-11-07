package Dandigo.SOS_map.domain.festival.controller;

import Dandigo.SOS_map.domain.festival.dto.response.FestivalListResponse;
import Dandigo.SOS_map.domain.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 축제 API (16번)
@RestController
@RequestMapping("/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    // #16.축제 목록 조회 (진행 중/종료 여부 포함)
    @GetMapping
    public ResponseEntity<FestivalListResponse> list() {
        return ResponseEntity.ok(festivalService.getFestivals());
    }
}
