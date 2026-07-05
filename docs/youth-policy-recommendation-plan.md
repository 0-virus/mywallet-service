# 청년 정책 추천 서비스 마일스톤 작업 계획

## 0. 문서 목적

이 문서는 청년 정책 추천 기능을 실제 구현하기 위한 작업 계획이다. 기존 기획 문서의 정책 추천 요구사항을 바탕으로, DB 변경부터 API 수집, 정책 목록/상세 화면, 신청 가능 여부 판별, RAG 챗봇, 관심 정책/캘린더/알림까지 마일스톤 단위로 나누어 정리한다.

이 계획은 이후 실제 개발 작업의 기준 문서로 사용한다. 각 마일스톤은 독립적으로 검증 가능한 산출물을 갖고, 앞 단계가 끝나야 다음 단계의 구현 품질이 안정된다.

## 1. 구현 범위

### 1.1 핵심 기능

- 공공데이터 API 기반 청년 정책 수집
- 중앙부처/전국 정책과 지자체 정책 통합 저장
- 지역 코드 표준화
- 정책 조건 구조화
- 신청기간 상태와 D-Day 계산
- 중복 정책 병합
- 정책 목록 조회, 키워드 검색, 카테고리/지역 필터, 정렬
- 정책 상세 조회
- 사용자 조건 기반 신청 가능 여부 판별
- 맞춤 정책 추천
- RAG 기반 자연어 정책 질의
- 관심 정책 저장
- 관심 정책 신청 상태 관리
- 정책 캘린더
- 정책 알림
- API 실패 및 데이터 누락 예외 처리
- 사용자별 접근 제어

### 1.2 사용 데이터

대한민국 공공서비스 정보:

- Swagger: `https://infuser.odcloud.kr/api/stages/44436/api-docs?1684891964110`
- 주요 엔드포인트:
  - `/gov24/v3/serviceList`
  - `/gov24/v3/serviceDetail`
  - `/gov24/v3/supportConditions`

한국사회보장정보원 지자체복지서비스:

- Base URL: `https://apis.data.go.kr/B554287/LocalGovernmentWelfareInformations`
- 주요 엔드포인트:
  - `/LcgvWelfarelist`
  - `/LcgvWelfaredetailed`

공공데이터 인증키는 코드에 직접 저장하지 않고 환경변수 또는 배포 환경 secret으로 주입한다.

권장 환경변수:

```text
PUBLIC_DATA_SERVICE_KEY
```

## 2. 마일스톤 요약

| 마일스톤 | 목표 | 주요 산출물 | 완료 기준 |
| --- | --- | --- | --- |
| M0 | 프로젝트 현황 파악 및 작업 기준 확정 | 현재 구조 분석, 작업 브랜치, 설정 목록 | 실행/테스트 방법과 수정 범위 확인 |
| M1 | 정책 도메인 DB 기반 구축 | migration, entity, repository | 정책 원본/정규화/조건/지역 테이블 저장 가능 |
| M2 | 정책 데이터 수집 및 정규화 | API client, scheduler, normalizer | 두 API에서 정책을 수집해 DB에 저장 가능 |
| M3 | 정책 목록/상세 조회 구현 | 목록 API, 상세 API, 검색/필터/정렬 | 화면에 정책 카드와 상세 정보 표시 가능 |
| M4 | 신청 가능 여부 판별 및 추천 | eligibility service, scoring service | 신청 가능/확인 필요/신청 불가와 사유 반환 |
| M5 | RAG 검색 및 챗봇 | chunk, embedding, retriever, chat API | 자연어 질문에 근거 기반 추천 답변 반환 |
| M6 | 관심 정책, 캘린더, 알림 | bookmark, calendar, notification | 관심 정책 저장, 일정 표시, 알림 예약 가능 |
| M7 | 안정화, 보안, QA | 테스트, 성능 개선, 예외 처리 | 발표/시연 가능한 품질 확보 |

## 3. M0. 프로젝트 현황 파악 및 작업 기준 확정

### 목표

현재 레포지토리의 Spring Boot, React, DB, Docker 구성을 확인하고 정책 추천 기능을 어느 모듈에 추가할지 결정한다.

### 작업

- 백엔드 구조 확인
  - `build.gradle` 또는 `pom.xml`
  - Spring Boot 버전
  - Spring AI 사용 여부
  - JPA, MyBatis, QueryDSL 등 persistence 방식
  - migration 도구 사용 여부: Flyway, Liquibase, schema.sql 등
- 프론트엔드 구조 확인
  - React/Vite 진입점
  - 라우팅 구조
  - API client 구조
  - 상태 관리 방식
  - UI 컴포넌트 구조
- DB 실행 방식 확인
  - 로컬 PostgreSQL
  - Docker Compose
  - 테스트 DB 설정
- 정책 추천 기능의 패키지와 라우트 위치 결정
- 공공데이터 인증키 주입 방식 결정
- 작업 브랜치 또는 작업 단위 확정

### 산출물

- 프로젝트 구조 메모
- 정책 추천 기능 패키지 위치
- 실행 명령어 정리
- 환경변수 목록

### 완료 기준

- 백엔드와 프론트엔드를 로컬에서 실행할 수 있다.
- DB migration 적용 방식을 확인했다.
- 정책 추천 기능을 추가할 코드 위치를 결정했다.
- 공공데이터 인증키가 코드에 하드코딩되지 않도록 설정 방향을 정했다.

### 검증

- 백엔드 기본 테스트 실행
- 프론트엔드 빌드 또는 dev server 실행
- DB 연결 확인

## 4. M1. 정책 도메인 DB 기반 구축

### 목표

정책 추천 기능에 필요한 최소 DB 구조를 구축한다. 기존 스키마의 `policy`, `policy_condition`, `policy_region`, `region_code`, `policy_bookmark`, `policy_calendar_event`, `policy_notification`을 활용하되, RAG와 수집 파이프라인에 필요한 테이블을 추가한다.

### DB 작업

#### 4.1 정책 중심 테이블 보강

`policy` 테이블에 다음 개념을 반영한다.

- `source_type`: `gov24`, `local_welfare`, `manual`
- `source_policy_id`: 원본 API 정책 ID
- `title`
- `summary`
- `agency_name`
- `department_name`
- `category`
- `support_type`
- `target_text`
- `criteria_text`
- `benefit_text`
- `apply_text`
- `required_docs_text`
- `contact_text`
- `region_scope`: `national`, `sido`, `sigungu`
- `official_url`
- `application_url`
- `start_date`
- `due_date`
- `application_status`: `scheduled`, `open`, `closing_soon`, `closed`, `always_open`, `unknown`
- `dday`
- `is_always_open`
- `is_active`
- `dedup_key`
- `last_synced_at`
- `created_at`
- `updated_at`

#### 4.2 정책 조건 테이블 보강

`policy_condition`은 신청 가능 여부 판별의 핵심이다.

필요 필드:

- `policy_id`
- `min_age`
- `max_age`
- `gender`
- `income_band`
- `employment_status`
- `student_status`
- `household_status`
- `housing_status`
- `business_status`
- `condition_summary`
- `need_manual_check`
- `missing_fields`
- `condition_source`

#### 4.3 원본 데이터 저장 테이블 추가

```sql
CREATE TABLE policy_source_raw (
  id BIGSERIAL PRIMARY KEY,
  source_type VARCHAR(30) NOT NULL,
  source_service_id VARCHAR(100) NOT NULL,
  endpoint VARCHAR(100) NOT NULL,
  fetched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  raw_data JSONB NOT NULL,
  UNIQUE (source_type, source_service_id, endpoint)
);
```

#### 4.4 RAG 문서 테이블 추가

```sql
CREATE TABLE policy_document_chunk (
  id BIGSERIAL PRIMARY KEY,
  policy_id BIGINT NOT NULL REFERENCES policy(id),
  chunk_type VARCHAR(50) NOT NULL,
  content TEXT NOT NULL,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

#### 4.5 임베딩 테이블 추가

PostgreSQL `pgvector` 사용을 우선 검토한다.

```sql
CREATE TABLE policy_embedding (
  id BIGSERIAL PRIMARY KEY,
  chunk_id BIGINT NOT NULL REFERENCES policy_document_chunk(id),
  embedding vector(1536),
  embedding_model VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

프로젝트에서 `pgvector` 적용이 어렵다면 M5에서 임베딩 저장소를 별도 옵션으로 조정한다.

#### 4.6 추천 이력 테이블 추가

```sql
CREATE TABLE policy_recommendation_log (
  id BIGSERIAL PRIMARY KEY,
  member_id BIGINT,
  policy_id BIGINT NOT NULL REFERENCES policy(id),
  query_text TEXT,
  filter_score NUMERIC(6,2),
  vector_score NUMERIC(6,2),
  final_score NUMERIC(6,2),
  eligibility_status VARCHAR(30) NOT NULL,
  matched_reasons JSONB,
  need_check_reasons JSONB,
  rejected_reasons JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

#### 4.7 인덱스

필수 인덱스:

- `policy(source_type, source_policy_id)`
- `policy(dedup_key)`
- `policy(application_status, due_date)`
- `policy(category)`
- `policy_region(policy_id, region_id)`
- `policy_condition(policy_id)`
- `policy_bookmark(member_id, policy_id)` unique
- `policy_document_chunk(policy_id, chunk_type)`

### 백엔드 작업

- 정책 도메인 entity/model 작성
- repository 작성
- enum 또는 code constant 작성
- region code seed 방식 결정
- migration 테스트 작성

### 산출물

- DB migration 파일
- 정책 도메인 entity/repository
- 기본 enum/code 정의
- region seed 데이터 또는 import 스크립트

### 완료 기준

- migration이 깨끗하게 적용된다.
- 정책 원본 데이터를 저장할 수 있다.
- 정책 정규화 데이터를 저장할 수 있다.
- 정책 조건, 지역, chunk, 추천 이력 테이블이 준비된다.

### 검증

- migration apply test
- repository save/find test
- unique constraint test
- FK constraint test

## 5. M2. 정책 데이터 수집 및 정규화

### 목표

두 공공데이터 API에서 정책 데이터를 수집하고 내부 공통 정책 모델로 저장한다.

### 백엔드 작업

#### 5.1 API Client 구현

`Gov24PolicyClient`

- `fetchServiceList(page, perPage, filters)`
- `fetchServiceDetail(serviceId)`
- `fetchSupportConditions(serviceId)`

`LocalWelfarePolicyClient`

- `fetchWelfareList(pageNo, numOfRows, filters)`
- `fetchWelfareDetail(servId)`

구현 기준:

- 인증키는 환경변수에서 주입
- timeout 설정
- retry는 과도하지 않게 제한
- API 오류 응답을 명확한 exception으로 변환
- XML 응답은 DTO로 파싱 후 JSON 원본도 보존

#### 5.2 Raw 저장

수집한 응답은 정규화 전에 `policy_source_raw`에 저장한다.

저장 기준:

- 목록 응답
- 상세 응답
- 지원조건 응답
- 실패 응답은 별도 로그 또는 sync history에 기록

#### 5.3 정규화 구현

`PolicyNormalizer`를 구현한다.

Gov24 매핑:

- `서비스ID` -> `source_policy_id`
- `서비스명` -> `title`
- `서비스목적`, `서비스목적요약` -> `summary`
- `지원대상` -> `target_text`
- `선정기준` -> `criteria_text`
- `지원내용` -> `benefit_text`
- `신청방법` -> `apply_text`
- `신청기한` -> `start_date`, `due_date`, `is_always_open`
- `온라인신청사이트URL`, `상세조회URL` -> `application_url`, `official_url`
- `문의처`, `전화문의` -> `contact_text`
- `지원유형` -> `support_type`
- `소관기관명`, `부서명` -> `agency_name`, `department_name`

Local Welfare 매핑:

- `servId` -> `source_policy_id`
- `servNm` -> `title`
- `servDgst` -> `summary`
- `sprtTrgtCn` -> `target_text`
- `slctCritCn` -> `criteria_text`
- `alwServCn` -> `benefit_text`
- `aplyMtdCn` -> `apply_text`
- `servDtlLink` -> `official_url`
- `ctpvNm`, `sggNm` -> 지역 매핑
- `bizChrDeptNm` -> `department_name`
- `inqplCtadrList` -> `contact_text`
- `basfrmList` -> `required_docs_text`

#### 5.4 조건 추출

`PolicyConditionExtractor`를 구현한다.

구조화 우선순위:

1. Gov24 `/supportConditions`
2. 지자체 API의 `lifeNmArray`, `trgterIndvdlNmArray`, `intrsThemaNmArray`
3. 상세 텍스트 정규식
4. 자동 판별 불가 시 `need_manual_check = true`

나이 추출:

- `JA0110`, `JA0111`
- 텍스트의 `만 19세 이상`, `39세 이하`, `18세 이상 ~ 34세 이하` 패턴

소득 추출:

- `중위소득 50% 이하`
- `중위소득 100% 이하`
- `월 소득`, `가구소득` 문구는 `확인 필요`로 남김

고용/상태 태그:

- 구직자
- 근로자
- 대학생
- 창업자
- 무주택
- 1인가구
- 다자녀
- 보훈대상자
- 장애인

#### 5.5 지역 표준화

`RegionResolver`를 구현한다.

처리 기준:

- 전국 정책: `region_scope = national`
- 시/도만 있는 정책: `region_scope = sido`
- 시/군/구까지 있는 정책: `region_scope = sigungu`
- 지역명이 불명확하면 `region_scope = unknown`, `need_manual_check = true`

#### 5.6 신청기간 상태 계산

`PolicyApplicationStatusCalculator`를 구현한다.

상태:

- `scheduled`: 접수 예정
- `open`: 접수 중
- `closing_soon`: 마감 임박
- `closed`: 마감
- `always_open`: 상시 신청
- `unknown`: 기간 확인 필요

마감 임박 기준:

- 기본 7일 이내

#### 5.7 중복 정책 병합

`PolicyDeduplicator`를 구현한다.

`dedup_key` 구성:

- 정규화된 정책명
- 기관명
- 지역
- 신청기간
- 신청 링크

중복 처리:

- 동일 `source_type + source_policy_id`는 update
- 서로 다른 source에서 같은 정책으로 판단되면 대표 policy 하나로 병합
- 원본 raw는 모두 보존

#### 5.8 스케줄러

`PolicySyncScheduler`를 구현한다.

MVP 기준:

- 수동 실행 API 또는 command runner 먼저 구현
- 이후 매일 1회 배치로 확장

### 산출물

- 두 API client
- XML parser
- raw 저장 로직
- normalizer
- condition extractor
- region resolver
- application status calculator
- deduplicator
- sync scheduler 또는 수동 sync endpoint

### 완료 기준

- Gov24 정책을 수집해 `policy`에 저장할 수 있다.
- 지자체복지서비스 정책을 수집해 `policy`에 저장할 수 있다.
- 원본 응답이 `policy_source_raw`에 저장된다.
- 청년 관련 정책이 분류된다.
- 지역, 신청기간, 조건이 일부라도 구조화된다.
- API 실패 시 전체 수집이 중단되지 않고 실패 내역이 확인된다.

### 검증

- API client 단위 테스트
- XML fixture parsing test
- normalizer mapping test
- condition extractor test
- duplicate merge test
- local DB integration test

## 6. M3. 정책 목록 및 상세 조회

### 목표

사용자가 지역 정책 목록을 조회하고, 검색/필터/정렬을 통해 정책을 탐색하며, 상세 화면에서 신청 조건과 공식 링크를 확인할 수 있게 한다.

### 백엔드 작업

#### 6.1 목록 API

```http
GET /api/policies
```

Query parameter:

- `regionId`
- `sido`
- `sigungu`
- `category`
- `keyword`
- `applicationStatus`
- `eligibilityStatus`
- `sort`
- `page`
- `size`

정렬 옵션:

- `eligibility_deadline`: 신청 가능성 높은 순 + 마감 임박순
- `deadline`: 마감 임박순
- `latest`: 최신 등록순
- `popular`: 관심 많은 순

응답 필드:

- `policyId`
- `title`
- `agencyName`
- `regionLabel`
- `category`
- `summary`
- `benefitSummary`
- `applicationStatus`
- `eligibilityStatus`
- `startDate`
- `dueDate`
- `dday`
- `applyMethod`
- `officialUrl`
- `bookmarked`

#### 6.2 상세 API

```http
GET /api/policies/{policyId}
```

응답 필드:

- 목록 필드 전체
- `targetText`
- `criteriaText`
- `benefitText`
- `applyText`
- `requiredDocsText`
- `contactText`
- `applicationUrl`
- `matchedReasons`
- `needCheckReasons`
- `rejectedReasons`
- `lastSyncedAt`
- `sourceType`

외부 신청 링크가 없는 경우:

- `applicationUrl = null`
- 문의처와 신청방법 원문을 함께 반환

#### 6.3 캐시/장애 대응

정책 API 호출 실패는 실시간 화면에 직접 영향을 주지 않도록 한다. 화면 조회는 DB 기준으로 수행한다.

정책 수집 실패 시:

- 기존 DB 데이터 표시
- 마지막 갱신 시각 표시
- 관리자 로그 또는 sync failure 기록

### 프론트엔드 작업

#### 6.4 정책 목록 화면

구성:

- 지역 선택
- 검색창
- 카테고리 필터
- 신청 상태 필터
- 정렬 select
- 정책 카드 리스트
- pagination 또는 infinite scroll

정책 카드 표시:

- 정책명
- 기관/지역
- 카테고리
- 지원내용 요약
- 신청기간
- D-Day
- 신청 가능 상태
- 상세보기
- 북마크

#### 6.5 정책 상세 화면

구성:

- 정책 헤더
- 신청 가능 상태 badge
- 추천/판별 사유
- 지원대상
- 선정기준
- 지원내용
- 신청방법
- 제출서류
- 문의처
- 공식 상세 링크
- 신청 링크 또는 문의 안내
- 북마크/알림 설정

### 산출물

- 정책 목록 API
- 정책 상세 API
- 목록 화면
- 상세 화면
- 검색/필터/정렬 UI

### 완료 기준

- 지역 기준 정책 목록을 조회할 수 있다.
- 키워드 검색이 동작한다.
- 카테고리 필터가 동작한다.
- 지역 필터가 동작한다.
- 정렬이 동작한다.
- 상세 화면에서 지원대상, 선정기준, 지원내용, 신청방법, 문의처를 볼 수 있다.
- 공식 신청 링크가 없으면 문의처와 신청방법을 표시한다.

### 검증

- repository query test
- controller test
- pagination test
- frontend list rendering test
- empty state 확인
- API 실패/데이터 없음 상태 확인

## 7. M4. 신청 가능 여부 판별 및 맞춤 추천

### 목표

사용자 조건과 정책 조건을 비교해 정책별 신청 가능 상태를 계산하고, 신청 가능성이 높은 정책을 우선 추천한다.

### 백엔드 작업

#### 7.1 사용자 정책 프로필

`user_policy_profile` 또는 요청 DTO에 다음 정보를 사용한다.

- 나이 또는 생년월일
- 거주 지역
- 소득 구간
- 고용 상태
- 학생 여부
- 관심 분야
- 주거 상태
- 가구 형태

초기 MVP에서는 나이, 거주 지역, 고용 상태, 학생 여부를 우선 사용한다.

#### 7.2 프로필 API

```http
POST /api/policy/profile
GET /api/policy/profile
PATCH /api/policy/profile
```

비로그인 또는 임시 질의에서는 요청 body의 profile을 사용한다.

#### 7.3 신청 가능 여부 판별

`PolicyEligibilityService`를 구현한다.

상태:

- `eligible`: 신청 가능
- `need_check`: 확인 필요
- `ineligible`: 신청 불가

판별 규칙:

- 나이 조건이 있고 범위를 벗어나면 `ineligible`
- 지역 조건이 있고 불일치하면 `ineligible`
- 마감된 정책이면 `ineligible`
- 필수 조건을 모두 만족하면 `eligible`
- 소득, 고용, 가구 조건이 불명확하거나 사용자 정보가 없으면 `need_check`

반환 사유:

- `matchedReasons`: 충족한 조건
- `needCheckReasons`: 확인 필요 조건
- `rejectedReasons`: 신청 불가 사유

#### 7.4 추천 점수화

`PolicyRecommendationService`를 구현한다.

기본 점수:

| 기준 | 점수 |
| --- | --- |
| 신청 가능 | +40 |
| 확인 필요 | +15 |
| 지역 일치 | +30 |
| 나이 조건 일치 | +25 |
| 마감 임박 | +15 |
| 고용 상태 일치 | +15 |
| 관심 분야 일치 | +10 |
| 소득 조건 일치 | +10 |
| 조건 불명확 | -10 |
| 신청 불가 | 추천 목록 제외 |

#### 7.5 추천 API

```http
GET /api/policies/recommendations
```

또는 자연어 질의와 분리된 추천 전용 API:

```http
POST /api/policies/recommendations
```

응답:

- 추천 정책 목록
- 점수
- 신청 가능 상태
- 추천 이유
- 확인 필요 조건
- 신청 불가 사유

### 프론트엔드 작업

- 사용자 조건 입력 UI
- 신청 가능 상태 badge
- 추천 이유 표시
- 확인 필요 조건 표시
- 신청 불가 사유 표시
- 기본 정렬을 신청 가능성 높은 순 + 마감 임박순으로 설정

### 산출물

- 사용자 정책 프로필 API
- 신청 가능 여부 판별 서비스
- 추천 점수화 서비스
- 추천 API
- 추천 카드 상태/사유 UI

### 완료 기준

- 나이와 지역으로 신청 불가 정책을 걸러낼 수 있다.
- 사용자 정보가 부족한 정책은 확인 필요로 표시한다.
- 신청 가능 정책이 추천 목록 상단에 노출된다.
- 추천 사유와 확인 필요 조건이 화면에 표시된다.

### 검증

- eligibility unit test
- scoring unit test
- recommendation integration test
- 주요 시나리오 테스트
  - 서울 거주 27세 구직자
  - 부산 거주 대학생
  - 나이 조건 초과 사용자
  - 소득 정보 미입력 사용자

## 8. M5. RAG 검색 및 정책 추천 챗봇

### 목표

사용자가 자연어로 질문하면 정책 DB와 임베딩 검색을 기반으로 근거 있는 추천 답변을 생성한다.

### 백엔드 작업

#### 8.1 Chunk 생성

`PolicyChunkService`를 구현한다.

정책 하나당 chunk 유형:

- `summary`
- `target`
- `criteria`
- `benefit`
- `apply`
- `contact`
- `document`

chunk content 예시:

```text
[정책명] 청년내일저축계좌
[유형] 지원대상
[내용] 신청 당시 만 15~39세 이하...
```

#### 8.2 임베딩 생성

`PolicyEmbeddingService`를 구현한다.

처리 방식:

- 정책 생성/수정 시 chunk 재생성
- chunk 변경 시 임베딩 재생성
- embedding model 이름 저장
- 실패 시 재시도 가능하게 로그 기록

#### 8.3 Retriever 구현

`PolicyRetriever`를 구현한다.

검색 흐름:

```text
사용자 질문
  -> 질문 임베딩
  -> vector search
  -> 사용자 조건 hard filter
  -> 정책 단위 결과 병합
  -> 추천 점수와 결합
  -> topK 반환
```

검색 대상:

- 지원대상
- 선정기준
- 지원내용
- 신청방법
- 정책 요약

#### 8.4 챗봇 API

```http
POST /api/policy-chat
```

요청:

```json
{
  "message": "서울 사는 27살 취준생인데 받을 수 있는 지원 있어?",
  "profile": {
    "age": 27,
    "region": "서울특별시",
    "employmentStatus": "구직자",
    "studentStatus": false
  }
}
```

응답:

```json
{
  "answer": "...",
  "recommendations": [],
  "citations": []
}
```

#### 8.5 답변 생성 규칙

LLM 프롬프트 규칙:

- 신청 가능 여부를 단정하지 않는다.
- `신청 가능`, `확인 필요`, `신청 불가`를 구분한다.
- 확인 필요 조건을 숨기지 않는다.
- 공식 링크와 신청 방법을 안내한다.
- 외부 신청 링크가 없으면 문의처를 안내한다.
- 출처 정책명을 함께 제공한다.
- 사용자 조건에 없는 정보는 추가 질문으로 유도한다.

### 프론트엔드 작업

- 정책 챗봇 입력창
- 답변 메시지
- 답변에 연결된 추천 정책 카드
- 근거 보기 또는 상세 보기 연결
- 추가 정보 입력 유도 UI

### 산출물

- policy chunk 생성 로직
- embedding 저장 로직
- vector search
- RAG retriever
- chat API
- chat UI

### 완료 기준

- 정책 상세 데이터로 chunk를 생성할 수 있다.
- 임베딩을 저장하고 유사도 검색할 수 있다.
- 자연어 질문에 대해 관련 정책을 찾을 수 있다.
- 답변에 추천 이유와 확인 필요 조건이 포함된다.
- 정책 상세로 이동할 수 있다.

### 검증

- chunk 생성 테스트
- embedding 저장 테스트
- retriever 테스트
- prompt snapshot 테스트
- hallucination 방지 시나리오 테스트

## 9. M6. 관심 정책, 캘린더, 알림

### 목표

사용자가 관심 있는 정책을 저장하고, 신청기간을 캘린더에서 확인하며, 신청 시작일과 마감일 알림을 받을 수 있게 한다.

### 백엔드 작업

#### 9.1 관심 정책

API:

```http
POST /api/policies/{policyId}/bookmark
DELETE /api/policy-bookmarks/{bookmarkId}
GET /api/policy-bookmarks
```

규칙:

- 로그인 사용자만 저장 가능
- 동일 정책 중복 저장 불가
- 본인 북마크만 조회/삭제 가능

#### 9.2 신청 상태 관리

API:

```http
PATCH /api/policy-bookmarks/{bookmarkId}/apply-status
```

상태:

- `planned`: 신청 예정
- `applied`: 신청 완료
- `not_applicable`: 해당 없음

#### 9.3 캘린더

API:

```http
GET /api/policy-calendar?yearMonth=2026-07
```

생성 규칙:

- 관심 정책의 신청 시작일과 마감일 자동 등록
- 전체 정책을 자동 등록하지 않는다.
- 신청 가능성이 높고 마감일이 가까운 추천 정책은 선택적으로 표시한다.
- 사용자가 관심 없음 처리하면 추천 일정에서 제거한다.

#### 9.4 알림

API:

```http
PATCH /api/policy-notifications/settings
```

알림 기준:

- 신청 시작일
- 마감 7일 전
- 마감 1일 전

규칙:

- 알림 동의가 있어야 생성
- 정책별 알림 여부 변경 가능
- 정책 신청기간이 변경되면 알림 일정을 재계산
- 마감된 정책은 알림 예약 취소

### 프론트엔드 작업

- 관심 정책 저장/해제 버튼
- 관심 정책 목록
- 신청 상태 변경 UI
- 정책 캘린더 월간 뷰
- 캘린더 일정 클릭 상세 요약
- 알림 설정 UI
- 마이페이지 관심 정책 요약

### 산출물

- bookmark API
- apply status API
- calendar API
- notification setting API
- 관심 정책 UI
- 캘린더 UI
- 마이페이지 요약 UI

### 완료 기준

- 관심 정책을 저장하고 중복 저장을 막을 수 있다.
- 관심 정책 목록에서 신청 상태를 변경할 수 있다.
- 관심 정책의 신청 시작일과 마감일이 캘린더에 표시된다.
- 알림 동의 사용자의 알림이 예약된다.
- 본인이 아닌 사용자의 관심 정책에 접근할 수 없다.

### 검증

- bookmark unique test
- authorization test
- calendar event generation test
- notification schedule test
- policy date changed test

## 10. M7. 안정화, 보안, QA

### 목표

발표와 시연이 가능한 수준으로 오류 처리, 성능, 보안, UI 상태를 정리한다.

### 안정화 작업

- API 실패 시 사용자 메시지 정리
- 정책 데이터가 없을 때 empty state 표시
- 마지막 갱신 시각 표시
- 데이터 누락 시 확인 필요 표시
- 외부 신청 링크 없음 처리
- XML 파싱 실패 처리
- 중복 정책 노출 확인
- 마감 정책 노출 정책 확인

### 보안 작업

- 공공데이터 인증키 secret 처리
- 사용자별 관심 정책 접근 제어
- 신청 상태 수정 권한 확인
- 알림 설정 접근 제어
- 민감한 사용자 프로필 로그 출력 금지

### 성능 작업

- 목록 조회 pagination
- 지역/카테고리/상태 인덱스 확인
- vector search topK 제한
- 대량 수집 시 batch insert 검토
- 수집 scheduler timeout 관리

### QA 시나리오

필수 시연 시나리오:

1. 사용자가 서울, 27세, 구직자 조건을 입력한다.
2. 정책 목록에서 서울/청년 정책이 표시된다.
3. `월세` 키워드로 검색한다.
4. 정책 상세에서 지원대상, 선정기준, 신청방법을 확인한다.
5. 신청 가능 상태와 확인 필요 조건을 확인한다.
6. 정책을 관심 정책으로 저장한다.
7. 관심 정책이 캘린더에 표시된다.
8. 챗봇에 "서울 사는 27살 취준생인데 받을 수 있는 지원 있어?"라고 질문한다.
9. 챗봇이 추천 정책과 확인 필요 조건을 답변한다.
10. 공식 링크 또는 문의처로 이동한다.

### 산출물

- 오류 메시지 정리
- 주요 테스트 통과
- 시연 데이터 준비
- 발표용 시나리오 준비

### 완료 기준

- 주요 기능 시나리오가 중단 없이 동작한다.
- API 실패와 데이터 누락 상황에서 화면이 깨지지 않는다.
- 사용자별 데이터 접근 제어가 적용된다.
- 시연용 정책 데이터가 준비되어 있다.

## 11. 작업 우선순위

시간이 부족할 경우 다음 순서로 구현 범위를 줄인다.

### 필수 MVP

1. 정책 DB 스키마
2. 정책 샘플 데이터 또는 API 수집
3. 정책 목록 조회
4. 정책 상세 조회
5. 사용자 조건 기반 신청 가능/확인 필요/신청 불가 판별
6. 추천 카드 UI

### 발표 완성도 향상

1. 실제 공공데이터 API 배치 수집
2. 검색/필터/정렬
3. 관심 정책 저장
4. 캘린더 표시
5. RAG 챗봇

### 후순위

1. 알림 실제 발송
2. 정책 변경 감지 알림
3. 추천 정책 캘린더 자동 제안
4. 사용자 피드백 기반 추천 개선

## 12. 작업 시작 체크리스트

실제 구현을 시작할 때 먼저 확인한다.

- 백엔드 실행 명령어
- 프론트엔드 실행 명령어
- PostgreSQL 실행 방식
- migration 도구
- 기존 member 인증 방식
- 기존 region/member/profile 테이블 사용 방식
- 기존 notification 공통 모듈 존재 여부
- Spring AI 설정 여부
- pgvector 사용 가능 여부
- 공공데이터 인증키 환경변수 설정 여부

## 13. 최종 완료 기준

청년 정책 추천 기능은 다음 조건을 만족하면 완료로 본다.

- 중앙부처 및 지자체 정책 데이터를 DB에 저장할 수 있다.
- 정책 데이터를 지역 코드 기준으로 표준화할 수 있다.
- 동일 정책이 중복 노출되지 않도록 병합할 수 있다.
- 신청기간 상태와 D-Day를 계산할 수 있다.
- 정책 목록에서 키워드 검색, 카테고리 필터, 지역 필터, 정렬을 사용할 수 있다.
- 정책 상세에서 지원대상, 선정기준, 지원내용, 신청방법, 구비서류, 문의처를 확인할 수 있다.
- 사용자 조건에 따라 신청 가능, 확인 필요, 신청 불가 상태와 사유를 반환할 수 있다.
- 추천 카드에 정책명, 기관/지역, 지원내용, 신청방법, 신청기한, D-Day, 공식 링크 또는 문의처가 표시된다.
- RAG 챗봇이 자연어 질문에 대해 정책 추천과 확인 필요 조건을 답변할 수 있다.
- 관심 정책을 저장하고 중복 저장을 방지할 수 있다.
- 관심 정책의 신청 상태를 신청 예정, 신청 완료, 해당 없음으로 관리할 수 있다.
- 관심 정책의 신청 시작일과 마감일을 캘린더에 표시할 수 있다.
- 알림 동의가 있는 사용자에게 신청 시작일, 마감 7일 전, 마감 1일 전 알림을 예약할 수 있다.
- 본인이 아닌 사용자의 관심 정책, 신청 상태, 알림 설정에는 접근할 수 없다.

최종 사용자 경험은 다음 질문에 답할 수 있어야 한다.

```text
내 조건에서 신청 가능한 청년 정책은 무엇이고,
왜 추천됐으며,
무엇을 추가로 확인해야 하고,
언제까지 어떻게 신청해야 하는가?
```
