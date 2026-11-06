package Dandigo.SOS_map.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// BaseTimeEntity의 createdAt, updatedAt을 자동으로 채우는 기능
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
