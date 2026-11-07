package Dandigo.SOS_map.domain.festival.service;

import Dandigo.SOS_map.domain.festival.entity.Festival;
import Dandigo.SOS_map.domain.festival.repository.FestivalRepository;
import Dandigo.SOS_map.global.common.DateTimes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// 개발용 축제 더미 데이터. 축제 테이블이 비어 있을 때만 넣는다.
// TODO: 배포 시 관광공사 TourAPI 데이터로 교체하고 이 클래스는 삭제한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class FestivalDummyDataLoader implements ApplicationRunner {

    private final FestivalRepository festivalRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (festivalRepository.count() > 0) {
            return;
        }
        LocalDate today = DateTimes.today();
        festivalRepository.saveAll(List.of(
                // 진행 중
                Festival.create("대구 야간 페스타", "국채보상운동기념공원", today.minusDays(1), today.plusDays(2)),
                // 종료
                Festival.create("수성못 가을 음악회", "수성못 상화동산", today.minusDays(35), today.minusDays(34)),
                // 예정
                Festival.create("동성로 거리 축제", "동성로 일대", today.plusDays(10), today.plusDays(12))
        ));
        log.info("축제 더미 데이터 3건을 넣었습니다.");
    }
}
