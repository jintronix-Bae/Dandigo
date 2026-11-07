package Dandigo.SOS_map.global.infra.gemini;

import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

// Gemini 이미지 모델로 "문제가 해결된 모습" 예시 이미지를 만든다 (10번)
// REST: POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
@Slf4j
@Component
public class GeminiImageClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiImageClient(@Value("${gemini.api-key:}") String apiKey,
                             @Value("${gemini.image-model}") String model,
                             @Value("${gemini.timeout-seconds:60}") long timeoutSeconds) {
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public GeneratedImage editImage(String prompt, byte[] imageBytes, String mimeType) {
        if (!StringUtils.hasText(apiKey)) {
            log.error("GEMINI_API_KEY 가 설정되지 않았습니다.");
            throw new CustomException(ErrorCode.AI_UNAVAILABLE);
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                        Map.of("text", prompt),
                        Map.of("inlineData", Map.of(
                                "mimeType", mimeType,
                                "data", Base64.getEncoder().encodeToString(imageBytes)))
                ))),
                "generationConfig", Map.of("responseModalities", List.of("TEXT", "IMAGE"))
        );

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            // 시간 초과(60초), 키 오류, 사용량 초과 등
            log.error("Gemini 이미지 생성 호출 실패 model={}", model, e);
            throw new CustomException(ErrorCode.AI_UNAVAILABLE);
        }

        return extractImage(response);
    }

    // 응답의 candidates[0].content.parts 중 이미지가 담긴 part를 찾는다
    private GeneratedImage extractImage(JsonNode response) {
        if (response != null) {
            for (JsonNode part : response.path("candidates").path(0).path("content").path("parts")) {
                JsonNode inline = part.has("inlineData") ? part.path("inlineData") : part.path("inline_data");
                String data = inline.path("data").asString("");
                if (!data.isEmpty()) {
                    String mimeType = inline.has("mimeType")
                            ? inline.path("mimeType").asString("")
                            : inline.path("mime_type").asString("");
                    return new GeneratedImage(Base64.getDecoder().decode(data), mimeType);
                }
            }
        }
        // 안전 정책으로 거절되었거나 텍스트만 돌아온 경우
        log.error("Gemini 응답에 이미지가 없습니다: {}", response);
        throw new CustomException(ErrorCode.AI_UNAVAILABLE);
    }

    public record GeneratedImage(byte[] bytes, String mimeType) {
    }
}
