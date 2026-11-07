package Dandigo.SOS_map.domain.complaint.entity;

import Dandigo.SOS_map.domain.festival.entity.Festival;
import Dandigo.SOS_map.domain.image.entity.Image;
import Dandigo.SOS_map.domain.user.entity.Member;
import Dandigo.SOS_map.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "complaints",
        indexes = {
                // 6번 지도 범위 조회용
                @Index(name = "idx_complaints_lat_lng", columnList = "latitude, longitude"),
                @Index(name = "idx_complaints_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_complaints_image_id", columnNames = "image_id"),
                @UniqueConstraint(name = "uk_complaints_result_image_id", columnNames = "result_image_id")
        }
)
// 숨김 삭제(18번)된 민원은 모든 조회에서 제외한다.
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Complaint extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    // 블러된 사진
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    // 10번 AI 처리 결과 이미지 (건너뛰었으면 null)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_image_id")
    private Image resultImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ComplaintStatus status;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "area", nullable = false, length = 20)
    private String area;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    // 공감 수 캐시 (공감/취소 때 DB에서 직접 +1/-1)
    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "face_blur_confirmed", nullable = false)
    private boolean faceBlurConfirmed;

    @Column(name = "contains_animal_carcass", nullable = false)
    private boolean containsAnimalCarcass;

    @Column(name = "animal_blur_confirmed", nullable = false)
    private boolean animalBlurConfirmed;

    // AI가 추천한 카테고리 (현재 기획에서는 사용하지 않아 null)
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_category", length = 20)
    private ComplaintCategory aiCategory;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Complaint(Member user, Image image, Image resultImage, Festival festival, ComplaintCategory category,
                      String title, String description, String address, String area,
                      double latitude, double longitude,
                      boolean faceBlurConfirmed, boolean containsAnimalCarcass, boolean animalBlurConfirmed) {
        this.user = user;
        this.image = image;
        this.resultImage = resultImage;
        this.festival = festival;
        this.category = category;
        this.status = ComplaintStatus.RECEIVED;
        this.title = title;
        this.description = description;
        this.address = address;
        this.area = area;
        this.latitude = toCoordinate(latitude);
        this.longitude = toCoordinate(longitude);
        this.likeCount = 0;
        this.faceBlurConfirmed = faceBlurConfirmed;
        this.containsAnimalCarcass = containsAnimalCarcass;
        this.animalBlurConfirmed = animalBlurConfirmed;
    }

    public boolean isWrittenBy(Long userId) {
        return user.getId().equals(userId);
    }

    // 접수 상태일 때만 수정·삭제 가능 (17·18번)
    public boolean isModifiable() {
        return status == ComplaintStatus.RECEIVED;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeCategory(ComplaintCategory category) {
        this.category = category;
    }

    public void changeFestival(Festival festival) {
        this.festival = festival;
    }

    public void changeLocation(String address, String area, double latitude, double longitude) {
        this.address = address;
        this.area = area;
        this.latitude = toCoordinate(latitude);
        this.longitude = toCoordinate(longitude);
    }

    public void changeImage(Image image) {
        this.image = image;
    }

    public void changeResultImage(Image resultImage) {
        this.resultImage = resultImage;
    }

    public void changePrivacyCheck(boolean faceBlurConfirmed, boolean containsAnimalCarcass, boolean animalBlurConfirmed) {
        this.faceBlurConfirmed = faceBlurConfirmed;
        this.containsAnimalCarcass = containsAnimalCarcass;
        this.animalBlurConfirmed = animalBlurConfirmed;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    private static BigDecimal toCoordinate(double value) {
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }
}
