package Dandigo.SOS_map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SosMapApplication {

	public static void main(String[] args) {
		// 서버(EC2 등)가 UTC여도 createdAt 등이 한국 시간(+09:00)으로 저장·응답되도록 고정한다.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(SosMapApplication.class, args);
	}

}
