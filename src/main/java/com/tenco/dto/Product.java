package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private int id;               // AUTO_INCREMENT PK
    private String barcode;       // 바코드 (유니크)
    private String name;          // 상품명
    private String category;      // 카테고리 (식품/음료/과자)
    private BigDecimal price;     // 판매가 (DECIMAL → BigDecimal)
    private BigDecimal cost;      // 원가
    private int stock;            // 현재 재고
    private int minStock;         // 최소 재고 (이 미만이면 부족 알림)
    // LocalDate: Java 8 날짜 API입니다.
    // java.util.Date (Legacy) - 멀티 스레드에 안전 함
    // 월(Month) 기준 0 부터 / (Month) LocalDate 1부터 시작
    private LocalDate expireDate; // 유통기한 (DATE → LocalDate)
    private boolean isActive;     // 소프트 삭제 여부 (TRUE = 정상)
}