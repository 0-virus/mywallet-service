CREATE TABLE region_code (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES region_code(id),
    region_code VARCHAR(30),
    province VARCHAR(50),
    city VARCHAR(50),
    region_level VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE policy (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(30) NOT NULL,
    source_policy_id VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary TEXT,
    agency_name VARCHAR(150),
    department_name VARCHAR(150),
    category VARCHAR(50),
    support_type VARCHAR(50),
    target_text TEXT,
    criteria_text TEXT,
    benefit_text TEXT,
    apply_text TEXT,
    required_docs_text TEXT,
    contact_text TEXT,
    region_scope VARCHAR(30) NOT NULL DEFAULT 'unknown',
    official_url VARCHAR(700),
    application_url VARCHAR(700),
    start_date DATE,
    due_date DATE,
    application_status VARCHAR(30) NOT NULL DEFAULT 'unknown',
    dday INTEGER,
    is_always_open BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    dedup_key VARCHAR(700),
    last_synced_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_policy_source UNIQUE (source_type, source_policy_id)
);

CREATE TABLE policy_condition (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    min_age INTEGER,
    max_age INTEGER,
    gender VARCHAR(20),
    income_band VARCHAR(100),
    employment_status VARCHAR(100),
    student_status BOOLEAN,
    household_status VARCHAR(100),
    housing_status VARCHAR(100),
    business_status VARCHAR(100),
    condition_summary TEXT,
    need_manual_check BOOLEAN NOT NULL DEFAULT false,
    missing_fields JSONB,
    condition_source VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE policy_region (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    region_id BIGINT REFERENCES region_code(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_policy_region UNIQUE (policy_id, region_id)
);

CREATE TABLE policy_source_raw (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(30) NOT NULL,
    source_service_id VARCHAR(100) NOT NULL,
    endpoint VARCHAR(100) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    raw_data JSONB NOT NULL,
    CONSTRAINT uk_policy_source_raw UNIQUE (source_type, source_service_id, endpoint)
);

CREATE TABLE policy_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    chunk_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE policy_embedding (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGINT NOT NULL REFERENCES policy_document_chunk(id) ON DELETE CASCADE,
    embedding_json JSONB,
    embedding_model VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_policy_profile (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    residence_region_id BIGINT REFERENCES region_code(id),
    birth_date DATE,
    age INTEGER,
    income_range VARCHAR(100),
    employment_status VARCHAR(100),
    student_status BOOLEAN,
    household_status VARCHAR(100),
    housing_status VARCHAR(100),
    interest_categories VARCHAR(500),
    notification_agreed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_policy_profile_member UNIQUE (member_id)
);

CREATE TABLE policy_bookmark (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    apply_status VARCHAR(30) NOT NULL DEFAULT 'planned',
    notification_enabled BOOLEAN NOT NULL DEFAULT true,
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_policy_bookmark_member_policy UNIQUE (member_id, policy_id)
);

CREATE TABLE policy_calendar_event (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_date DATE NOT NULL,
    title VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE policy_notification (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    bookmark_id BIGINT REFERENCES policy_bookmark(id) ON DELETE CASCADE,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    is_sent BOOLEAN NOT NULL DEFAULT false,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE policy_recommendation_log (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT,
    policy_id BIGINT NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
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

CREATE INDEX idx_policy_status_due_date ON policy(application_status, due_date);
CREATE INDEX idx_policy_category ON policy(category);
CREATE INDEX idx_policy_dedup_key ON policy(dedup_key);
CREATE INDEX idx_policy_region_policy ON policy_region(policy_id);
CREATE INDEX idx_policy_region_region ON policy_region(region_id);
CREATE INDEX idx_policy_condition_policy ON policy_condition(policy_id);
CREATE INDEX idx_policy_chunk_policy_type ON policy_document_chunk(policy_id, chunk_type);
CREATE INDEX idx_policy_calendar_member_date ON policy_calendar_event(member_id, event_date);
CREATE INDEX idx_policy_notification_member_scheduled ON policy_notification(member_id, scheduled_at);
