<p align="center">
  <img src="../docs/nexus%20%EB%8C%80%ED%91%9C%20%EC%9D%B4%EB%AF%B8%EC%A7%80.png" alt="Nexus — 더벤티 프랜차이즈 통합 관리 시스템" width="100%"/>
</p>

<h3 align="center">🎨 Frontend — Vue 3 + Vite 기반 통합 웹 클라이언트</h3>

<br>

## 🤼‍♂️ 팀원 소개

<br>

| 권민석 | 노승찬 | 이재혁 | 이지희 | 정동현 |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://github.com/RIMIN0650.png" width="96" alt="권민석"/> | <img src="https://github.com/seungchan-0629.png" width="96" alt="노승찬"/> | <img src="https://github.com/hijaehyuk.png" width="96" alt="이재혁"/> | <img src="https://github.com/dwg0245.png" width="96" alt="이지희"/> | <img src="https://github.com/DongHyunj.png" width="96" alt="정동현"/> |
| [@RIMIN0650](https://github.com/RIMIN0650) | [@seungchan-0629](https://github.com/seungchan-0629) | [@hijaehyuk](https://github.com/hijaehyuk) | [@dwg0245](https://github.com/dwg0245) | [@DongHyunj](https://github.com/DongHyunj) |
| 로그인 / 회원<br/>ESG 대시보드<br/>결제 BATCH | 제품 / 카테고리<br/>배송 / 정산<br/>결제수단관리 | 재고 / 입출고<br/>주문 / 마감<br/>뉴스 요약<br/>승인 처리 BATCH | 가맹점 / 메뉴<br/>AI 챗봇<br/>POS MSA | 발주 / 알림<br/>대시보드<br/>통계 MSA<br/>인프라 일부 |

<br>

## 📌 기술 스택

<div align="center">
<img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white"/>
<img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white"/>
<img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=JavaScript&logoColor=black"/>
<img src="https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=Vue.js&logoColor=white"/>
<img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white"/>
<img src="https://img.shields.io/badge/Vue_Router-4FC08D?style=for-the-badge&logo=Vue.js&logoColor=white"/>
<img src="https://img.shields.io/badge/Pinia-FFD43B?style=for-the-badge&logo=Pinia&logoColor=black"/>
<img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=Axios&logoColor=white"/>
<img src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white"/>
<img src="https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white"/>
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
<img src="https://img.shields.io/badge/PortOne-3182F6?style=for-the-badge&logo=&logoColor=white"/>
<img src="https://img.shields.io/badge/ESLint-4B32C3?style=for-the-badge&logo=eslint&logoColor=white"/>
<img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
</div>

<br>

### 📋 상세 기술 스택

| 카테고리 | 라이브러리 | 버전 | 용도 |
|---|---|:---:|---|
| 🎨 **Core** | Vue.js | 3.5 | 프레임워크 |
| ⚡ **Build** | Vite | 8.0 | 개발 서버 / 빌드 도구 |
| 🛣️ **Routing** | Vue Router | 4.x | SPA 라우팅 |
| 💾 **State** | Pinia | 3.0 | 상태 관리 |
| 📡 **HTTP** | Axios | 1.15 | REST API 호출 (쿠키 기반 인증) |
| 🎨 **Styling** | Tailwind CSS | 4.2 | 유틸리티 CSS |
| 📊 **Chart** | Chart.js | 4.5 | 차트 / 시각화 |
| 🎯 **Icon** | Lucide Vue Next | - | 아이콘 컴포넌트 |
| 💳 **Payment** | PortOne Browser SDK, tosspayment | 0.1.6 | 결제 게이트 연동 |
| 🔍 **Lint** | ESLint + Oxlint | - | 코드 품질 / 스타일 검사 |
| 🌐 **Deploy** | Nginx | 1.25 | 운영 배포 (TLS + Reverse Proxy) |

---

## 📁 디렉토리 구조

<details>
<summary><b>전체 구조 보기</b></summary>

```
frontend/
├── public/                       # 정적 파일
├── nginx/                        # 운영 배포용 nginx 설정
├── src/
│   ├── api/                      # 도메인별 API 모듈 (axios) - 19개
│   │   ├── category/  dashboard/  delivery/  headincome/
│   │   ├── headinventory/  inventory/  menu/  news/
│   │   ├── notification/  orders/  pos/  product/
│   │   ├── report/  settlement/  statistics/  store/
│   │   ├── store-dashboard/  user/  wastelog/
│   ├── assets/                   # 이미지 / 글로벌 스타일
│   ├── components/               # 재사용 컴포넌트
│   ├── composables/              # 재사용 로직 (use*)
│   ├── constants/                # 상수
│   ├── plugins/
│   │   └── axiosinterceptor.js   # JWT 자동 첨부 + 401 처리
│   ├── router/
│   │   └── index.js              # Vue Router 라우팅
│   ├── stores/                   # Pinia
│   │   ├── auth.js               # 로그인 / 사용자 상태
│   │   └── notification.js       # 알림 SSE 구독
│   ├── utils/                    # 유틸 함수
│   ├── views/                    # 페이지 컴포넌트
│   │   ├── LoginView.vue
│   │   ├── ProfileView.vue
│   │   ├── hq/                   # 본사 페이지 (16개)
│   │   └── store/                # 가맹점 페이지 (11개)
│   ├── App.vue                   # 루트 컴포넌트
│   └── main.js                   # 앱 진입점
├── vite.config.js
├── tailwind.config.js
├── eslint.config.js
└── package.json
```
</details>

---

## 🪟 페이지

<details>
<summary><span style="font-size:1.2em;font-weight:bold;">👉 본사 ( <code>views/hq/</code> ) — 16개 페이지</span></summary>

| 파일 | 페이지 |
|---|---|
| DashboardView | 본사 대시보드 (KPI + 차트) |
| HqOrderManageView | 발주 관리 (자동 / 확정 / 이력 / 이상) |
| DeliveryView | 배송 관리 |
| InventoryHeadOfficeView | 본사 재고 |
| InventoryHistoryView | 재고 이력 |
| InventoryView | 재고 관리 |
| StoreView | 가맹점 관리 |
| ProductView | 상품 (원자재) |
| RecipeView | 메뉴 레시피 |
| StatisticsView | 실시간 통계 |
| LongTermStatisticsView | 장기 통계 (월 / 분기 / 연도) |
| SettlementView | 정산 |
| ReportView | 리포트 |
| EsgDashboardView | ESG 대시보드 |
| HqNotificationView | 알림 |
| AdminAccountCreateView | 본사 계정 생성 |

</details>

<details>
<summary><span style="font-size:1.2em;font-weight:bold;">👉 가맹점 ( <code>views/store/</code> ) — 11개 페이지</span></summary>

| 파일 | 페이지 |
|---|---|
| PosView | POS 결제 화면 |
| PaymentView | 결제 |
| StoreDashboardView | 가맹점 대시보드 |
| StoreInventoryView | 매장 재고 |
| StoreOrderManageView | 발주 관리 (수동 / AI 추천 확인) |
| StoreDeliveryView | 배송 현황 |
| StoreProductView | 상품 |
| StoreRecipeView | 레시피 |
| StoreSettlementView | 정산 내역 |
| StoreNewsView | 소식 |
| StoreNotificationView | 알림 |

</details>

---

## 🎬 기능 테스트

> 본사 / 가맹점 영역을 펼친 뒤, 각 기능을 다시 펼쳐서 시연 영상을 확인하실 수 있습니다.

<details>
<summary><span style="font-size:1.3em;font-weight:bold;">🏢 본사 (Head)</span></summary>

<details>
<summary><b>🔐 로그인 / 회원가입</b></summary>
  
https://github.com/user-attachments/assets/c109b564-af28-4343-af63-e4312a4c4173

</details>

<details>
<summary><b>👤 마이페이지</b></summary>
<br>
<p align="center"><img width="80%" src="../docs/img/gif/login.gif"/></p>
</details>

<details>
<summary><b>📊 본사 대시보드</b></summary>

https://github.com/user-attachments/assets/de76e3eb-00f5-4306-baac-593f19009722

</details>

<details>
<summary><b>🏪 가맹점 관리</b></summary>


https://github.com/user-attachments/assets/929b48dc-0b6a-4a65-bbc6-cabf44bfa1ff

https://github.com/user-attachments/assets/6ccd5fca-eb97-4f8e-b77d-919d90f725a3

</details>

<details>
<summary><b>📦 제품 목록</b></summary>

https://github.com/user-attachments/assets/9adfeb48-927b-4c9f-aa27-fe6c1184826e

</details>

<details>
<summary><b>☕ 메뉴 관리</b></summary>

https://github.com/user-attachments/assets/b5fba72a-f75d-4343-aa39-b61caab16009

</details>

<details>
<summary><b>📋 자동 발주 제안</b></summary>

https://github.com/user-attachments/assets/f0487da3-5c45-42a0-83ad-f83c715aefa1

</details>

<details>
<summary><b>✅ 확정 발주</b></summary>

https://github.com/user-attachments/assets/9a168091-d858-470d-8881-95ec7b5c6ab0

</details>

<details>
<summary><b>📜 발주 이력</b></summary>

https://github.com/user-attachments/assets/04ca4200-108b-4e2c-a2d5-07ed424f6eb6

</details>

<details>
<summary><b>⚠️ 이상 발주</b></summary>

https://github.com/user-attachments/assets/8c6b98e6-ce02-4e22-8183-3c155f87e64a

</details>

<details>
<summary><b>⚙️ 이상 발주 기준 설정</b></summary>

https://github.com/user-attachments/assets/c5a66746-32fe-484a-ae21-120db58b01a2

</details>

<details>
<summary><b>🗃️ 본사 재고 현황</b></summary>

https://github.com/user-attachments/assets/8c8a9b19-6347-4d05-ae1a-1afeb5d2c0c1

</details>

<details>
<summary><b>🏬 가맹점 재고 현황</b></summary>

https://github.com/user-attachments/assets/bec58d04-76aa-4e70-862c-6e74a5aa9a8c

</details>

<details>
<summary><b>🚚 배송 관리</b></summary>

https://github.com/user-attachments/assets/30dadc00-3fa6-45fb-b5be-6eca07cb6acf

</details>

<details>
<summary><b>💰 정산 관리</b></summary>

https://github.com/user-attachments/assets/7e9e8758-f049-4e37-b911-79f5a322a291

</details>

<details>
<summary><b>👥 계정 관리</b></summary>
<br>
<p align="center"><img width="80%" src="../docs/img/gif/login.gif"/></p>
</details>

<details>
<summary><b>📈 실시간 통계</b></summary>

https://github.com/user-attachments/assets/e92f1926-9ced-44df-b42b-95fc02755d57

</details>

<details>
<summary><b>📉 장기 통계</b></summary>

https://github.com/user-attachments/assets/b912272a-0b39-486f-ae4c-18a2e6439208

</details>

<details>
<summary><b>🌱 ESG 대시보드</b></summary>
<br>
<p align="center"><img width="80%" src="../docs/img/gif/login.gif"/></p>
</details>

<details>
<summary><b>📄 보고서</b></summary>

https://github.com/user-attachments/assets/7c044219-2131-4404-8fc8-6714cfb3517b

</details>

<details>
<summary><b>📰 뉴스 요약</b></summary>

https://github.com/user-attachments/assets/0829ed44-eeed-4f39-b582-e472355a8245

</details>

<details>
<summary><b>🔔 실시간 알림 (SSE)</b></summary>

https://github.com/user-attachments/assets/c1ce6550-4707-416d-9f41-82759ead7e31

</details>

</details>

<details>
<summary><span style="font-size:1.3em;font-weight:bold;">🏪 가맹점 (Store)</span></summary>

<details>
<summary><b>🔐 로그인 / 회원가입</b></summary>
  
https://github.com/user-attachments/assets/0efb363e-1df6-4d1a-bd36-6d13dc854981

</details>

<details>
<summary><b>📊 가맹점 대시보드</b></summary>

https://github.com/user-attachments/assets/028f019e-afa7-427e-b708-95559ce63cc0

</details>

<details>
<summary><b>💳 POS 결제</b></summary>

https://github.com/user-attachments/assets/6ecf9980-f869-47ee-b30e-4c28ad5f6067

</details>

<details>
<summary><b>🌙 영업 마감 → AI 자동 발주서 생성</b></summary>

https://github.com/user-attachments/assets/b747bdc1-6a4d-4e43-8830-13b3ff00c922

https://github.com/user-attachments/assets/d76a2f00-5ab2-492d-a853-18985f9b3cc8

</details>

<details>
<summary><b>📋 제안 발주서</b></summary>

https://github.com/user-attachments/assets/f78e7130-eba9-49a8-ae31-f750c03c2a98

</details>

<details>
<summary><b>📜 발주 이력</b></summary>

https://github.com/user-attachments/assets/2e9a2c2f-c477-4d43-adad-170e4a7d284f

</details>

<details>
<summary><b>✏️ 수동 발주 생성</b></summary>

https://github.com/user-attachments/assets/07948962-1833-4c13-b74c-8bf3a452f989

</details>

<details>
<summary><b>📦 제품 관리</b></summary>

https://github.com/user-attachments/assets/a3ad0baf-38d7-4215-9e0f-9df17434c72f

</details>

<details>
<summary><b>☕ 메뉴 관리</b></summary>

https://github.com/user-attachments/assets/e803736f-dedf-49c4-b704-1501d2fa988b

</details>

<details>
<summary><b>🗃️ 매장 재고</b></summary>

https://github.com/user-attachments/assets/04246473-3ae0-4d17-bab5-f78a71bc7bca

</details>

<details>
<summary><b>🚚 배송 현황</b></summary>

https://github.com/user-attachments/assets/7bd34e8e-d20e-4822-921f-a219048feaa9

</details>

<details>
<summary><b>💰 정산 내역</b></summary>
<br>

<video src="https://github.com/user-attachments/assets/81af6c01-6d89-4c7a-9630-608b21633ceb" controls width="100%"></video>
<video src="https://github.com/user-attachments/assets/c0a4eab0-abd9-4455-9d76-c120cad1c976" controls width="100%"></video>

</details>

<details>
<summary><b>🧪 결제 수단 관리</b></summary>

https://github.com/user-attachments/assets/fdb33fd4-771c-4efd-8f09-eaa8ccaa5bee

</details>

<details>
<summary><b>🔔 알림</b></summary>

https://github.com/user-attachments/assets/4bc63c4c-f273-4f93-8fcc-a2dc1e65a004

</details>

</details>

---

## 🛣️ API 라우팅 (Vite Proxy)

<details>
<summary><b>로컬 dev proxy 규칙</b></summary>

`vite.config.js` 의 proxy 규칙 — 로컬 dev 환경에서 백엔드 호출 분기:

| 프론트 경로 | 대상 | 비고 |
|---|---|---|
| `/api/pos/**` | `http://localhost:8080` | 로컬에서 Gateway 없을 때 모놀리식 직접 (Gateway 띄울 시 `:8088`) |
| `/api/statistics/**` | `http://localhost:8081` | 통계 MSA 직접 (또는 Gateway) |
| `/api/**` | `http://localhost:8080` | 모놀리식 |

운영 환경에서는 nginx 가 라우팅 (아래 운영 배포 참조).
</details>

---

## 📡 API 호출 컨벤션

<details>
<summary><b>Axios 인스턴스 (plugins/axiosinterceptor.js)</b></summary>
<br>

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  withCredentials: true,   // 🍪 쿠키 자동 전송 (JWT HttpOnly 쿠키 인증)
})

export default api
```

- **baseURL** — 환경변수 `VITE_API_URL` 또는 기본 `/api` (Vite proxy 가 `/api/*` 를 백엔드로 분기)
- **withCredentials: true** — 🍪 **쿠키 기반 JWT 인증** (HttpOnly 쿠키 자동 전송, Bearer 헤더 X)
- 인증 / 401 처리는 Vue Router 가드 + 백엔드 Spring Security 가 담당

> 💡 파일명에 `interceptor` 가 들어가지만 현재 별도 axios interceptor (request / response) 는 없음. 향후 BaseResponse 자동 핸들링이나 401 리다이렉트가 필요하면 이 파일에 추가.
</details>

<details>
<summary><b>API 모듈 패턴 (api/&lt;domain&gt;/index.js)</b></summary>
<br>

도메인별로 두 가지 패턴이 혼재합니다 (점진적으로 통일 예정).

**패턴 1 : Named export (`api/store/`, `api/menu/` 등 다수)**
```javascript
import api from '@/plugins/axiosinterceptor'

export const getStoreList = (searchReq, page, size) =>
  api.get('/store/list', { params: { ...searchReq, page, size } })

export const getStoreDetailList = (storeIdx) =>
  api.get(`/store/detail/list/${storeIdx}`)

export const searchStoreList = (keyword = '') =>
  api.get('/store/search', { params: { keyword } })
```

**패턴 2 : Default export 모음 (`api/orders/`)**
```javascript
import api from '@/plugins/axiosinterceptor'

const getAutoOrders = (params = {}) =>
  api.get('/orders/list/auto', { params })

const cancelOrder = (ordersIdx) =>
  api.put(`/orders/store/${ordersIdx}/cancel`)

const deleteStoreItem = (ordersItemIdx) =>
  api.delete(`/orders/store/${ordersItemIdx}/items`)

export default {
  getAutoOrders,
  cancelOrder,
  deleteStoreItem,
  // ... 함수들 모음
}
```

**사용처에서 import**
```javascript
// named export 패턴
import { getStoreList } from '@/api/store'

// default export 모음 패턴
import ordersApi from '@/api/orders'
ordersApi.getAutoOrders()
```
</details>

---

## 💾 상태 관리 (Pinia)

<details>
<summary><b>stores/auth.js</b></summary>

- **사용자 정보** — `localStorage('nexus_user')` 영속 (id, email, role, name, avatar 등)
- **🍪 쿠키 기반 JWT 인증** — `CTOKEN` 쿠키에서 JWT payload 추출 → user 객체 빌드
- **role 매핑** — 백엔드 `STORE` → 프론트 `STORE_OWNER`
- **computed**: `isLoggedIn` / `isAdmin` / `isStoreOwner`
- **액션**: `login(email, password)` / `logout()` / `fetchUserInfo()` (`/user/mypage`)
- 로그인 / 로그아웃 액션
</details>

<details>
<summary><b>stores/notification.js</b></summary>

- **알림 리스트** — Slice 페이징 (`notifications`, `hasNext`, `currentPage`)
- **필터 상태** — `currentType` (NotificationType) / `currentIsRead` (true/false/undefined)
- **미읽음 카운트** — `unreadCount` (헤더 뱃지)
- **SSE 구독** — `EventSource` 로 본사 (`HeadNotificationApi`) / 가맹점 (`StoreNotificationApi`) 알림 채널 구독
- **액션**: `fetchNotifications(type, size, isRead)` / `loadMore(size)` / `fetchUnreadCount()` / `markRead(idx)` / `markAllAsRead()` / `connectSse()` / `disconnectSse()`
</details>

---



![footer](https://capsule-render.vercel.app/api?type=Venom&color=4F46E5&height=120&section=footer&fontColor=ffffff)
