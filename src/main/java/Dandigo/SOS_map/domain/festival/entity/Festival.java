package Dandigo.SOS_map.domain.festival.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private Festival(String name, String location, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Festival create(String name, String location, LocalDate startDate, LocalDate endDate) {
        return new Festival(name, location, startDate, endDate);
    }

    // 진행 중 여부: 시작일 ~ 종료일 사이(양 끝 포함)
    public boolean isActiveOn(LocalDate today) {
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}
