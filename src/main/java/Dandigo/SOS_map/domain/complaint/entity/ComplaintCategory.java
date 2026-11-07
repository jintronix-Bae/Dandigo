package Dandigo.SOS_map.domain.complaint.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 공통 규칙 "2. 코드값" 카테고리 표 + 10번 결과 이미지 문구/프롬프트
@Getter
@RequiredArgsConstructor
public enum ComplaintCategory {
    ROAD("도로 파손", "도로관리과", "3~5일",
            "파손된 도로를 보수한 모습이에요.",
            "Repair the damaged road or sidewalk so the pavement looks new, flat and even."),
    TRASH("쓰레기", "청소행정과", "1~2일",
            "쓰레기를 깨끗하게 치운 모습이에요.",
            "Remove all the trash and litter so the area looks clean and tidy."),
    STREETLIGHT("가로등", "도로관리과", "2~4일",
            "가로등을 고쳐 다시 불이 켜진 모습이에요.",
            "Fix the broken streetlight so it looks repaired and is lit up."),
    PARKING("불법 주정차", "교통지도과", "현장 확인 후 조치",
            "불법 주정차 차량이 정리된 모습이에요.",
            "Remove the illegally parked vehicles so the road and sidewalk are clear."),
    NOISE("소음", "환경관리과", "2~3일",
            "소음 원인을 정리해 조용해진 모습이에요.",
            "Remove the source of the noise problem (such as loudspeakers or construction equipment) so the scene looks calm and orderly."),
    ANIMAL("동물 사체", "동물보호 담당 부서", "당일 확인",
            "동물 사체를 수습해 깨끗해진 모습이에요.",
            "Remove the dead animal completely so the ground looks clean."),
    FESTIVAL("축제 불편", "축제 운영센터", "당일~1일",
            "축제장 불편 사항을 정리한 모습이에요.",
            "Resolve the festival inconvenience shown so the festival area looks clean, safe and well organized."),
    OTHER("기타", "민원 담당 부서", "담당 부서 확인 후 안내",
            "불편 사항을 해결한 모습이에요.",
            "Fix the problem shown so the scene looks clean, safe and well maintained.");

    private final String displayName;
    private final String agency;
    private final String estimatedPeriod;
    private final String resultDescription;   // 10번 응답 description (카테고리별 고정 문구)
    private final String fixInstruction;      // 10번 Gemini 프롬프트용 지시문
}
