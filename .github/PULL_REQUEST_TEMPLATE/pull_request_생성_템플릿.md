# PR 제목 규칙
형식: `[태그] #{이슈번호} - 작업 내용 요약`
예시: `[Feat] #316 - NewsCollect 도메인 기본 구조 구현`

## Summary
- 작업 내용에 대한 간략한 설명을 작성해 주세요.
- 예: `NewsCollect` Entity, Controller, Service, Repository 기본 구조 구현

## 변경 파일
- 수정한 파일 목록을 작성해 주세요.
- 예: `backend/src/main/java/com/example/nexus/newscollect/model/NewsCollect.java`

## 주요 변경 사항
- 구체적인 작업 내용을 체크리스트로 작성해 주세요.
- [ ] Entity 정의 및 JPA 매핑 (`NewsCollect`)
- [ ] Enum 추가 (`NewsCollectTarget`, `NewsCollectStatus`)
- [ ] Controller/Service/Repository 기본 레이어 구축
- [ ] 기타 (메서드 로직 설명 등)

## DB & Infrastructure (해당 시)
- [ ] 엔티티 수정으로 인한 테이블 스키마 변경 여부
- [ ] nGrinder 등 성능 테스트 결과 (필요 시 첨부)

## Test Plan
- 테스트 방법 및 결과를 체크리스트로 작성해 주세요.
- [ ] 서버 실행 시 테이블 정상 생성 확인
- [ ] 주요 API 엔드포인트 호출 및 응답 확인
- [ ] 테스트 코드 통과 여부

## Issue
- 관련 이슈 번호를 작성해 주세요.
- Closes #