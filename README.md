# Rag Virus

청년 정책 추천 서비스 구현 작업용 프로젝트입니다.

## 구성

- `backend`: Spring Boot, JPA, Flyway, PostgreSQL
- `frontend`: React, Vite
- `docs/youth-policy-recommendation-plan.md`: 마일스톤 기반 구현 계획

## 환경변수

```text
DB_URL=jdbc:postgresql://localhost:5432/ragvirus
DB_USERNAME=ragvirus
DB_PASSWORD=ragvirus
PUBLIC_DATA_SERVICE_KEY=공공데이터_인증키
```

## 로컬 DB

Docker Desktop 실행 후:

```bash
docker compose up -d postgres
```

## 백엔드

로컬 Gradle이 없으면 작업 중 사용한 Gradle 배포본이 `.tools`에 있습니다. `.tools`는 `.gitignore` 대상입니다.

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot'
.\.tools\gradle-8.10.2\bin\gradle.bat :backend:bootRun
```

검증:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot'
.\.tools\gradle-8.10.2\bin\gradle.bat test
```

## 프론트엔드

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

기본 API 주소는 `http://localhost:8080`입니다. 변경하려면 `VITE_API_BASE`를 설정합니다.

## 현재 구현된 API

정책 목록:

```http
GET /api/policies?keyword=월세&category=주거&applicationStatus=open
```

정책 상세:

```http
GET /api/policies/{policyId}
```

Gov24 단건 수동 동기화:

```http
POST /api/admin/policies/gov24/{serviceId}/sync
```

맞춤 추천:

```http
POST /api/policies/recommendations
Content-Type: application/json

{
  "age": 27,
  "employmentStatus": "구직자",
  "studentStatus": false
}
```

정책 챗봇:

```http
POST /api/policy-chat
Content-Type: application/json

{
  "message": "서울 사는 27살 취준생인데 받을 수 있는 지원 있어?",
  "profile": {
    "age": 27,
    "employmentStatus": "구직자",
    "studentStatus": false
  }
}
```

관심 정책:

```http
POST /api/policies/{policyId}/bookmark
X-Member-Id: 1

GET /api/policy-bookmarks
X-Member-Id: 1
```

정책 캘린더:

```http
GET /api/policy-calendar?yearMonth=2026-07
X-Member-Id: 1
```

## 임시 결정 사항

- 기존 인증/회원 모듈이 없어서 관심 정책 API는 임시로 `X-Member-Id` 헤더를 사용합니다.
- 실제 LLM/임베딩 호출은 아직 연결하지 않았고, 현재 챗봇은 추천 엔진 결과를 기반으로 답변을 생성합니다.
- `policy_embedding.embedding_json`은 우선 JSONB로 저장합니다. PostgreSQL `pgvector` 사용 가능 여부가 확인되면 vector 컬럼으로 전환할 수 있습니다.
