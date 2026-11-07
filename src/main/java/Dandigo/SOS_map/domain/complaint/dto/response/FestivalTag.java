package Dandigo.SOS_map.domain.complaint.dto.response;

import Dandigo.SOS_map.domain.festival.entity.Festival;

// 민원 응답 안의 festival: { "festivalId": 1, "name": "대구 야간 페스타" } 또는 null
public record FestivalTag(Long festivalId, String name) {

    public static FestivalTag from(Festival festival) {
        return festival == null ? null : new FestivalTag(festival.getId(), festival.getName());
    }
}
