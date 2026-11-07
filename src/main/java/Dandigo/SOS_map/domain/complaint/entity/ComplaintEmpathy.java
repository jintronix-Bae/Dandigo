package Dandigo.SOS_map.domain.complaint.entity;

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
@Table(
        name = "complaint_empathies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_empathies_complaint_user", columnNames = {"complaint_id", "user_id"})
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComplaintEmpathy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empathy_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ComplaintEmpathy(Complaint complaint, Member user) {
        this.complaint = complaint;
        this.user = user;
    }

    public static ComplaintEmpathy of(Complaint complaint, Member user) {
        return new ComplaintEmpathy(complaint, user);
    }
}
