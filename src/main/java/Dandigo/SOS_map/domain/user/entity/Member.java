package Dandigo.SOS_map.domain.user.entity;

import Dandigo.SOS_map.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    private Member(String loginId, String name, String email, String password, Role role) {
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 일반 회원가입(기본 권한)
    public static Member register(String loginId, String name, String email, String password) {
        return new Member(loginId, name, email, password, Role.ROLE_USER);
    }

}
