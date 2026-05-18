# 🐷 DON DO THAT (돈두댓)

> Z세대의 소비습관 형성을 위한 챌린지 서비스  
> KB IT's Your Life 6기 | 프로젝트 15반 2팀 BAGGISIX

---

## 📌 프로젝트 개요

**"돈두댓(DON DO THAT)"**
<img width="1398" height="782" alt="image" src="https://github.com/user-attachments/assets/baba55a9-82f8-41d8-b103-8b9af72cde6e" />

카카오톡 거지방처럼 무지출을 목표로 모이는 커뮤니티가 트렌드가 되었지만, 기간 설정 불가·강제성 부족·고인물 문제·성과 가시화 부족으로 구조적 지속이 어렵다는 한계가 있었습니다.

**DON DO THAT**은 사용자의 실제 금융 소비 내역을 분석하고, Z세대의 세 가지 특성을 반영한 서비스를 설계했습니다.

| Z세대 특성 | DON DO THAT의 해결책 |
|-----------|-------------------|
| 금융리터러시 부족 | 실제 소비내역 기반 AI 자동 분석 |
| 의지 부족 | 7~35일 기한 설정 + 챌린지 강제성 부여 |
| 즉각적 보상 선호 | 절약 금액 저금통 적립 + 티어 시스템 |

**DON DO THAT**은 사용자의 실제 금융 소비 내역(Codef API)을 분석하고,
절약 의지를 자산 형성으로 연결하는 구조로 건전한 소비습관 형성을 돕습니다.

- **프로젝트 기간** : 2025.07.09 ~ 2025.08.21 (약 6주)
- **팀명** : BAGGI-SIX (백이식스)
- **인원** : 6명

---

## 👥 팀 구성

| 서상훈 | 김민지 | 김서현 | 문상혁 | 장현지 | 최지형 |
|---|---|---|---|---|---|
| 팀장 <br> CI/CD 서버 구축<br>로그인<br>적금상품 추천 | UI/UX 팀장<br>챌린지 페이지<br>마이 페이지 | 계좌 연결<br>Codef API 연동<br>프론트엔드 팀장<br>프론트 서버관리 | 백엔드 팀장<br>소비내역 관리<br>적금상품 추천 | PM<br>실시간 채팅<br>마이페이지<br>티어 | LLM<br>챌린지 로직<br>저금통 |

---
## ✨ 주요 기능

### 1) 입출금 계좌 연동 및 AI 카테고리 분석
- Codef API를 통한 실제 계좌 연동 및 10분마다 소비내역 동기화
- 1차: 키워드 필터링 - 자주 출현하는 150개 단어로 기본 분류 (LLM 호출 최소화)
- 2차: LLM 분석 - 키워드로 걸러지지 않는 고유명사 분류 (멘츠루, 가츠시 등)

### 2) 사용자 대시보드
- 카테고리별 지출 비율 차트
- 진행 중인 챌린지 실시간 진척도
- 저금통 누적 금액 확인

### 3) AI 맞춤형 챌린지 추천
- 지난 60~30일 vs 최근 30일 증가율 분석 기반 개인화 챌린지 추천
- 하나의 챌린지만 참여 가능한 강제성 부여 시스템

### 4) 챌린지 참여 및 저금
- 매일 자정 소비내역 감지로 성공/실패 자동 판정
- 아낀 금액 계산: `일일 적립금 = 과거 동일기간 총 지출 ÷ 챌린지 기간`
- 저금 → 티어 → 상품 추천 → 자산 형성 흐름

### 5) 실시간 채팅
- WebSocket + STOMP 프로토콜 기반 챌린지별 채팅방
- 실시간 메시지 전송, 입·퇴장 알림, 접속자 수 업데이트
---

## 🛠 기술 스택
<img width="973" height="478" alt="image" src="https://github.com/user-attachments/assets/51c5e026-85f7-4a1e-8225-a6eb12e27583" />

| 분류 | 기술 |
|------|------|
| **Back-end** | Java, Spring Legacy, MyBatis, JWT, MySQL |
| **Front-end** | Vue.js, HTML, CSS, JavaScript, Tailwind CSS |
| **Infra** | AWS EC2, Nginx, Docker, Let's Encrypt (HTTPS) |
| **AI/LLM** | Python, GPT-4o |
| **협업** | Git, GitHub, Notion, Figma |

## 🗄 ERD
<img width="1252" height="492" alt="image" src="https://github.com/user-attachments/assets/66824790-2a1b-440e-80fc-f7b8d6187966" />

주요 설계 포인트:
- **챌린지 상태(status)**: `ongoing / completed / failed / closed` enum으로 관리
- **소비내역 soft delete**: `deleted_at` 컬럼으로 DB 유지 → Codef API 재호출 시 덮어쓰기 방지
- **user_modified**: 사용자가 수동 수정한 내역은 API 재호출 시 보호

## ✅ 기여 내용

<img width="1402" height="783" alt="image" src="https://github.com/user-attachments/assets/03a3fa53-ef53-4184-bf49-dd21752f0069" />

<img width="1381" height="539" alt="image" src="https://github.com/user-attachments/assets/a5f5bc99-fbe9-4549-9135-3813460ad55f" />

### 1. Codef 외부 금융 API 연동

Spring Legacy 환경에서 Codef API를 연동하여 사용자 소비내역을 호출했습니다.

- **기능**: AccessToken 관리, ConnectedID 생성, 소비 내역 조회 등 핵심 API 연동 로직 구현
- **Access Token 관리**: DB에 저장하고 만료 시 자동 재발급하는 구조 설계 → 불필요한 API 호출 감소
- **Connected ID 기반 호출**: 민감 정보를 직접 사용하지 않는 안전한 API 호출 구조 구현

---

### 2. AES-256 + RSA 이중 암호화 보안 시스템 구축



**[상황]**  
Codef API 연동 시 `bank_pw`는 RSA 암호화된 값을 요구했고, `bank_account`, `bank_id`, `connected_id` 등 민감 정보도 DB에 안전하게 저장해야 했습니다.

**[문제 판단]**  
RSA 암호화 값을 DB에 그대로 저장하면 보안 취약, 그렇다고 복호화해서 저장하면 평문 노출 문제가 발생합니다.

**[해결]**  
RSA(API 요구사항) + AES-256(DB 저장 보안)을 분리하여 이중으로 감싸는 구조를 직접 설계했습니다.

| 컬럼 | 암호화 전략 |
|------|------------|
| `bank_account`, `bank_id`, `connected_id` | AES-256 CBC + 랜덤 IV |
| `bank_pw` | RSA 최초 1회 → AES-256 재암호화 저장 |

**[추가 고려사항]**
- **성능 최적화**: RSA 연산은 최초 1회만 수행하고 결과를 AES로 재암호화 → API 호출 시 AES 복호화만 수행하여 CPU 부하 감소
- **IV 보안**: 매 암호화 시 랜덤 IV를 생성하여 암호문 패턴 노출 방지
- **키 관리**: 코드 내 하드코딩 금지 원칙 수립, 운영 환경 KMS/HSM 기반 키 관리 정책 적용

**[MyBatis TypeHandler 활용]**  
DB 입출력 시 자동 암복호화가 처리되도록 TypeHandler를 직접 구현했습니다.
- 서비스/컨트롤러 레이어에서 민감 데이터 평문 노출 없음
- 암복호화 로직 중앙 집중 관리 → 중복 제거 및 유지보수성 향상

---
## 🔧 프로젝트 종료 후 개선 작업

프로젝트 완료 후, 스케줄러 구조의 잠재적 성능 문제를 직접 발견하고 별도로 개선을 시도한 내용입니다.

### 대용량 데이터 처리 성능 최적화 (100만 건)

**[상황]**  
10분 주기 스케줄러로 100만 건의 더미 데이터를 DB에 저장하는 과정에서  
실제 실행 시간이 **20분을 초과**, 스케줄러가 겹치는 구조적 문제가 발생했습니다.

<br>

**[테스트 환경 구성]**

실제 외부 API(Codef) 없이 검증하기 위해 Mock 데이터를 직접 생성하여 사용했습니다.

| 구분 | 내용 |
|---|---|
| DB 데이터 | `MockDataGenerator`로 user, user_asset **10만 건** 직접 삽입 |
| CSV 파일 | `MockDataGenerator`로 지출 내역 **100만 건** TXT 파일 생성 |
| CSV 포맷 | `connectedId, codefTransactionId, amount, description, expenditureDate` |

**[실행 흐름]**

MockDataGenerator 실행
→ user / user_asset 10만 건 DB 삽입
→ 지출 내역 CSV 파일 생성 (ht_expenses.txt)
스케줄러 실행 (10분 주기)
→ CSV 파일 한 줄씩 읽기
→ connectedId로 user_asset 조회 → userId, assetId 획득
→ ExpenseVO 생성 후 expenditure 테이블 INSERT

**[발견]**  
로그 분석을 직접 수행하여 Full Scan 방식의 병목 구간을 파악했습니다.  
> 소규모 테스트에서는 보이지 않던 문제가 대용량 환경에서 드러남

**[해결]**

| 방안 | 내용 |
|---|---|
| 인덱싱 전략 | 자주 조회되는 `connected_id` 컬럼에 Index 설정 → 조회 효율 개선 |
| 배치 처리 | Spring Batch 도입, 50,000건 단위 chunk 처리 → 트랜잭션 부하 분산 |

**[성과]**  
> 데이터 처리 시간 **20분 초과 → 약 2분으로 단축** (약 10배 개선)
