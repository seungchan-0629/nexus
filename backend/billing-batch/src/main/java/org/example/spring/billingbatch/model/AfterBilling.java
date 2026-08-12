package org.example.spring.billingbatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class AfterBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private Long storeIdx;

    private Integer totalPayAmount;

    private String payedMonth; // yyyy-MM 형식

    private Boolean isPaid; // 결제 시도 여부 혹은 기존 필드 유지

    private Boolean isSuccess; // 성공 여부 (추가)

    private String failReason; // 실패 사유 (추가)

    @Builder.Default
    private Boolean isRetryFailed = false; // 재시도 실패 여부 (추가)

    private LocalDateTime createdAt; // 생성 일시
}
