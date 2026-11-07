package Dandigo.SOS_map.global.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

// 날짜 응답 형식 통일: "2026-11-02T14:30:00+09:00" (공통 규칙 2번)
public final class DateTimes {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private DateTimes() {
    }

    public static OffsetDateTime toOffset(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(SEOUL).toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDate today() {
        return LocalDate.now(SEOUL);
    }
}
