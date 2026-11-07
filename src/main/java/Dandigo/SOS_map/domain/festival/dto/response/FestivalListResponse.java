package Dandigo.SOS_map.domain.festival.dto.response;

import Dandigo.SOS_map.domain.festival.entity.Festival;

import java.time.LocalDate;
import java.util.List;

// 16번
public record FestivalListResponse(List<FestivalItem> festivals) {

    public record FestivalItem(Long festivalId, String name, String location,
                               LocalDate startDate, LocalDate endDate, boolean active) {

        public static FestivalItem of(Festival festival, LocalDate today) {
            return new FestivalItem(festival.getId(), festival.getName(), festival.getLocation(),
                    festival.getStartDate(), festival.getEndDate(), festival.isActiveOn(today));
        }
    }
}
