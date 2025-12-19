# StepByStep Backend (BE)

청소년 성교육 앱 **성큼성큼(Step-By-Step)** 의 Backend 서버입니다.  
Spring Boot 기반 REST API 서버로, 인증/인가, 사용자 관리, 퀴즈, 게시판, AI 연동을 담당합니다.

---

## 1. Source Code Description

### Tech Stack
- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- MySQL
- Gradle

### Main Features
- JWT 기반 인증/인가
- 사용자/프로필 관리
- AI(RAG) 서버 연동 API
- 퀴즈 및 시나리오 관리
- 포인트/배지 시스템
- 게시판 API

---

## 2. Repository Structure
```text
StepByStep_BE/
├─ README.md
├─ build.gradle
├─ settings.gradle
├─ gradlew
├─ gradlew.bat
├─ .github/
├─ gradle/             # Gradle wrapper 설정
├─ build/              # 빌드 산출물 (로컬/CI 빌드 결과로 자동 생성)
└─ src/
   ├─ main/
   │  ├─ java/com/dragon/stepbystep/
   │  │  ├─ config       # 전역 설정(Bean 등록, CORS, Swagger/Docs, Web 설정 등)
   │  │  ├─ security     # Spring Security/JWT 인증·인가, 필터, 시큐리티 설정
   │  │  ├─ controller   # REST API 엔드포인트(요청/응답), 입력 검증 및 라우팅
   │  │  ├─ service      # 비즈니스 로직(트랜잭션 단위 처리), 도메인 규칙 구현
   │  │  ├─ repository   # DB 접근 계층(JPA Repository), 영속성 관련 쿼리
   │  │  ├─ domain       # 엔티티/도메인 모델(테이블 매핑, 핵심 상태/관계)
   │  │  ├─ dto          # 요청/응답 DTO, 계층 간 데이터 전달용 객체
   │  │  ├─ exception    # 예외 정의 및 전역 예외 처리(@ControllerAdvice 등)
   │  │  ├─ ai           # AI(RAG/FastAPI 등) 연동 모듈(클라이언트, 요청/응답 모델)
   │  │  └─ common       # 공통 유틸/상수/응답 포맷/공용 컴포넌트
   │  └─ resources/
   │     └─ application.properties  # Spring 설정(프로필/DB/메일/로그 등, env 주입)
   └─ test/
      └─ java/com/dragon/stepbystep/
         └─ StepByStepBeApplicationTests.java  # 기본 애플리케이션 컨텍스트 테스트
```

---

## 3. How to Install

1. 깃허브 Repository 클론
```bash
git clone https://github.com/EwhaCapstone-2025-Team27/StepByStep_BE.git
```

2. BE 폴더로 이동
```bash
cd StepByStep_BE
```

3. 프로젝트 최상위 디렉토리에 .env 파일 생성
```bash
# JWT secret key
JWT_SECRET_KEY=a7eqx/zHFUfabeIU+XSeOwmssPNQ3WXmpBswTCQY7ypW8GNZmb5xQqjSpkfsgiw6DRQJhnGnj+Nd7gYYw9z4lw==

# Server Port (EC2 실행)
SERVER_PORT=8080

# DB (개발용은 RDS에 연결된 DB)
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

4. 

---

## 4. How to Run
```bash
java -jar build/libs/*.jar --spring.profiles.active=prod
```

---

## 5. Sample Data
테스트 계정:
- ID:test00@abc.com
- PW:12345678!

> 테스트 계정으로 사용 시, 이메일 찾기와 비밀번호 찾기는 할 수 없습니다.

---

## 6. Runtime Information
- Backend API URL: https://api.seongkeum.com
- Test Account 제공 (위 참조)

## 7. Used Open Source
- Spring Boot (Apache 2.0)
- Spring Security (Apache 2.0)
- Hibernate (LGPL)
- JJWT (Apache 2.0)
- MySQL Connector
