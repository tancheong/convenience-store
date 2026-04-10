package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sales {
    private int id;                  // PK
    private int productId;           // 판매된 상품 ID (FK)
    private String productName;      // 조회 JOIN 결과 받을 때 사용 (매출 조회용)
    private int quantity;            // 판매 수량
    private BigDecimal unitPrice;    // 판매 당시 단가
    private LocalDateTime soldAt;    // 판매 시각 (DATETIME → LocalDateTime)
    private BigDecimal totalSales;
}