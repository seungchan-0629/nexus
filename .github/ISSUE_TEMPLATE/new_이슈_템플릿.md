---
name: NEW 이슈 템플릿
about: 새로 바뀐 이슈 템플릿
title: "[Feat] 제목 작성"
labels: Feat
assignees: ''

---

## 📋 요구사항 정보

| 항목 | 내용 |
|------|------|
| **요구사항 ID** | `ORDER_001` |
| **분류** | 백엔드 > 발주 |
| **기능** | 발주 도메인 기본 구조 세팅 |
| **담당자** | - |
| **우선순위** | - |
| **구현 대상** | 백엔드 |

## 📌 요구사항

- 발주(Order) 도메인의 Entity, Repository, Service, Controller를 구현한다.

## 📝 상세 내용

- `Order` Entity 필드 정의 및 JPA 매핑
- `OrderRepository` 인터페이스 생성 (JpaRepository 상속)
- `OrderService` 기본 CRUD 비즈니스 로직 구현
- `OrderController` REST API 엔드포인트 구현
- 패키지 구조: `order/model`, `order/repository`, `order/service`, `order/controller`
