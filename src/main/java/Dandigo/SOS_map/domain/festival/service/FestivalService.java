package Dandigo.SOS_map.domain.festival.service;

import Dandigo.SOS_map.domain.festival.dto.response.FestivalListResponse;
import Dandigo.SOS_map.domain.festival.repository.FestivalRepository;
import Dandigo.SOS_map.global.common.DateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// 축제 목록 (16번). 아직 시작하지 않은 축제는 빼고, active는 오늘(한국 시간) 기준으로 계산한다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public FestivalListResponse getFestivals() {
        LocalDate today = DateTimes.today();
        return new FestivalListResponse(festivalRepository.findAllByStartDateLessThanEqualOrderByStartDateDesc(today).stream()
                .map(festival -> FestivalListResponse.FestivalItem.of(festival, today))
                .toList());
    }
}
