# 성큼성큼 - Backend

해당 리포지토리는 청소년 성교육 앱 **성큼성큼**의 백엔드 서버 코드를 포함하고 있습니다.</p>
본 서버는 Spring Boot 기반 REST API로 구성되어 있으며,</br>
사용자 인증/인가, 시나리오 퀴즈 학습, 익명 게시판, 포인트·배지 시스템 및 AI(RAG) 서버 연동을 담당합니다.

---

## 📁 Source Code 설명
본 프로젝트의 주요 디렉토리 구조는 다음과 같습니다:
```text
StepByStep_BE/
├── src/
│   ├── main/
│   │   ├── java/com/dragon/stepbystep/
│   │   │   ├── config/        # 전역 설정 (CORS, Web, Swagger, Bean 설정 등)
│   │   │   ├── security/      # Spring Security, JWT 인증/인가, 필터
│   │   │   ├── controller/    # REST API 엔드포인트 (요청/응답 처리)
│   │   │   ├── service/       # 비즈니스 로직 계층 (트랜잭션 단위 처리)
│   │   │   ├── repository/    # JPA Repository (DB 접근)
│   │   │   ├── domain/        # 엔티티 및 도메인 모델
│   │   │   ├── dto/           # 요청/응답 DTO
│   │   │   ├── exception/     # 커스텀 예외 및 전역 예외 처리
│   │   │   ├── ai/            # AI(RAG/FastAPI) 서버 연동 모듈
│   │   │   └── common/        # 공통 응답 포맷, 상수, 유틸 클래스
│   │   └── resources/
│   │       └── application.properties  # Spring 환경 설정
│   └── test/
│       └── java/com/dragon/stepbystep/
│           └── StepByStepBeApplicationTests.java
├── build.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 🛠️ How to Build

**1. GitHub 저장소 클론**
```bash
git clone https://github.com/EwhaCapstone-2025-Team27/StepByStep_BE.git
cd StepByStep_BE
```

**2. 환경 설정 파일 (.env) 준비**
프로젝트 루트 경로에 .env 파일을 생성하고 아래 환경변수를 설정합니다.
```text
# JWT secret key
JWT_SECRET_KEY=<본인 JWT SECRET KEY>

# Server Port (EC2 실행)
SERVER_PORT=8080

# DB (RDS에 연결된 DB)
DB_URL=jdbc:mysql://stepbystep-public-rds.chog8wcgurb6.ap-northeast-2.rds.amazonaws.com:3306/stepbystep?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul&sslMode=REQUIRED
DB_USERNAME=admin
DB_PASSWORD=adminadmin
DB_DRIVER=com.mysql.cj.jdbc.Driver

# Mail
MAIL_HOST=email-smtp.ap-northeast-2.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=AKIAVA5YLBOFLY6DNIVN
MAIL_PASSWORD=BBBKgvw+p2rqA6VCea7k/ky0pyCoEWTZtPdQr+qlKtcp
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
MAIL_FROM=no-reply@seongkeum.com
MAIL_TEMP_PASSWORD_SUBJECT="성큼성큼 임시 비밀번호 안내"

# Temp Password
TEMP_PASSWORD_EXPIRATION_MINUTES=30
TEMP_PASSWORD_LENGTH=12

# AI 연결
AI_BASE_URL=http://127.0.0.1:8000
```

**3. 빌드**
```bash
./gradlew clean build
```
* Gradle Wrapper를 사용하여 의존성을 자동 설치합니다.
* `build/libs/` 디렉토리에 실행 가능한 `.jar` 파일이 생성됩니다.

---

## 🚀 How to Install & Run

**1. Gradle로 실행 (로컬)**
```bash
./gradlew bootRun
```

**2. JAR 파일 실행**
```bash
java -jar build/libs/stepbystep-0.0.1-SNAPSHOT.jar
```

---

## 📮 How to Test
**Postman을 이용한 API 테스트**
본 서버는 JWT 기반 인증을 사용합니다. </br>
로그인 API 호출 후 발급받은 토큰을 모든 요청에 포함해야 합니다.
```bash
Authorization: Bearer {ACCESS_TOKEN}
Content-Type: application/json
```

### 🧪 주요 API 테스트 예시

**1. 로그인**
* `POST /api/auth/login`: 기존에 있는 테스트 계정을 사용합니다.
```json
{
  "email": "test00@abc.com",
  "password": "12345678!"
}
```
응답으로 나온 accessToken을 이후 요청에 사용합니다.

**2. AI 챗봇 상답**
* `POST /api/chat`
```json
{
  "message": "질문",
  "userId": "string",
  "top_k": 8,
  "enable_bm25": true,
  "enable_rrf": true
}
```

---

## 📦 사용한 Open Source
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)
- JJWT
- MySQL Connector/J
- Lombok

---

## 🔗 외부 서비스 연동
- **AI RAG Server (FastAPI)** </br>
성교육 문서 기반 질의응답을 제공하기 위해 별도의 AI 서버와 HTTP 통신 방식으로 연동됩니다.
