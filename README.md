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
DB_URL=jdbc:mysql://localhost:3306/stepbystep
DB_USERNAME=test
DB_PASSWORD=test

JWT_SECRET=your_jwt_secret_key

AI_BASE_URL=http://localhost:8000

MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=test@example.com
MAIL_PASSWORD=password
MAIL_FROM=no-reply@example.com
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
