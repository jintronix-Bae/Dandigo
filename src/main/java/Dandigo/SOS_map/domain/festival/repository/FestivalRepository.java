package Dandigo.SOS_map.domain.festival.repository;

import Dandigo.SOS_map.domain.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    // 16번: 이미 시작한 축제(진행 중 + 종료)만, 최근 시작한 순
    List<Festival> findAllByStartDateLessThanEqualOrderByStartDateDesc(LocalDate today);
}
