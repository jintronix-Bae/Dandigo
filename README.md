# 동네SOS — Backend

대구·경북 생활 불편 공유 지도 서비스 **동네SOS**의 REST API 서버입니다.
회원 인증, 민원 등록·조회, 사진 저장과 얼굴 블러, AI 예시 이미지 생성, 지역 판별, 축제 데이터를 담당합니다.

- API 문서: 서버 실행 후 `/swagger-ui.html` 에서 확인

<br>

## 백엔드 주요 구현

| 구현 | 설명 |
| --- | --- |
| JWT 인증 | Spring Security + JWT, 회원가입·로그인·중복 확인 |
| 지도 범위 조회 | 위도·경도 범위 쿼리로 화면 안 민원을 최신순 조회 (최대 200개) |
| 사진 저장 | S3 업로드, jpg·png만 허용, 최대 10MB, EXIF 회전 보정 |
| 얼굴 블러 | AWS Rekognition으로 얼굴 위치 감지 → 서버에서 모자이크 → 블러 사진 별도 저장 |
| 원본 자동 삭제 | 민원 등록 후 얼굴이 담긴 원본 사진은 커밋 이후 S3에서 삭제 |
| AI 예시 이미지 | Gemini 이미지 모델로 "해결된 모습" 예시 생성 (타임아웃 2분, 실패 시 502) |
| 지역 판별 | 좌표 → 카카오 로컬 API → 9개 생활권, 실패 시 주소 문자열로 판별 |
| 담당 부서 안내 | 카테고리별 담당 부서·예상 처리 기간 계산 |
| 공감 | 사용자당 1회, 공감 수 원자적 증가·감소 |
| 소프트 삭제 | 민원 삭제 시 `deleted_at` 기록, 조회에서 자동 제외 |
| 축제 데이터 | 한국관광공사 TourAPI 기반 대구 축제 목록, 진행 중 여부 계산 |

<br>

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language / Framework | Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, Validation |
| Database | MySQL 8.4 |
| Storage / AI | AWS S3, AWS Rekognition, Google Gemini (이미지 생성) |
| 외부 API | 카카오 로컬 API, 한국관광공사 TourAPI |
| Infra | AWS EC2, Docker Compose, Caddy (HTTPS, Let's Encrypt) |
| CI/CD | GitHub Actions → Docker Hub → EC2 |
| Docs | springdoc-openapi (Swagger UI) |

<br>

## 시스템 구조

```
사용자 앱 (Expo)
     │  HTTPS
     ▼
┌──────────────── AWS EC2 (Docker Compose) ────────────────┐
│  Caddy (HTTPS, 인증서 자동)  →  Spring Boot  →  MySQL     │
└──────────────────────────────┬───────────────────────────┘
                               │
     ┌──────────────┬──────────┼───────────┬──────────────┐
     ▼              ▼          ▼           ▼              ▼
  AWS S3      Rekognition    Gemini    카카오 로컬     TourAPI
 (사진 저장)   (얼굴 감지)   (예시 이미지) (좌표→지역)   (축제 데이터)
```

<br>

## API 목록

로그인 이후 요청은 헤더에 `Authorization: Bearer {accessToken}` 이 필요합니다. (1~4번 제외)

| No | Method | URL | 설명 |
| --- | --- | --- | --- |
| 1 | POST | `/users/signup` | 회원가입 |
| 2 | GET | `/users/check-email` | 이메일 중복 확인 |
| 3 | GET | `/users/check-login-id` | 아이디 중복 확인 |
| 4 | POST | `/users/login` | 로그인 (JWT 발급) |
| 5 | GET | `/users/mypage` | 마이페이지 (내 정보·활동 통계·최근 민원) |
| 6 | GET | `/complaints/nearby` | 지도 범위 안의 민원 조회 |
| 7 | GET | `/complaints` | 민원 목록 (지역·카테고리·축제·상태 필터, 페이지) |
| 8 | GET | `/complaints/{complaintId}` | 민원 상세 |
| 9 | POST | `/complaints/preview` | 민원 등록 점검 (얼굴 블러, 담당 부서·지역 계산) |
| 10 | POST | `/complaints/preview/result-image` | AI 처리 결과 미리보기 이미지 생성 |
| 11 | POST | `/complaints` | 민원 등록 |
| 12 | POST | `/complaints/{complaintId}/empathy` | 공감 |
| 13 | DELETE | `/complaints/{complaintId}/empathy` | 공감 취소 |
| 14 | POST | `/images` | 사진 업로드 (jpg·png, 최대 10MB) |
| 15 | DELETE | `/images/{imageId}` | 사진 삭제 |
| 16 | GET | `/festivals` | 축제 목록 (진행 중 여부 포함) |
| 17 | PATCH | `/complaints/{complaintId}` | 민원 수정 |
| 18 | DELETE | `/complaints/{complaintId}` | 민원 삭제 |

### 에러 응답 형식 (공통)

```json
{
  "status": 400,
  "code": "INVALID_INPUT",
  "message": "비밀번호는 영문과 숫자를 포함해 8자 이상 입력해 주세요.",
  "errors": [
    { "field": "password", "reason": "비밀번호는 영문과 숫자를 포함해 8자 이상 입력해 주세요." }
  ]
}
```

<br>

## 민원 등록 흐름

```
14. 사진 업로드 → 9. 등록 점검(얼굴 블러) → 10. AI 결과 미리보기(선택) → 11. 민원 등록
```

- 9번에서 만든 **블러 사진**만 민원에 연결되고, 얼굴이 담긴 **원본 사진은 등록 후 자동 삭제**됩니다.
- 통과 여부는 사용자가 점검 화면에서 직접 판단합니다. (블러가 어색하면 사진을 다시 올려 9번 재호출)

<br>

## 프로젝트 구조

```
src/main/java/Dandigo/SOS_map
├── domain
│   ├── user        # 회원가입, 로그인, 마이페이지
│   ├── complaint   # 민원 조회·등록·수정·삭제, 공감, 지역 판별
│   ├── image       # 사진 업로드·삭제
│   └── festival    # 축제 목록
└── global
    ├── config      # Security, CORS, AWS, JPA 설정
    ├── security    # JWT 인증
    ├── error       # 공통 에러 코드·응답
    ├── common      # 공통 엔티티, 날짜 처리
    └── infra       # S3, Rekognition, Gemini, 카카오, 이미지 처리
```

<br>

## 로컬 실행

### 1. 환경변수 (`.env`)

프로젝트 최상위에 `.env` 파일을 만들고 아래 값을 채웁니다. **`.env`는 Git에 올리지 않습니다.**

| 변수 | 설명 |
| --- | --- |
| `DB_URL` | MySQL 접속 주소 (JDBC URL) |
| `DB_USERNAME`, `DB_PW` | MySQL 계정 |
| `JWT_SECRET` | JWT 서명 키 |
| `JWT_EXPIRATION` | 토큰 유효 시간 (ms) |
| `AWS_REGION` | AWS 리전 |
| `AWS_S3_BUCKET` | 사진 저장 버킷 이름 |
| `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` | AWS 접근 키 |
| `GEMINI_API_KEY` | Google Gemini API 키 |
| `GEMINI_IMAGE_MODEL` | 사용할 Gemini 이미지 모델 |
| `KAKAO_REST_KEY` | 카카오 로컬 REST 키 (비우면 주소 문자열로 지역 판단) |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트 주소 |
| `JPA_DDL_AUTO` | 기본 `update` (로컬 초기화가 필요하면 `create`) |

### 2. 실행

```bash
./gradlew bootRun
```

### 3. Docker로 실행 (Spring + MySQL + Caddy)

```bash
docker compose up -d --build
```

<br>

## 배포 (CI/CD)

| 워크플로 | 시점 | 내용 |
| --- | --- | --- |
| `ci.yml` | PR, develop 푸시 | 빌드 + 테스트 |
| `deploy.yml` | main 푸시 | 테스트 → Docker 이미지 빌드 → 서버 배포 |

배포에 필요한 키와 환경변수는 GitHub Secrets로 관리합니다.

### 축제 데이터

한국관광공사 TourAPI 기반 대구 축제 데이터는 `sql/festivals.sql` 에 있습니다. 여러 번 실행해도 중복되지 않습니다.
