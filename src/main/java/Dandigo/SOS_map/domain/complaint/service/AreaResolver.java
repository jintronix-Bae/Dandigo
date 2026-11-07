package Dandigo.SOS_map.domain.complaint.service;

import Dandigo.SOS_map.global.infra.kakao.KakaoRegionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// 지역(area) 판단: 동성로, 북구, 서구, 중구, 남구, 수성구, 달서구, 달성군, 기타
// 1순위: 위도·경도 → 카카오 행정구역 / 2순위(키 없음·호출 실패): 주소 문자열
@Component
@RequiredArgsConstructor
public class AreaResolver {

    public static final String DONGSEONGRO = "동성로";
    public static final String OTHER = "기타";

    private static final String DAEGU = "대구";
    private static final List<String> DISTRICTS = List.of("북구", "서구", "중구", "남구", "수성구", "달서구", "달성군");

    private final KakaoRegionClient kakaoRegionClient;

    public String resolve(double latitude, double longitude, String address) {
        return kakaoRegionClient.findRegion(latitude, longitude)
                .map(this::fromRegion)
                .orElseGet(() -> fromAddress(address));
    }

    // 예) 대구광역시 / 중구 / 동성로2가 → 동성로, 대구광역시 / 북구 / 산격동 → 북구
    String fromRegion(KakaoRegionClient.Region region) {
        if (!region.depth1().startsWith(DAEGU)) {
            return OTHER;
        }
        if (region.depth3().startsWith(DONGSEONGRO)) {   // 법정동 동성로1가~3가 (중구)
            return DONGSEONGRO;
        }
        return DISTRICTS.contains(region.depth2()) ? region.depth2() : OTHER;
    }

    // 예) "대구 중구 동성로 12-3" → 동성로 / "대구 북구 대학로 80" → 북구
    String fromAddress(String address) {
        if (address == null) {
            return OTHER;
        }
        String[] tokens = address.trim().split("\\s+");

        // 동성로는 중구 안의 거리라서 구보다 먼저 확인한다 ("동성로2가" 같은 동 이름 포함)
        for (String token : tokens) {
            if (token.startsWith(DONGSEONGRO)) {
                return DONGSEONGRO;
            }
        }
        // 띄어쓰기 단위로 정확히 비교한다 ("달서구" 안의 "서구"를 잘못 잡지 않도록)
        for (String token : tokens) {
            if (DISTRICTS.contains(token)) {
                return token;
            }
        }
        return OTHER;
    }
}
