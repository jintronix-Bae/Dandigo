package Dandigo.SOS_map.domain.image.entity;

import Dandigo.SOS_map.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "images")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private Member uploader;

    // 블러/결과 이미지가 어떤 사진에서 만들어졌는지 (원본은 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_image_id")
    private Image sourceImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ImageType type;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ImageStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Image(Member uploader, Image sourceImage, ImageType type, String url, String thumbnailUrl,
                  String contentType, long sizeBytes) {
        this.uploader = uploader;
        this.sourceImage = sourceImage;
        this.type = type;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = ImageStatus.TEMP;
    }

    public static Image create(Member uploader, Image sourceImage, ImageType type, String url, String thumbnailUrl,
                               String contentType, long sizeBytes) {
        return new Image(uploader, sourceImage, type, url, thumbnailUrl, contentType, sizeBytes);
    }

    public boolean isOwnedBy(Long userId) {
        return uploader.getId().equals(userId);
    }

    public boolean isAttached() {
        return status == ImageStatus.ATTACHED;
    }

    public void attach() {
        this.status = ImageStatus.ATTACHED;
    }

    public void detach() {
        this.status = ImageStatus.TEMP;
    }

    public void clearSource() {
        this.sourceImage = null;
    }
}
