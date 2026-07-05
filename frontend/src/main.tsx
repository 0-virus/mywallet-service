import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';
const MEMBER_ID = '1';

type PolicySummary = {
  policyId: number;
  title: string;
  agencyName: string | null;
  regionScope: string | null;
  category: string | null;
  summary: string | null;
  benefitSummary: string | null;
  applicationStatus: string;
  eligibilityStatus: string;
  startDate: string | null;
  dueDate: string | null;
  dday: number | null;
  applyMethod: string | null;
  officialUrl: string | null;
  bookmarked: boolean;
};

type Recommendation = {
  policyId: number;
  title: string;
  agencyName: string | null;
  regionScope: string | null;
  summary: string | null;
  benefitSummary: string | null;
  applicationStatus: string;
  eligibilityStatus: string;
  dueDate: string | null;
  dday: number | null;
  score: number;
  matchedReasons: string[];
  needCheckReasons: string[];
  rejectedReasons: string[];
  officialUrl: string | null;
};

type Citation = {
  policyId: number;
  policyTitle: string;
  chunkType: string;
  content: string;
  officialUrl: string | null;
  similarityScore: number;
};

type Profile = {
  age: string;
  employmentStatus: string;
  studentStatus: string;
  incomeRange: string;
  householdStatus: string;
  housingStatus: string;
  interestCategories: string;
  notificationAgreed: boolean;
};

const initialProfile: Profile = {
  age: '27',
  employmentStatus: '구직자',
  studentStatus: 'false',
  incomeRange: '',
  householdStatus: '',
  housingStatus: '',
  interestCategories: '주거,취업',
  notificationAgreed: true,
};

function App() {
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [status, setStatus] = useState('');
  const [policies, setPolicies] = useState<PolicySummary[]>([]);
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [profile, setProfile] = useState<Profile>(initialProfile);
  const [profileStatus, setProfileStatus] = useState('');
  const [chatMessage, setChatMessage] = useState('서울 사는 27살 취준생인데 받을 수 있는 지원 있어?');
  const [chatAnswer, setChatAnswer] = useState('');
  const [citations, setCitations] = useState<Citation[]>([]);
  const [loading, setLoading] = useState(false);

  const profilePayload = useMemo(() => ({
    age: profile.age ? Number(profile.age) : null,
    employmentStatus: profile.employmentStatus || null,
    studentStatus: profile.studentStatus === '' ? null : profile.studentStatus === 'true',
    incomeRange: profile.incomeRange || null,
    householdStatus: profile.householdStatus || null,
    housingStatus: profile.housingStatus || null,
    interestCategories: profile.interestCategories || category || null,
    notificationAgreed: profile.notificationAgreed,
  }), [profile, category]);

  useEffect(() => {
    void loadProfile();
    void loadPolicies();
  }, []);

  async function loadProfile() {
    const response = await fetch(`${API_BASE}/api/policy/profile`, {
      headers: { 'X-Member-Id': MEMBER_ID },
    });
    if (response.status === 404) {
      return;
    }
    if (!response.ok) {
      setProfileStatus('프로필을 불러오지 못했습니다.');
      return;
    }
    const data = await response.json();
    setProfile({
      age: data.age?.toString() ?? '',
      employmentStatus: data.employmentStatus ?? '',
      studentStatus: data.studentStatus === null || data.studentStatus === undefined ? '' : String(data.studentStatus),
      incomeRange: data.incomeRange ?? '',
      householdStatus: data.householdStatus ?? '',
      housingStatus: data.housingStatus ?? '',
      interestCategories: data.interestCategories ?? '',
      notificationAgreed: Boolean(data.notificationAgreed),
    });
  }

  async function saveProfile() {
    const response = await fetch(`${API_BASE}/api/policy/profile`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Member-Id': MEMBER_ID,
      },
      body: JSON.stringify(profilePayload),
    });
    setProfileStatus(response.ok ? '저장되었습니다.' : '저장에 실패했습니다.');
  }

  async function loadPolicies() {
    setLoading(true);
    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (category) params.set('category', category);
    if (status) params.set('applicationStatus', status);
    params.set('size', '20');
    const response = await fetch(`${API_BASE}/api/policies?${params.toString()}`);
    const data = await response.json();
    setPolicies(data.content ?? []);
    setLoading(false);
  }

  async function recommend() {
    setLoading(true);
    const response = await fetch(`${API_BASE}/api/policies/recommendations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(profilePayload),
    });
    setRecommendations(await response.json());
    setLoading(false);
  }

  async function chat() {
    setLoading(true);
    const response = await fetch(`${API_BASE}/api/policy-chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: chatMessage, profile: profilePayload }),
    });
    const data = await response.json();
    setChatAnswer(data.answer);
    setRecommendations(data.recommendations ?? []);
    setCitations(data.citations ?? []);
    setLoading(false);
  }

  async function bookmark(policyId: number) {
    await fetch(`${API_BASE}/api/policies/${policyId}/bookmark`, {
      method: 'POST',
      headers: { 'X-Member-Id': MEMBER_ID },
    });
  }

  return (
    <main className="shell">
      <section className="topbar">
        <div>
          <p className="eyebrow">청년 정책 추천</p>
          <h1>내 조건에 맞는 혜택을 놓치지 않게</h1>
        </div>
        <button className="primary" onClick={recommend} disabled={loading}>맞춤 추천</button>
      </section>

      <section className="layout">
        <div className="mainPanel">
          <div className="toolbar">
            <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="월세, 취업, 장학금" />
            <select value={category} onChange={(event) => setCategory(event.target.value)}>
              <option value="">전체 분야</option>
              <option value="주거">주거</option>
              <option value="취업">취업</option>
              <option value="창업">창업</option>
              <option value="금융">금융</option>
              <option value="생활지원">생활지원</option>
            </select>
            <select value={status} onChange={(event) => setStatus(event.target.value)}>
              <option value="">전체 상태</option>
              <option value="open">접수 중</option>
              <option value="closing_soon">마감 임박</option>
              <option value="scheduled">접수 예정</option>
              <option value="always_open">상시</option>
            </select>
            <button onClick={loadPolicies} disabled={loading}>검색</button>
          </div>

          <PolicyList title="정책 목록" policies={policies} onBookmark={bookmark} />
          {recommendations.length > 0 && (
            <RecommendationList recommendations={recommendations} onBookmark={bookmark} />
          )}
        </div>

        <aside className="sidePanel">
          <ProfileForm profile={profile} setProfile={setProfile} onSave={saveProfile} status={profileStatus} />
          <div className="chatBox">
            <h2>정책 챗봇</h2>
            <textarea value={chatMessage} onChange={(event) => setChatMessage(event.target.value)} />
            <button className="primary wide" onClick={chat} disabled={loading}>질문하기</button>
            {chatAnswer && <p className="answer">{chatAnswer}</p>}
            {citations.length > 0 && <CitationList citations={citations} />}
          </div>
        </aside>
      </section>
    </main>
  );
}

function ProfileForm({
  profile,
  setProfile,
  onSave,
  status,
}: {
  profile: Profile;
  setProfile: (profile: Profile) => void;
  onSave: () => void;
  status: string;
}) {
  return (
    <div className="profileBox">
      <h2>사용자 조건</h2>
      <label>나이<input value={profile.age} onChange={(event) => setProfile({ ...profile, age: event.target.value })} /></label>
      <label>고용 상태<input value={profile.employmentStatus} onChange={(event) => setProfile({ ...profile, employmentStatus: event.target.value })} /></label>
      <label>학생 여부
        <select value={profile.studentStatus} onChange={(event) => setProfile({ ...profile, studentStatus: event.target.value })}>
          <option value="">모름</option>
          <option value="true">학생</option>
          <option value="false">학생 아님</option>
        </select>
      </label>
      <label>소득 구간<input value={profile.incomeRange} onChange={(event) => setProfile({ ...profile, incomeRange: event.target.value })} placeholder="중위소득 100% 이하" /></label>
      <label>가구 상태<input value={profile.householdStatus} onChange={(event) => setProfile({ ...profile, householdStatus: event.target.value })} placeholder="1인가구" /></label>
      <label>주거 상태<input value={profile.housingStatus} onChange={(event) => setProfile({ ...profile, housingStatus: event.target.value })} placeholder="무주택" /></label>
      <label>관심 분야<input value={profile.interestCategories} onChange={(event) => setProfile({ ...profile, interestCategories: event.target.value })} placeholder="주거,취업" /></label>
      <label className="inline">
        <input type="checkbox" checked={profile.notificationAgreed} onChange={(event) => setProfile({ ...profile, notificationAgreed: event.target.checked })} />
        알림 동의
      </label>
      <button className="primary wide" onClick={onSave}>조건 저장</button>
      {status && <p className="statusText">{status}</p>}
    </div>
  );
}

function CitationList({ citations }: { citations: Citation[] }) {
  return (
    <div className="citations">
      <h3>근거</h3>
      {citations.slice(0, 3).map((citation) => (
        <div className="citation" key={`${citation.policyId}-${citation.chunkType}`}>
          <p className="citationTitle">{citation.policyTitle} · {labelChunkType(citation.chunkType)}</p>
          <p>{trimCitation(citation.content)}</p>
        </div>
      ))}
    </div>
  );
}

function PolicyList({ title, policies, onBookmark }: { title: string; policies: PolicySummary[]; onBookmark: (policyId: number) => void }) {
  return (
    <section className="list">
      <h2>{title}</h2>
      {policies.length === 0 && <p className="empty">표시할 정책이 없습니다. 데이터를 동기화하거나 검색 조건을 바꿔보세요.</p>}
      <div className="cards">
        {policies.map((policy) => (
          <PolicyCard key={policy.policyId} policy={policy} onBookmark={onBookmark} />
        ))}
      </div>
    </section>
  );
}

function RecommendationList({ recommendations, onBookmark }: { recommendations: Recommendation[]; onBookmark: (policyId: number) => void }) {
  return (
    <section className="list">
      <h2>맞춤 추천</h2>
      <div className="cards">
        {recommendations.map((policy) => (
          <PolicyCard key={policy.policyId} policy={policy} onBookmark={onBookmark} />
        ))}
      </div>
    </section>
  );
}

function PolicyCard({ policy, onBookmark }: { policy: PolicySummary | Recommendation; onBookmark: (policyId: number) => void }) {
  const reasons = 'needCheckReasons' in policy ? policy.needCheckReasons : [];
  return (
    <article className="card">
      <div className="cardHeader">
        <span className={`badge ${policy.applicationStatus}`}>{labelStatus(policy.applicationStatus)}</span>
        <span className="dday">{policy.dday === null ? 'D-Day 확인 필요' : `D-${policy.dday}`}</span>
      </div>
      <h3>{policy.title}</h3>
      <p className="meta">{policy.regionScope ?? '지역 확인'} · {policy.agencyName ?? '기관 확인'}</p>
      <p>{policy.summary ?? policy.benefitSummary ?? '정책 요약을 확인하세요.'}</p>
      {reasons.length > 0 && <p className="check">확인 필요: {reasons.join(', ')}</p>}
      <div className="actions">
        {policy.officialUrl && <a href={policy.officialUrl} target="_blank" rel="noreferrer">공식 링크</a>}
        <button onClick={() => onBookmark(policy.policyId)}>관심 저장</button>
      </div>
    </article>
  );
}

function labelStatus(status: string) {
  const labels: Record<string, string> = {
    open: '접수 중',
    closing_soon: '마감 임박',
    scheduled: '접수 예정',
    closed: '마감',
    always_open: '상시',
    unknown: '확인 필요',
  };
  return labels[status] ?? status;
}

function labelChunkType(type: string) {
  const labels: Record<string, string> = {
    summary: '정책 요약',
    target: '지원대상',
    criteria: '선정기준',
    benefit: '지원내용',
    apply: '신청방법',
    contact: '문의처',
  };
  return labels[type] ?? type;
}

function trimCitation(content: string) {
  const compact = content.replace(/\s+/g, ' ').trim();
  return compact.length > 180 ? `${compact.slice(0, 180)}...` : compact;
}

createRoot(document.getElementById('root')!).render(<App />);
