package Dandigo.SOS_map.global.infra.kakao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

// 카카오 로컬 API: 좌표 → 행정구역 (coord2regioncode)
// GET https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x={경도}&y={위도}
@Slf4j
@Component
public class KakaoRegionClient {

    private static final String BASE_URL = "https://dapi.kakao.com";

    private final RestClient restClient;
    private final String restKey;

    public KakaoRegionClient(@Value("${kakao.rest-key:}") String restKey) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder().baseUrl(BASE_URL).requestFactory(requestFactory).build();
        this.restKey = restKey;
    }

    // 법정동 기준 행정구역. 키가 없거나 호출에 실패하면 empty (호출한 쪽에서 주소 문자열로 대신 판단)
    public Optional<Region> findRegion(double latitude, double longitude) {
        if (!StringUtils.hasText(restKey)) {
            return Optional.empty();
        }
        try {
            JsonNode response = restClient.get()
                    .uri(uri -> uri.path("/v2/local/geo/coord2regioncode.json")
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .build())
                    .header("Authorization", "KakaoAK " + restKey)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                return Optional.empty();
            }
            for (JsonNode document : response.path("documents")) {
                if ("B".equals(document.path("region_type").asString(""))) {   // B: 법정동, H: 행정동
                    return Optional.of(new Region(
                            document.path("region_1depth_name").asString(""),
                            document.path("region_2depth_name").asString(""),
                            document.path("region_3depth_name").asString("")));
                }
            }
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("카카오 좌표→행정구역 조회 실패 lat={}, lng={}", latitude, longitude, e);
            return Optional.empty();
        }
    }

    // 예) 대구광역시 / 중구 / 동성로2가
    public record Region(String depth1, String depth2, String depth3) {
    }
}
