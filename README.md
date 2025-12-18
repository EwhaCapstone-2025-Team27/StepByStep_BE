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
├─ build/              # 빌드 산출물
└─ src/
   ├─ main/
   │  ├─ java/com/dragon/stepbystep/
   │  │  ├─ config
   │  │  ├─ security
   │  │  ├─ controller
   │  │  ├─ service
   │  │  ├─ repository
   │  │  ├─ domain
   │  │  ├─ dto
   │  │  ├─ exception
   │  │  ├─ ai
   │  │  └─ common
   │  └─ resources/
   │     └─ application.properties
   └─ test/
      └─ java/com/dragon/stepbystep/
         └─ StepByStepBeApplicationTests.java

```

---

## 3. How to Build

```bash
cd StepByStep_BE
./gradlew clean build
```

빌드 결과:
```bash
build/libs/stepbystep-0.0.1-SNAPSHOT.jar
```

---

## 4. How to Install

### 4.1 Requirements
- Java 17+
- MySQL 8.x
- Linux / macOS / Windows(WSL 권장)

### 4.2 Environment Variables
`env` 또는 systemd override로 설정
```ini
# JWT secret key
JWT_SECRET_KEY=IxnW:H@Mr^Uo96ONfV}%wAk$6|}&>-d%Cl<cttb7JrR=6]Rs0~pC3z!~6G-c(&K

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

---

## 5. How to Run
```bash
java -jar build/libs/*.jar --spring.profiles.active=prod
```

---

## 6. How to Test
```bash
./gradlew test
```

---

## 7. Sample Data
테스트 계정:
- ID:
- PW:

---

## 8. Runtime Information
- Backend API URL: https://api.seongkeum.com
- Test Account 제공 (위 참조)

## 9. Used Open Source
- Spring Boot (Apache 2.0)
- Spring Security (Apache 2.0)
- Hibernate (LGPL)
- JJWT (Apache 2.0)
- MySQL Connector/J
