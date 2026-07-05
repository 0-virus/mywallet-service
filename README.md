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
OPENAI_API_KEY=OpenAI_API_키
POLICY_RAG_EMBEDDING_PROVIDER=local 또는 openai
POLICY_RAG_CHAT_PROVIDER=local 또는 openai
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
OPENAI_CHAT_MODEL=gpt-4o-mini
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

지자체복지서비스 단건 수동 동기화:

```http
POST /api/admin/policies/local-welfare/{servId}/sync
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

사용자 정책 프로필:

```http
GET /api/policy/profile
X-Member-Id: 1

POST /api/policy/profile
X-Member-Id: 1
Content-Type: application/json

{
  "age": 27,
  "incomeRange": "중위소득 50% 이하",
  "employmentStatus": "구직",
  "studentStatus": false,
  "householdStatus": "1인가구",
  "housingStatus": "월세",
  "interestCategories": "주거,취업",
  "notificationAgreed": true
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

챗봇 응답에는 추천 정책과 함께 `citations`가 포함됩니다. 기본 RAG MVP는 외부 API key 없이 동작하는 `local-hashing-v1` 임베딩을 사용해 `policy_document_chunk`와 `policy_embedding.embedding_json`을 검색합니다.

OpenAI 직접 연동 모드:

```powershell
$env:OPENAI_API_KEY='...'
$env:POLICY_RAG_EMBEDDING_PROVIDER='openai'
$env:POLICY_RAG_CHAT_PROVIDER='openai'
$env:OPENAI_EMBEDDING_MODEL='text-embedding-3-small'
$env:OPENAI_CHAT_MODEL='gpt-4o-mini'
```

```json
{
  "answer": "...",
  "recommendations": [],
  "citations": [
    {
      "policyId": 1,
      "policyTitle": "청년내일저축계좌",
      "chunkType": "target",
      "content": "...",
      "similarityScore": 0.42
    }
  ]
}
```

관심 정책:

```http
POST /api/policies/{policyId}/bookmark
X-Member-Id: 1

GET /api/policy-bookmarks
X-Member-Id: 1

DELETE /api/policy-bookmarks/{bookmarkId}
X-Member-Id: 1
```

정책 캘린더:

```http
GET /api/policy-calendar?yearMonth=2026-07
X-Member-Id: 1
```

## 임시 결정 사항

- 기존 인증/회원 모듈이 없어서 관심 정책 API는 임시로 `X-Member-Id` 헤더를 사용합니다.
- `POLICY_RAG_CHAT_PROVIDER=openai`로 실행하면 OpenAI Chat Completions API로 답변을 생성합니다. 기본값은 local fallback입니다.
- `policy_embedding.embedding_json`은 우선 JSONB로 저장합니다. PostgreSQL `pgvector` 사용 가능 여부가 확인되면 vector 컬럼으로 전환할 수 있습니다.
- 임베딩 provider는 `TextEmbeddingProvider` 인터페이스로 분리되어 있습니다. `POLICY_RAG_EMBEDDING_PROVIDER=openai`로 실행하면 OpenAI Embeddings API를 사용합니다.
- 로컬 환경변수 `DEBUG`가 설정되어 있으면 Spring Boot가 긴 디버그 리포트를 출력할 수 있습니다. 필요하면 `Remove-Item Env:DEBUG` 후 백엔드를 실행합니다.
